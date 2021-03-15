package com.mchain.tokenengine.utils.eth.params;

import jodd.util.StringUtil;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 发送交易参数类
 */
public class SendTransactionParams {
    private String fromAddress;
    private String password;
    private String toAddress;
    private BigDecimal amount;

    private BigInteger gasLimit;
    private BigInteger gasPrice;

    private String contractAddress;
    private BigDecimal unit;
    private String method;

    private String data;

    public static SendTransactionParams createAllEthTransaction(String fromAddress, String password, String toAddress) {
        return new SendTransactionParams(
                fromAddress,
                password,
                toAddress,
                BigDecimal.ZERO,
                null,
                "",
                null,
                null);
    }

    public static SendTransactionParams createEthTransaction(String fromAddress, String password, String toAddress, BigDecimal amount) {
        return new SendTransactionParams(
                fromAddress,
                password,
                toAddress,
                amount,
                null,
                "",
                null,
                null);
    }

    public static SendTransactionParams createEthTransaction(String fromAddress, String password, String toAddress, BigDecimal amount, BigInteger gasLimit, BigInteger gasPrice) {
        return new SendTransactionParams(
                fromAddress,
                password,
                toAddress,
                amount,
                null,
                "",
                gasLimit,
                gasPrice);
    }

    public static SendTransactionParams createTokenTransaction(String fromAddress, String password, String toAddress, String contractAddress, BigDecimal amount, String method, String unit) {
        return new SendTransactionParams(
                fromAddress,
                password,
                toAddress,
                amount,
                contractAddress,
                unit,
                method,
                null,
                null,
                null);
    }

    public static SendTransactionParams createTokenTransaction(String fromAddress, String password, String toAddress, String contractAddress, BigDecimal amount, String method, String unit, BigInteger gasLimit, BigInteger gasPrice) {
        return new SendTransactionParams(
                fromAddress,
                password,
                toAddress,
                amount,
                contractAddress,
                unit,
                method,
                null,
                gasLimit,
                gasPrice);
    }

    public SendTransactionParams() {
    }

    public SendTransactionParams(String fromAddress, String password, String toAddress, BigDecimal amount, String contractAddress, String data, BigInteger gasLimit, BigInteger gasPrice) {
        this.fromAddress = fromAddress;
        this.password = password;
        this.toAddress = toAddress;
        this.amount = amount;
        this.contractAddress = contractAddress;
        this.data = data;
        this.gasLimit = gasLimit;
        this.gasPrice = gasPrice;
    }

    public SendTransactionParams(String fromAddress, String password, String toAddress, BigDecimal amount, String contractAddress, String unit, String method, String data, BigInteger gasLimit, BigInteger gasPrice) {
        this.fromAddress = fromAddress;
        this.password = password;
        this.toAddress = toAddress;
        this.amount = amount;
        this.contractAddress = contractAddress;
        if(StringUtil.isNotBlank(unit)) {
            this.unit = new BigDecimal(unit);
        } else {
            this.unit = null;
        }
        if(StringUtil.isNotBlank(method)) {
            this.method = method;
        } else {
            this.method = null;
        }
        this.data = data;
        this.gasLimit = gasLimit;
        this.gasPrice = gasPrice;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public BigDecimal getUnit() {
        return unit;
    }

    public void setUnit(BigDecimal unit) {
        this.unit = unit;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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
}
