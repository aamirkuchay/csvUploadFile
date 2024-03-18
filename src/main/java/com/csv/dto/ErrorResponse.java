package com.csv.dto;

import lombok.Data;


@Data
public class ErrorResponse extends UploadResponse {
    private String message;
    private int statusCode;
    public ErrorResponse(String message) {
        this.message = message;
    }

    public ErrorResponse(String message, int statusCode) {
        this.message = message;
        this.statusCode = statusCode;
    }



}
