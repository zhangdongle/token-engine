package com.mchain.tokenengine.service;

import com.baomidou.mybatisplus.service.IService;
import com.mchain.tokenengine.entity.SyncBlock;
import com.mchain.tokenengine.enums.ChainTypeEnum;

/**
 * <p>
 * 区块同步高度记录表 服务类
 * </p>
 *
 * @author koc
 * @since 2018-09-06
 */
public interface SyncBlockService extends IService<SyncBlock> {

	SyncBlock getOrInsertNumberByChainType(ChainTypeEnum chainType);

}
