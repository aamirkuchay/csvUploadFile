package com.csv.service;

import com.csv.entity.File;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public interface CsvEntryService {
    void save(MultipartFile file, File f) throws IOException;
    ByteArrayInputStream getDataByFile(File file);
}
