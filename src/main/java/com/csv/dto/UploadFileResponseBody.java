package com.csv.dto;

import lombok.Data;

@Data
public class UploadFileResponseBody {
    private String uniqueId;

    public UploadFileResponseBody(String uniqueId) {
        this.uniqueId = uniqueId;
    }

}
