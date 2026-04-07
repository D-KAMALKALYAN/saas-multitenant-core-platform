package com.saasplatform.common.exception;

public class TenantNotActiveException extends RuntimeException{

    public TenantNotActiveException(String message){
        super(message);
    }

}
