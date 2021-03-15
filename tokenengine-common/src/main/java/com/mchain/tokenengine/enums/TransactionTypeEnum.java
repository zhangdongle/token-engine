package com.mchain.tokenengine.enums;

import com.baomidou.mybatisplus.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TransactionTypeEnum implements IEnum {
    USER(1, "用户转入"),
    COLD(2, "转入冷钱包"),
    EXTRACT(3, "提币转出"),
    REFUND(4, "退币")
    ;

    private Integer value;
    private String message;

    TransactionTypeEnum(Integer value, String message) {
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
