package com.saasplatform.common.exception;

public class TenantAlreadyExistsException extends RuntimeException{
    public TenantAlreadyExistsException(String message){
        super(message);
    }
}
