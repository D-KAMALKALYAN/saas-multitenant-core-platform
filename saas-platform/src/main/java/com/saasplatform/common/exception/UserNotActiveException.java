package com.saasplatform.common.exception;

public class UserNotActiveException extends RuntimeException{

    public UserNotActiveException(String message){
        super(message);
    }
}
