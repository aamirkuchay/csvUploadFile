package com.csv.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


import org.hibernate.service.spi.ServiceException;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.csv.entity.CsvEntry;
import com.csv.entity.File;
import com.csv.helper.ExcelHelper;
import com.csv.repository.CsvEntryRepository;
import com.csv.repository.FileRepository;

@Service

public class CsvEntryService {
	
	@Autowired
	CsvEntryRepository csvEntryRepository;
	@Autowired
	FileRepository fileRepository;


    public void save(MultipartFile file, File f) throws ServiceException, IOException {
        // Validate file format
        if (!ExcelHelper.checkExcelFormat(file)) {
            throw new ServiceException("Invalid Excel file format. Please upload a valid .xls or .xlsx file.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            // Convert Excel to CSV and save entries
            List<CsvEntry> csvList = ExcelHelper.convertExcelToCsv(inputStream, f);
            csvEntryRepository.saveAll(csvList);
            f.setProcessing(false);
            fileRepository.save(f);
        } catch (IOException e) {
            throw new ServiceException("Failed to save CSV entries: " + e.getMessage(), e);
        }
    }

    public ByteArrayInputStream getDataByFile(File file) throws ServiceException {
        // Check if file exists and entries are found
        List<CsvEntry> entries = csvEntryRepository.findByFile(file);
        if (file == null || entries.isEmpty()) {
            throw new ServiceException("No data found for the specified file.");
        }
        return ExcelHelper.dataToExcel(entries);
    }

}
