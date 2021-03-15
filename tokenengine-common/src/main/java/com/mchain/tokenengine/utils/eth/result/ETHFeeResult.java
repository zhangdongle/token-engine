package com.mchain.tokenengine.utils.eth.result;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 以太坊手续费 返回类
 */
public class ETHFeeResult {

    private BigInteger gasLimit;

    private BigInteger gasPrice;

    private BigDecimal transactionFee;

    public ETHFeeResult(BigInteger gasLimit, BigInteger gasPrice, BigDecimal transactionFee) {
        this.gasLimit = gasLimit;
        this.gasPrice = gasPrice;
        this.transactionFee = transactionFee;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public BigDecimal getTransactionFee() {
        return transactionFee;
    }

    public void setTransactionFee(BigDecimal transactionFee) {
        this.transactionFee = transactionFee;
    }
}
