package com.mchain.tokenengine.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.mchain.tokenengine.common.SuperModel;
import com.mchain.tokenengine.enums.ChainTypeEnum;
import com.mchain.tokenengine.enums.CoinTypeEnum;

import java.math.BigDecimal;

/**
 * <p>
 * 币种表
 * </p>
 *
 * @author koc
 * @since 2018-09-06
 */
public class Coin extends SuperModel<Coin> {

	private static final long serialVersionUID = 1L;

	/**
	 * 币名称
	 */
	@TableField("coin_name")
	private String coinName;
	/**
	 * 链类型: 以太坊:1
	 */
	@TableField("chain_type")
	private ChainTypeEnum chainType;
	/**
	 * 币类型: 主币:0 认购币:1
	 */
	@TableField("coin_type")
	private CoinTypeEnum coinType;

	/**
	 * 币种小数长度
	 */
	private Integer unit;
	/**
	 * 冷钱包地址(子钱包汇集处)
	 */
	@TableField("cold_address")
	private String coldAddress;
	/**
	 * 转入冷钱包触发值
	 */
	private BigDecimal threshold;
	/**
	 * 合约地址(主币该字段为空)
	 */
	@TableField("contract_address")
	private String contractAddress;
	/**
	 * IP
	 */
	private String ip;
	/**
	 * 端口
	 */
	private String port;


	public String getCoinName() {
		return coinName;
	}

	public Coin setCoinName(String coinName) {
		this.coinName = coinName;
		return this;
	}

	public ChainTypeEnum getChainType() {
		return chainType;
	}

	public Coin setChainType(ChainTypeEnum chainType) {
		this.chainType = chainType;
		return this;
	}

	public CoinTypeEnum getCoinType() {
		return coinType;
	}

	public Coin setCoinType(CoinTypeEnum coinType) {
		this.coinType = coinType;
		return this;
	}

	public Integer getUnit() {
		return unit;
	}

	public Coin setUnit(Integer unit) {
		this.unit = unit;
		return this;
	}

	public String getColdAddress() {
		return coldAddress;
	}

	public Coin setColdAddress(String coldAddress) {
		this.coldAddress = coldAddress;
		return this;
	}

	public BigDecimal getThreshold() {
		return threshold;
	}

	public void setThreshold(BigDecimal threshold) {
		this.threshold = threshold;
	}

	public String getContractAddress() {
		return contractAddress;
	}

	public Coin setContractAddress(String contractAddress) {
		this.contractAddress = contractAddress;
		return this;
	}

	public String getIp() {
		return ip;
	}

	public Coin setIp(String ip) {
		this.ip = ip;
		return this;
	}

	public String getPort() {
		return port;
	}

	public Coin setPort(String port) {
		this.port = port;
		return this;
	}

	public static final String COIN_NAME = "coin_name";

	public static final String CHAIN_TYPE = "chain_type";

	public static final String COIN_TYPE = "coin_type";

	public static final String COLD_ADDRESS = "cold_address";

	public static final String THRESHOLD = "threshold";

	public static final String CONTRACT_ADDRESS = "contract_address";

	public static final String IP = "ip";

	public static final String PORT = "port";

	public static final String UNIT = "unit";

}
