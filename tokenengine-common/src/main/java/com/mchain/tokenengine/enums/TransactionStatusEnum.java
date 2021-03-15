package com.mchain.tokenengine.enums;

import com.baomidou.mybatisplus.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TransactionStatusEnum implements IEnum {
    PENDING(0, "交易进行中"),
    SUCCESS(1, "交易成功"),
    FAIL(2, "交易失败"),
    ;


    private Integer value;
    private String message;

    TransactionStatusEnum(Integer value, String message) {
        this.value = value;
        this.message = message;
    }

    @Override
    public Integer getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }
}
