package com.csv.exception;

public class UploadNotFoundException extends RuntimeException{
    public UploadNotFoundException(String message) {
        super(message);
    }
}
