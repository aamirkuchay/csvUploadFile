package com.csv.controller;

import com.csv.entity.CsvEntry;
import com.csv.entity.File;
import com.csv.helper.ExcelHelper;
import com.csv.repository.FileRepository;
import com.csv.dto.UploadFileResponseBody;
import com.csv.service.serviceImpl.CsvEntryServiceImpl;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;


@RunWith(MockitoJUnitRunner.class)
public class CsvControllerTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private CsvEntryServiceImpl csvService;
    @Mock
    private ExcelHelper excelHelper;

    @InjectMocks
    private   CsvController csvController;

    private final CsvController csvContt = new CsvController();

    public CsvControllerTest() {
    }


    @Test
    public void testUploadValidExcelFile() throws Exception {
        MultipartFile mockFile = new MockMultipartFile("test.xlsx", "test data".getBytes());

        ResponseEntity<?> response = csvContt.uploadFile(mockFile);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());

        UploadFileResponseBody responseBody = (UploadFileResponseBody) response.getBody();
        assertNotNull(responseBody.getUniqueId());
    }

    @Test
    void testUploadInvalidFile() throws IOException {

        byte[] fileContent = "invalid file content".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", fileContent);


        when(excelHelper.checkExcelFormat(any())).thenReturn(false);

        ResponseEntity<?> response = csvContt.uploadFile(mockFile);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }


    @Test
    void testConvertExcelToCsv() throws IOException {
        byte[] excelFileContent = createSampleExcelFile();

        InputStream inputStream = new ByteArrayInputStream(excelFileContent);
        File file = new File(/* mock file parameters */);

        List<CsvEntry> csvEntries = ExcelHelper.convertExcelToCsv(inputStream, file);
        assertNotNull(csvEntries);
    }

    private byte[] createSampleExcelFile() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sample Sheet");
            Row headerRow = sheet.createRow(0);
            Cell cell = headerRow.createCell(0);
            cell.setCellValue("Header Column");

            // Add sample data
            Row dataRow = sheet.createRow(1);
            cell = dataRow.createCell(0);
            cell.setCellValue("Data");

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        }
    }

}
