package com.mchain.tokenengine.service;

import com.baomidou.mybatisplus.service.IService;
import com.mchain.tokenengine.entity.TransactionRecord;

import java.util.List;

/**
 * <p>
 * 交易信息记录表 服务类
 * </p>
 *
 * @author koc
 * @since 2018-09-06
 */
public interface TransactionRecordService extends IService<TransactionRecord> {

	List<TransactionRecord> batchInsertOrUpdate(List<TransactionRecord> transactionRecordList);

}
