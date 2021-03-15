package com.mchain.tokenengine.utils.eth.exception;

public class AmountTooSmallException extends RuntimeException {
    private String message;

    public AmountTooSmallException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
