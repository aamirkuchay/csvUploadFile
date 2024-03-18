package com.csv.service.serviceImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


import com.csv.exception.ResourceNotFoundException;
import com.csv.service.CsvEntryService;
import lombok.extern.log4j.Log4j2;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.csv.entity.CsvEntry;
import com.csv.entity.File;
import com.csv.helper.ExcelHelper;
import com.csv.repository.CsvEntryRepository;
import com.csv.repository.FileRepository;

@Service
@Log4j2
public class CsvEntryServiceImpl implements CsvEntryService {
	
	@Autowired
	CsvEntryRepository csvEntryRepository;
	@Autowired
	FileRepository fileRepository;

    /**
     * Override method to save a multipart file (likely an Excel file) and its related information.
     *
     * @param file The uploaded MultipartFile object representing the Excel file.
     * @param f    A File object containing additional information about the file (purpose needs clarification).
     * @return
     * @throws ServiceException Thrown when the file format is invalid.
     * @throws IOException      Thrown for any issues during file processing.
     */

    @Async
    public CompletableFuture<String> uploadAndProcessFile(MultipartFile file) {
        try {
            if (!ExcelHelper.checkExcelFormat(file)) {
                throw new ServiceException("Please upload an CSV file.");
            }

            String uniqueId = UUID.randomUUID().toString();
            File f = new File(uniqueId, true);
            fileRepository.save(f);
            List<CsvEntry> csvList = ExcelHelper.convertExcelToCsv(file.getInputStream(), f);
            log.info("Check csvList {} for id:{}",csvList,f);
            csvEntryRepository.saveAll(csvList);
            f.setProcessing(false);
            fileRepository.save(f);

            return CompletableFuture.completedFuture(uniqueId);
        } catch (IOException e) {
            throw new ServiceException("Failed to upload and process file: " + e.getMessage(), e);
        }
    }

    /**
     * Service method to retrieve CSV data associated with a specific file.
     *
     * @param file The File object representing the file for which CSV data is needed.
     * @return ByteArrayInputStream containing the CSV data in bytes.
     * @throws ServiceException Thrown if no data is found for the specified file.
     */

    @Override
    public ByteArrayInputStream downloadCsvFile(String id) throws ResourceNotFoundException, ServiceException {
        // Find the file by ID from the repository.
        File file = fileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

        // Check if the file is still being processed (upload not complete).
        if (file.isProcessing()) {
            throw new ServiceException("Upload is still being processed");
        }

        // Set a default filename with .xlsx extension
        String filename = "csv.xlsx";
        // Get the CSV data for this file.
        List<CsvEntry> entries = csvEntryRepository.findByFile(file);

        // Check if entries are found
        if (entries.isEmpty()) {
            throw new ServiceException("No data found for the specified file.");
        }

        // Convert the CsvEntry list to a ByteArrayInputStream representing the CSV data.
        return ExcelHelper.dataToExcel(entries);
    }
}
