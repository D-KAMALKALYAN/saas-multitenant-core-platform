package com.saasplatform.common.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StandardApiResponse <T>{
    private boolean success;
    private String message;
    private T data;

    private LocalDateTime timestamp;

    public static <T> StandardApiResponse<T> success(String message, T data){
        return new StandardApiResponse<>(true , message , data, LocalDateTime.now());
    }

    //Optional Success without data
    public static <T> StandardApiResponse<T> success(String message){
        return new StandardApiResponse<>(true , message , null , LocalDateTime.now());
    }

    //Optional Failure with data
    public static <T> StandardApiResponse<T> failure(String message, T data){
        return new StandardApiResponse<>(false , message , data , LocalDateTime.now());
    }

    public static <T> StandardApiResponse<T> failure(String message){
        return new StandardApiResponse<>(false , message , null , LocalDateTime.now());
    }
}
