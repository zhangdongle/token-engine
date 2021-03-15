package com.mchain.tokenengine.schedule;

import com.alibaba.fastjson.JSON;
import com.mchain.tokenengine.config.Web3jPool;
import com.mchain.tokenengine.constants.RabbitConstant;
import com.mchain.tokenengine.dto.SyncBlockInfo;
import com.mchain.tokenengine.entity.Coin;
import com.mchain.tokenengine.entity.SyncBlock;
import com.mchain.tokenengine.enums.ChainTypeEnum;
import com.mchain.tokenengine.service.CoinService;
import com.mchain.tokenengine.service.SyncBlockService;
import com.mchain.tokenengine.utils.eth.utils.EthTransactionInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.web3j.protocol.Web3j;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 检测用户转入任务
 * - @DisallowConcurrentExecution 等待任务执行往后再执行下次任务
 * @author GWELL
 */
@Slf4j
@DisallowConcurrentExecution
public class SyncBlockTask extends QuartzJobBean {

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
		sync();
	}

	//	@Value("0x${coin.contract.address}")
	//	private String contractAddress;

	@Value("${coin.name}")
	private String coinName;

	//	@Value("${coin.unit}")
	//	private Integer coinUnit;

	@Resource
	private CoinService coinService;

	@Resource
	private EthTransactionInfoUtil ethTransactionInfoUtil;
	@Resource
	private SyncBlockService syncBlockService;
	@Autowired
	private AmqpTemplate amqpTemplate;

	private BigInteger syncNumber = new BigInteger("5473639");

	private final ChainTypeEnum chainType = ChainTypeEnum.ETH;

	// 更新区块数量
	//	private final int syncCont = 10;

	// 小于20个再添加进队列
	private final int minCount = 20;

	private final BigInteger maxSyncCount = new BigInteger("50");

	/**
	 * 检测用户转入
	 */
	private void sync() {
		log.info("====> 检测用户转入 TASK START!!");

		//		if (StringUtils.isEmpty(contractAddress)) {
		//			return;
		//		}

		final Coin mainCoin = coinService.getByCoinName(coinName);

		if (mainCoin == null) {
			log.error("代币不存在，name:{}", coinName);
			return;
		}
		// 锁定主任务，不继续执行
		if (BlockNumberLock.lock == 1) {
			return;
		}

		if (BlockNumberLock.syncSize() > minCount) {
			// 待处理数大于最小数，则等待执行
			log.info("主任务待处理数：{}", BlockNumberLock.syncSize());
			return;
		}

		SyncBlock syncBlock = syncBlockService.getOrInsertNumberByChainType(chainType);
		// 更新一次数据库区块高度
		Integer saveNumber = BlockNumberLock.getSaveNumber();
		if (saveNumber != -1 && saveNumber > syncBlock.getNumber().intValue() + 10) {
			syncBlock.setNumber(BigInteger.valueOf(saveNumber));
			syncBlock.setUpdateTime(new Date());
			syncBlock.updateById();
		}

		syncNumber = syncBlock.getNumber();
		Integer number = BlockNumberLock.getMaxNumber();
		//		log.info("待同步的最大区块高度：{}", number);
		if (syncNumber.compareTo(BigInteger.valueOf(number)) < 0) {
			syncNumber = BigInteger.valueOf(number);
		}
		syncNumber = syncNumber.add(BigInteger.ONE);
		Web3j web3j = Web3jPool.pool();

		BigInteger blockNumber = syncNumber;
		BigInteger currentBlockNumber = ethTransactionInfoUtil.getBlockNumber(web3j);
		//		log.info("当前链上最大区块高度：{}", currentBlockNumber);
		if (blockNumber.compareTo(currentBlockNumber) > 0) {
			return;
			//		} else if (blockNumber.compareTo(currentBlockNumber) > 0) {
			//			blockNumber = currentBlockNumber;
		} else {
			// 这次任务需要同步的区块数
			BigInteger differenceValue = currentBlockNumber.subtract(blockNumber);
			if (differenceValue.compareTo(maxSyncCount) > 0) {
				// 如果这次任务需要同步的区块数 大于 maxSyncCount
				// 则这次任务只需要同步 10 个块
				currentBlockNumber = blockNumber.add(maxSyncCount);
			}
		}

		// 待同步的区块
		final List<Integer> blockNumberList = IntStream
				.rangeClosed(blockNumber.intValue(), currentBlockNumber.intValue()).boxed().parallel()
				.collect(Collectors.toList());
		BlockNumberLock.push(blockNumberList);

		for (int i = 0; i < blockNumberList.size(); i++) {
			SyncBlockInfo block = new SyncBlockInfo();
			block.setContractAddress(mainCoin.getContractAddress());
			block.setNumber(blockNumberList.get(i));
			block.setCoinId(mainCoin.getId().intValue());
			block.setCoinUnit(mainCoin.getUnit());
			amqpTemplate.convertAndSend(RabbitConstant.QUEUE_MAPBLOCK, JSON.toJSONString(block));
		}

		web3j.shutdown();

		log.info("====> 检测用户转入 TASK END!!");
	}
}
