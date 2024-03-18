package com.csv.controller;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.csv.entity.CsvEntry;
import com.csv.entity.File;
import com.csv.helper.ExcelHelper;
import com.csv.repository.CsvEntryRepository;
import com.csv.repository.FileRepository;
import com.csv.service.serviceImpl.CsvEntryServiceImpl;
import org.hibernate.service.spi.ServiceException;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;


@ExtendWith(MockitoExtension.class)
public class CsvDownloadTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    CsvEntryRepository csvEntryRepository;

    @Mock
    private CsvEntryServiceImpl csvService;

    @InjectMocks
    private CsvController csvController;

    private MockMvc mockMvc;

//    @BeforeEach
//    public void setUp() {
//        mockMvc = MockMvcBuilders.standaloneSetup(csvController).build();
//    }

    @BeforeEach
    public void setUp() {
        try (MockedStatic<ExcelHelper> mocked = mockStatic(ExcelHelper.class)) {
            // Mock ExcelHelper.dataToExcel method
            mocked.when(() -> ExcelHelper.dataToExcel(Collections.emptyList()))
                    .thenReturn(new ByteArrayInputStream("fake excel data".getBytes()));
        }
    }
    public static final MediaType APPLICATION_MS_EXCEL = MediaType.parseMediaType("application/vnd.ms-excel");

    @Test
    public void testDownloadCsv_Success() throws Exception {
        // Mock file repository and csv entry repository
        File file = new File();
        file.setId("1");
        when(fileRepository.findById("1")).thenReturn(Optional.of(file));

        List<CsvEntry> entries = new ArrayList<>();
        entries.add(new CsvEntry("data"));
        when(csvEntryRepository.findByFile(file)).thenReturn(entries);

        // Test the API
        ResponseEntity<?> response = csvController.downloadCsv("1");

        // Verify the response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ByteArrayInputStream);
    }


    @Test
    public void testDownloadCsv_notFound() throws Exception {
        String fileId = "93ae6e4a-b7cb-4833-b857-f81324116fcd";
        Mockito.when(fileRepository.findById(fileId)).thenReturn(null);
        ResponseEntity<?> response = csvController.downloadCsv(fileId);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDownloadCsv_UploadInProgress() throws Exception {
        // Mock file repository to return a file with isProcessing() true
        File file = new File();
        file.setId("1");
        file.setProcessing(true);
        when(fileRepository.findById("1")).thenReturn(Optional.of(file));

        // Test the API
        ResponseEntity<?> response = csvController.downloadCsv("1");

        // Verify the response
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void testDownloadCsv_NoDataFound() throws Exception {
        // Mock file repository and csv entry repository to return empty lists
        File file = new File();
        file.setId("1");
        when(fileRepository.findById("1")).thenReturn(Optional.of(file));
        when(csvEntryRepository.findByFile(file)).thenReturn(Collections.emptyList());

        // Test the API
        ResponseEntity<?> response = csvController.downloadCsv("1");

        // Verify the response
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
    }


    @Test
    public void testDownloadCsv_InternalServerError() throws Exception {
        // Mock file repository to throw an exception
        when(fileRepository.findById("1")).thenThrow(new RuntimeException("Test exception"));

        // Test the API
        ResponseEntity<?> response = csvController.downloadCsv("1");

        // Verify the response
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
