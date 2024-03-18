package com.csv.service;

import com.csv.entity.File;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface CsvEntryService {

    CompletableFuture<String> uploadAndProcessFile(MultipartFile file);

    ByteArrayInputStream downloadCsvFile(String id);
}
