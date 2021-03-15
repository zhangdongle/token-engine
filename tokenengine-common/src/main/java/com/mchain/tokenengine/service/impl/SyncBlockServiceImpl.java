package com.mchain.tokenengine.service.impl;

import com.baomidou.mybatisplus.mapper.Condition;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.mchain.tokenengine.entity.SyncBlock;
import com.mchain.tokenengine.enums.ChainTypeEnum;
import com.mchain.tokenengine.mapper.SyncBlockMapper;
import com.mchain.tokenengine.service.SyncBlockService;
import com.mchain.tokenengine.utils.eth.utils.EthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;

import java.math.BigInteger;
import java.util.Optional;

/**
 * <p>
 * 区块同步高度记录表 服务实现类
 * </p>
 *
 * @author koc
 * @since 2018-09-06
 */
@Service
public class SyncBlockServiceImpl extends ServiceImpl<SyncBlockMapper, SyncBlock> implements SyncBlockService {
	@Autowired
	private EthUtil ethUtil;

	@Override
	public SyncBlock getOrInsertNumberByChainType(ChainTypeEnum chainType) {
		EntityWrapper<SyncBlock> wrapper = Condition.wrapper();
		wrapper.eq(SyncBlock.CHAIN_TYPE, chainType);
		return Optional.ofNullable(this.selectOne(wrapper)).orElseGet(() -> {
			Web3j web3j = ethUtil.getWeb3j();
			BigInteger blockNumber = ethUtil.getBlockNumber(web3j);
			web3j.shutdown();

			SyncBlock syncBlock = new SyncBlock();
			syncBlock.setChainType(ChainTypeEnum.ETH);
			syncBlock.setNumber(blockNumber);
			syncBlock.insert();

			return syncBlock;
		});
	}

}
