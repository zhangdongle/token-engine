package com.mchain.tokenengine.dto;

import lombok.Data;

@Data
public class SyncBlockInfo {

	/**
	 * 合约地址
	 */
	private String contractAddress;
	/**
	 * 区块高度
	 */
	private Integer number;
	/**
	 * 币种ID
	 */
	private Integer coinId;

	/**
	 * 币种单位精度
	 */
	private Integer coinUnit;
}
