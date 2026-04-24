package com.saasplatform.common.exception;

public class InvalidRefreshTokenException extends RuntimeException{

    public InvalidRefreshTokenException(String message){
        super(message);
    }
}
