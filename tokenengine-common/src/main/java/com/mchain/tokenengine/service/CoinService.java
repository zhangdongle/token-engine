package com.mchain.tokenengine.service;

import com.baomidou.mybatisplus.service.IService;
import com.mchain.tokenengine.entity.Coin;

/**
 * <p>
 * 币种表 服务类
 * </p>
 *
 * @author koc
 * @since 2018-09-06
 */
public interface CoinService extends IService<Coin> {

	Coin getByCoinName(String name);

}
