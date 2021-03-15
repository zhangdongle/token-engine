package com.mchain.tokenengine.consumer;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.mapper.Condition;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.mchain.tokenengine.config.Web3jPool;
import com.mchain.tokenengine.constants.RabbitConstant;
import com.mchain.tokenengine.dto.RechargeNotifyDto;
import com.mchain.tokenengine.dto.SyncBlockInfo;
import com.mchain.tokenengine.entity.BankUserAddress;
import com.mchain.tokenengine.entity.TransactionRecord;
import com.mchain.tokenengine.enums.TransactionTypeEnum;
import com.mchain.tokenengine.mapper.BankUserAddressMapper;
import com.mchain.tokenengine.service.TransactionRecordService;
import com.mchain.tokenengine.utils.eth.result.ETHTransactionResult;
import com.mchain.tokenengine.utils.eth.utils.EthTransactionInfoUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
@RabbitListener(bindings = @QueueBinding(exchange = @Exchange(value = RabbitConstant.EXCHANGE_ETH, type = "topic"), key = RabbitConstant.KEY_BLOCK, value = @Queue(RabbitConstant.QUEUE_MAPBLOCK)))
public class MapBlockConsumer {

	@Resource
	private EthTransactionInfoUtil ethTransactionInfoUtil;
	//	@Resource
	//	private AccountService accountService;
	@Resource
	private BankUserAddressMapper addressMapper;
	@Resource
	private AmqpTemplate amqpTemplate;
	// 同步成功 待删除的区块
	// 保证线程安全, 多线程下ArrayList.add()会出现下标越界问题
	List<SyncBlockInfo> syncBlockList = Collections.synchronizedList(new ArrayList<>());
	@Resource
	private TransactionRecordService transactionRecordService;
	public static Web3j web3j;

	@RabbitHandler
	public void handle(String body) {

		SyncBlockInfo syncBlock;
		try {
			syncBlock = JSON.parseObject(body, SyncBlockInfo.class);
			log.info("同步区块开始:{}", syncBlock.getNumber());
			execute(syncBlock);
			log.info("同步区块完成，{}", syncBlock.getNumber());
		} catch (Exception e) {
			log.error("解析区块信息异常，{}", body);
			return;
		}


	}

	private void execute(SyncBlockInfo syncBlock) {
		if (web3j == null) {
			web3j = Web3jPool.pool();
			if (web3j == null) {
				log.error("Web3j获取连接对象超时，当前同步区块，{}", syncBlock);
				amqpTemplate.convertAndSend(RabbitConstant.QUEUE_MAPBLOCK, JSON.toJSONString(syncBlock));
				return;
			}
		}
		try {
			// 同步方法
			int result = mapBlock(web3j, syncBlock);
			if (result == 1) {
				amqpTemplate.convertAndSend(RabbitConstant.EXCHANGE_SYNCNUMBER, "", JSON.toJSONString(syncBlock));
			} else {
				amqpTemplate.convertAndSend(RabbitConstant.QUEUE_MAPBLOCK, JSON.toJSONString(syncBlock));
			}
		} catch (Exception e) {
			log.error("同步区块异常，{}", syncBlock);
			log.error("同步区块异常", e);
			// 同步异常，重新发送指令
			amqpTemplate.convertAndSend(RabbitConstant.QUEUE_MAPBLOCK, JSON.toJSONString(syncBlock));
		}

		web3j.shutdown();
	}

	private int mapBlock(Web3j web3j, SyncBlockInfo syncBlock) {
		BigInteger thisBlockNumber = BigInteger.valueOf(syncBlock.getNumber());

		// 获取区块信息
		EthBlock.Block block;
		try {
			EthBlock ethBlock = ethTransactionInfoUtil.getBlockByBlockNumber(web3j, thisBlockNumber, true);
			if (null != ethBlock.getError()) {
				log.info("检测用户转入-获取区块中的交易记录失败! blockNumber={}", syncBlock);
				return 0;
			}
			block = ethBlock.getBlock();
		} catch (Exception e) {
			log.info("检测用户转入-获取区块中的交易记录失败! {}", syncBlock);
			return 0;
		}
		if (null == block || null == block.getTimestamp()) {
			// 没有区块时间表示该区块还在打包中
			return 0;
		}

		// 完成交易时间
		Date transferTime = new Date(block.getTimestamp().multiply(BigInteger.valueOf(1000)).longValue());

		// 筛选 用户转入 且 交易成功 的交易
		List<TransactionRecord> userTransactionRecords = Collections.synchronizedList(new ArrayList<>());

		// 遍历区块中的所有交易记录
		try {
			block.getTransactions().parallelStream().filter(object -> {
				Transaction transaction = (Transaction) object.get();
				if (StringUtils.isNotBlank(transaction.getInput()) && transaction.getInput().length() == 138) {
					String method = transaction.getInput().substring(0, 10);
					if ("0xa9059cbb".equals(method)) {
						return true;
					}
				}
				return false;
			}).forEach(object -> {
				Transaction transaction = (Transaction) object.get();

				String hash = transaction.getHash();
				// 筛选成功交易
				ETHTransactionResult transactionResult = ethTransactionInfoUtil.getTransactionAllInfo(web3j, hash);
				if (!transactionResult.isSuccess()) {
					// forEach 中的 return = continue
					return;
				}

				// 查询是否为系统用户的地址
				ETHTransactionResult.Data data = transactionResult.getInputData();
				if (data != null && data.getContractAddress() != null && data.getContractAddress()
						.equals(syncBlock.getContractAddress())) {
					log.info("FROM:{} ===========> TO:{}", transaction.getFrom(), data.getToAddress());
					EntityWrapper<BankUserAddress> userWrapper = Condition.wrapper();
					userWrapper.eq(BankUserAddress.ADDRESS, data.getToAddress());
					userWrapper.eq(BankUserAddress.COIN_ID, 2);
					int userResult = addressMapper.selectCount(userWrapper);
					if (userResult > 0) {
						userTransactionRecords.add(TransactionRecord
								.initCoin(syncBlock.getCoinId().longValue(), syncBlock.getCoinUnit(), transactionResult,
										transferTime, TransactionTypeEnum.USER));
					} else {
						//					log.info("非系统用户地址：" + data.getToAddress());
					}
				}
			});
		} catch (Exception e) {
			log.error("查询交易异常:{}", syncBlock);
			//			log.error("查询交易异常", e);
			return 0;
		}
		// 保存交易记录
		transactionRecordService.batchInsertOrUpdate(userTransactionRecords);
		if (userTransactionRecords.size() > 0) {
			for (TransactionRecord record : userTransactionRecords) {
				RechargeNotifyDto dto = new RechargeNotifyDto();
				dto.setDealTime(record.getTransferTime());
				dto.setFromAddress(record.getFromAddress());
				dto.setToAddress(record.getToAddress());
				dto.setRequestNo("" + record.getId());
				dto.setCoinId(2L);
				dto.setTransAmt(record.getAmount());
				log.info("发送到账通知到Aladdin，{}", dto);
				amqpTemplate.convertAndSend("recharge.result.queue", JSON.toJSONString(dto));
			}
		}
		return 1;
	}
}
