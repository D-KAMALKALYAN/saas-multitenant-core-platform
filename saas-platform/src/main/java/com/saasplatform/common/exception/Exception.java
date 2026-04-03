package com.saasplatform.common.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;

public class Exception extends RuntimeException{

    public Exception(String message){
        super(message);
    }
}
