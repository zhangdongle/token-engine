package com.mchain.tokenengine.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class RechargeNotifyDto {
	private String hash;
	private String fromAddress;
	private String toAddress;
	private BigDecimal transAmt;
	private Date dealTime;
	private String requestNo;
	private String userId;
	private Long coinId;

}
