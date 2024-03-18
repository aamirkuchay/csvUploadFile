package com.csv.dto;

import lombok.Data;

@Data
public class UploadFileResponseBody{
    private String id;

    public UploadFileResponseBody(String id) {
        this.id = id;
    }

}
