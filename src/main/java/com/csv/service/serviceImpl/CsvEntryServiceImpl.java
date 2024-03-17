package com.csv.service.serviceImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;


import com.csv.service.CsvEntryService;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.csv.entity.CsvEntry;
import com.csv.entity.File;
import com.csv.helper.ExcelHelper;
import com.csv.repository.CsvEntryRepository;
import com.csv.repository.FileRepository;

@Service
public class CsvEntryServiceImpl implements CsvEntryService {
	
	@Autowired
	CsvEntryRepository csvEntryRepository;
	@Autowired
	FileRepository fileRepository;

    /**
     * Override method to save a multipart file (likely an Excel file) and its related information.
     *
     * @param file The uploaded MultipartFile object representing the Excel file.
     * @param f A File object containing additional information about the file (purpose needs clarification).
     * @throws ServiceException Thrown when the file format is invalid.
     * @throws IOException Thrown for any issues during file processing.
     */
   @Override
    public void save(MultipartFile file, File f) throws ServiceException, IOException {
        // Validate file format
        if (!ExcelHelper.checkExcelFormat(file)) {
            throw new ServiceException("Invalid Excel file format. Please upload a valid .xls or .xlsx file.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            // Convert the Excel file to a list of CsvEntry objects for further processing.
            List<CsvEntry> csvList = ExcelHelper.convertExcelToCsv(inputStream, f);
            // Save CSV entries to the database
            csvEntryRepository.saveAll(csvList);
            // Mark file processing as completed
            f.setProcessing(false);
            // Save file metadata
            fileRepository.save(f);
        } catch (IOException e) {
            throw new ServiceException("Failed to save CSV entries: " + e.getMessage(), e);
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
    public ByteArrayInputStream getDataByFile(File file) throws ServiceException {
        // Check if file exists and entries are found
        List<CsvEntry> entries = csvEntryRepository.findByFile(file);
        if (file == null || entries.isEmpty()) {
            throw new ServiceException("No data found for the specified file.");
        }
        // Convert the CsvEntry list to a ByteArrayInputStream representing the CSV data.
        return ExcelHelper.dataToExcel(entries);
    }

}
