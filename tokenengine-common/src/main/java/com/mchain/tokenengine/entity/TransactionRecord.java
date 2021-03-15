package com.mchain.tokenengine.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.FieldFill;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.mchain.tokenengine.common.SuperModel;
import com.mchain.tokenengine.enums.TransactionOpenStatusEnum;
import com.mchain.tokenengine.enums.TransactionStatusEnum;
import com.mchain.tokenengine.enums.TransactionTypeEnum;
import com.mchain.tokenengine.utils.eth.result.ETHTransactionResult;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 交易信息记录表
 * </p>
 *
 * @author koc
 * @since 2018-09-06
 */
@TableName("transaction_record")
public class TransactionRecord extends SuperModel<TransactionRecord> {

	private static final long serialVersionUID = 1L;

	/**
	 * 币id, 与coin中的id关联
	 */
	@TableField("coin_id")
	private Long coinId;
	/**
	 * 交易Hash
	 */
	private String hash;
	/**
	 * From地址, 发送发
	 */
	@TableField("from_address")
	private String fromAddress;
	/**
	 * To地址, 接收方
	 */
	@TableField("to_address")
	private String toAddress;
	/**
	 * 数量
	 */
	private BigDecimal amount;
	/**
	 * GasLimit
	 */
	@TableField("gas_limit")
	private BigDecimal gasLimit;
	/**
	 * 已使用的Gas
	 */
	@TableField("use_gas")
	private BigDecimal useGas;
	/**
	 * GasPrice
	 */
	@TableField("gas_price")
	private BigDecimal gasPrice;
	/**
	 * 旷工费
	 */
	private BigDecimal fee;
	/**
	 * 交易状态: 等待中:0 成功:1 失败:2
	 */
	private TransactionStatusEnum status;
	/**
	 * 操作状态(0-未操作, 1-已操作)
	 */
	@TableField("open_status")
	private TransactionOpenStatusEnum openStatus;
	/**
	 * 确认交易时间 (等待中的交易该字段为空)
	 */
	@TableField("transfer_time")
	private Date transferTime;
	/**
	 * 交易类型: 1-用户转入, 2-转入冷钱包, 3-提币, 4-退币
	 */
	private TransactionTypeEnum type;
	/**
	 * 更新时间
	 */
	@TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateTime;

	public static TransactionRecord init(Long coinId, ETHTransactionResult transaction, Date transferTime,
			TransactionTypeEnum type) {
		TransactionRecord transactionRecord = new TransactionRecord();
		transactionRecord.setCoinId(coinId);
		transactionRecord.setHash(transaction.getHash());
		transactionRecord.setFromAddress(transaction.getFrom());
		transactionRecord.setToAddress(transaction.getTo());
		transactionRecord.setAmount(transaction.getValue());
		transactionRecord.setTransferTime(transferTime);
		transactionRecord.setType(type);
		transactionRecord.setOpenStatus(TransactionOpenStatusEnum.UNOPENED);
		transactionRecord.setGasLimit(BigDecimal.valueOf(transaction.getGas().longValue()));
		transactionRecord.setUseGas(BigDecimal.valueOf(transaction.getUseGas().longValue()));
		transactionRecord.setGasPrice(BigDecimal.valueOf(transaction.getGasPrice().longValue()));
		transactionRecord.setGasPrice(transaction.getFee());
		if (transaction.isSuccess()) {
			transactionRecord.setStatus(TransactionStatusEnum.SUCCESS);
		} else if (transaction.isPending()) {
			transactionRecord.setStatus(TransactionStatusEnum.PENDING);
		} else {
			transactionRecord.setStatus(TransactionStatusEnum.FAIL);
		}
		return transactionRecord;
	}

	public static TransactionRecord initCoin(Long coinId, Integer unit, ETHTransactionResult transaction,
			Date transferTime, TransactionTypeEnum type) {
		transaction.setTo(transaction.getInputData().getToAddress());
		// 根据合约中，小数长度，该合约为8
		transaction.setValue(new BigDecimal(transaction.getInputData().getWeiValue()).movePointLeft(unit));
		return init(coinId, transaction, transferTime, type);
	}

	public Long getCoinId() {
		return coinId;
	}

	public TransactionRecord setCoinId(Long coinId) {
		this.coinId = coinId;
		return this;
	}

	public String getHash() {
		return hash;
	}

	public TransactionRecord setHash(String hash) {
		this.hash = hash;
		return this;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public TransactionRecord setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
		return this;
	}

	public String getToAddress() {
		return toAddress;
	}

	public TransactionRecord setToAddress(String toAddress) {
		this.toAddress = toAddress;
		return this;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public TransactionRecord setAmount(BigDecimal amount) {
		this.amount = amount;
		return this;
	}

	public BigDecimal getGasLimit() {
		return gasLimit;
	}

	public void setGasLimit(BigDecimal gasLimit) {
		this.gasLimit = gasLimit;
	}

	public BigDecimal getUseGas() {
		return useGas;
	}

	public void setUseGas(BigDecimal useGas) {
		this.useGas = useGas;
	}

	public BigDecimal getGasPrice() {
		return gasPrice;
	}

	public void setGasPrice(BigDecimal gasPrice) {
		this.gasPrice = gasPrice;
	}

	public BigDecimal getFee() {
		return fee;
	}

	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	public TransactionStatusEnum getStatus() {
		return status;
	}

	public TransactionRecord setStatus(TransactionStatusEnum status) {
		this.status = status;
		return this;
	}

	public TransactionOpenStatusEnum getOpenStatus() {
		return openStatus;
	}

	public TransactionRecord setOpenStatus(TransactionOpenStatusEnum openStatus) {
		this.openStatus = openStatus;
		return this;
	}

	public Date getTransferTime() {
		return transferTime;
	}

	public TransactionRecord setTransferTime(Date transferTime) {
		this.transferTime = transferTime;
		return this;
	}

	public TransactionTypeEnum getType() {
		return type;
	}

	public TransactionRecord setType(TransactionTypeEnum type) {
		this.type = type;
		return this;
	}

	public static final String COIN_ID = "coin_id";

	public static final String HASH = "hash";

	public static final String FROM_ADDRESS = "from_address";

	public static final String TO_ADDRESS = "to_address";

	public static final String AMOUNT = "amount";

	public static final String GAS_LIMIT = "gas_limit";

	public static final String USE_GAS = "use_gas";

	public static final String GAS_PRICE = "gas_price";

	public static final String FEE = "fee";

	public static final String STATUS = "status";

	public static final String OPEN_STATUS = "open_status";

	public static final String TRANSFER_TIME = "transfer_time";

	public static final String TYPE = "type";

	public static final String UPDATE_TIME = "update_time";

	@Override
	public String toString() {
		return "TransactionRecord{" + "coinId=" + coinId + ", hash=" + hash + ", fromAddress=" + fromAddress
				+ ", toAddress=" + toAddress + ", amount=" + amount + ", gasLimit=" + gasLimit + ", useGas=" + useGas
				+ ", gasPrice=" + gasPrice + ", fee=" + fee + ", status=" + status + ", openStatus=" + openStatus
				+ ", transferTime=" + transferTime + ", type=" + type + ", updateTime=" + updateTime + "}";
	}

	//	public static void main(String[] args) throws Exception {
	//		Method refMethod = TypeDecoder.class.getDeclaredMethod("decode", String.class, int.class, Class.class);
	//		refMethod.setAccessible(true);
	//		Uint256 unitValue = (Uint256) refMethod
	//				.invoke(null, "0000000000000000000000000000000000000000000000000000000005f5e100", 0, Uint256.class);
	//		BigInteger i = unitValue.getValue();
	//		System.out.println(i);
	//		System.out.println(new BigInteger(i.toString(), 10).toString(16));
	//		System.out.println(new BigDecimal(i).movePointLeft(8));
	//	}
}
