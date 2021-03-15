package com.mchain.tokenengine.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mchain.tokenengine.common.SuperModel;
import com.mchain.tokenengine.enums.ChainTypeEnum;

import java.math.BigInteger;
import java.util.Date;

/**
 * <p>
 * 区块同步高度记录表
 * </p>
 *
 * @author koc
 * @since 2018-09-06
 */
@TableName("sync_block")
public class SyncBlock extends SuperModel<SyncBlock> {

	private static final long serialVersionUID = 1L;

	/**
	 * 链类型: 以太坊:1
	 */
	@TableField("chain_type")
	private ChainTypeEnum chainType;
	/**
	 * 同步高度
	 */
	private BigInteger number;
	/**
	 * 更新时间
	 */
	@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;


	public ChainTypeEnum getChainType() {
		return chainType;
	}

	public SyncBlock setChainType(ChainTypeEnum chainType) {
		this.chainType = chainType;
		return this;
	}

	public BigInteger getNumber() {
		return number;
	}

	public SyncBlock setNumber(BigInteger number) {
		this.number = number;
		return this;
	}

	public static final String CHAIN_TYPE = "chain_type";

	public static final String NUMBER = "number";

	@Override
	public String toString() {
		return "SyncBlock{" + "chainType=" + chainType + ", number=" + number + ", updateTime=" + updateTime + "}";
	}
}
