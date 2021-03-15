package com.mchain.tokenengine.enums;

import com.baomidou.mybatisplus.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TransactionOpenStatusEnum implements IEnum {
    OPEN(1, "已操作"),
    UNOPENED(0, "未操作"),
    ;

    private Integer value;
    private String message;

    TransactionOpenStatusEnum(Integer value, String message) {
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
