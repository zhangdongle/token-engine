package com.mchain.tokenengine.utils.eth.exception;

public class AddressMismatchException extends RuntimeException {
    private String message;

    public AddressMismatchException(String message) {
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
