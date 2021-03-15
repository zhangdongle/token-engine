package com.mchain.tokenengine.service.impl;

import com.baomidou.mybatisplus.mapper.Condition;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.mchain.tokenengine.entity.Coin;
import com.mchain.tokenengine.mapper.CoinMapper;
import com.mchain.tokenengine.service.CoinService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 币种表 服务实现类
 * </p>
 *
 * @author koc
 * @since 2018-09-06
 */
@Service
public class CoinServiceImpl extends ServiceImpl<CoinMapper, Coin> implements CoinService {

	@Override
	public Coin getByCoinName(String name) {
		EntityWrapper wrapper = Condition.wrapper();
		wrapper.eq(Coin.COIN_NAME,name);
		return selectOne(wrapper);
	}
}
