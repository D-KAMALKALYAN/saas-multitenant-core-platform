package com.saasplatform.common.exception;

public class ApiKeyNotFoundException extends  RuntimeException{

    public ApiKeyNotFoundException(String message){
        super(message);
    }
}
