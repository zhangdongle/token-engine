package com.mchain.tokenengine.utils.eth.params;

import jodd.util.StringUtil;

import java.math.BigDecimal;

/**
 * 获取余额参数类
 */
public class BalanceParams {
    private String address;
    private String contractAddress;
    private BigDecimal unit;

    public static BalanceParams createEthBalance(String address) {
        return new BalanceParams(
                address,
                null,
                null
        );
    }

    /**
     * unit 为空 则会调用 getTokenUnit() 获取合约精度
     */
    public static BalanceParams createTokenBalance(String address, String contractAddress) {
        return new BalanceParams(
                address,
                contractAddress,
                null);
    }

    public static BalanceParams createTokenBalance(String address, String contractAddress, String unit) {
        return new BalanceParams(
                address,
                contractAddress,
                unit);
    }

    public BalanceParams(String address, String contractAddress, String unit) {
        this.address = address;
        this.contractAddress = contractAddress;
        if(StringUtil.isNotBlank(unit)) {
            this.unit = new BigDecimal(unit);
        } else {
            this.unit = null;
        }
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
}
