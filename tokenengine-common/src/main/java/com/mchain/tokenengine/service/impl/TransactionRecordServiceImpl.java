package com.mchain.tokenengine.service.impl;

import com.baomidou.mybatisplus.mapper.Condition;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.mchain.tokenengine.entity.TransactionRecord;
import com.mchain.tokenengine.mapper.TransactionRecordMapper;
import com.mchain.tokenengine.service.TransactionRecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 交易信息记录表 服务实现类
 * </p>
 *
 * @author koc
 * @since 2018-09-06
 */
@Service
public class TransactionRecordServiceImpl extends ServiceImpl<TransactionRecordMapper, TransactionRecord>
		implements TransactionRecordService {

	@Override
	@Transactional(rollbackFor = Exception.class)
	public List<TransactionRecord> batchInsertOrUpdate(List<TransactionRecord> transactionRecordList) {
		if (null != transactionRecordList && transactionRecordList.size() > 0) {
			transactionRecordList = transactionRecordList.stream().peek(transactionRecord -> {
				EntityWrapper<TransactionRecord> wrapper = Condition.wrapper();
				wrapper.eq(TransactionRecord.HASH, transactionRecord.getHash());
				Optional.ofNullable(this.selectOne(wrapper)).ifPresent(oldTransactionRecord -> {
					transactionRecord.setId(oldTransactionRecord.getId());
				});
			}).collect(Collectors.toList());
			this.insertOrUpdateBatch(transactionRecordList);
		}
		return transactionRecordList;
	}

}
