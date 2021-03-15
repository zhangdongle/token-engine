package com.mchain.tokenengine.enums;

import com.baomidou.mybatisplus.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CoinTypeEnum implements IEnum {
    MAIN(0, "主币"),
    SUBSCRIBE(1, "认购币");

    private Integer value;
    private String message;

    CoinTypeEnum(Integer value, String message) {
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
