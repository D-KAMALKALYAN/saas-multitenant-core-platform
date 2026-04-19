package com.saasplatform.common.exception;

public class InvalidCredentialsException extends RuntimeException{

    public InvalidCredentialsException(String message){
        super(
                message
        );
    }
}
