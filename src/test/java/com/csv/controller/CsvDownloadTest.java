package com.csv.controller;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.csv.entity.CsvEntry;
import com.csv.entity.File;
import com.csv.repository.CsvEntryRepository;
import com.csv.repository.FileRepository;
import com.csv.service.serviceImpl.CsvEntryServiceImpl;
import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.ByteArrayInputStream;
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

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(csvController).build();
    }
    public static final MediaType APPLICATION_MS_EXCEL = MediaType.parseMediaType("application/vnd.ms-excel");

    @Test
    public void testDownloadCsv_ValidFile() throws Exception {

        // Arrange
        String fileId = "c54fe9ba-d15e-413e-9e85-8098c4292f5c";
        File file = new File();
        file.setId(fileId);
        when(fileRepository.findById(fileId)).thenReturn(Optional.of(file));

        ByteArrayInputStream csvData = new ByteArrayInputStream("dummy csv data".getBytes());
        when(csvService.getDataByFile(file)).thenReturn(csvData);

        // Act
        byte[] expectedContent = "dummy csv data".getBytes();
        byte[] actualContent = mockMvc.perform(get("/api/download/" + fileId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=csv.xlsx"))
                .andExpect(content().contentType(APPLICATION_MS_EXCEL)) // Update to expected content type
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        // Assert
        assertArrayEquals(expectedContent, actualContent);
    }


    @Test
    public void testDownloadCsv_notFound() throws Exception {
        String fileId = "93ae6e4a-b7cb-4833-b857-f81324116fcd";
        Mockito.when(fileRepository.findById(fileId)).thenReturn(null);
        ResponseEntity<?> response = csvController.downloadCsv(fileId);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testGetDataByFile_noData() throws Exception {
        File mockFile = Mockito.mock(File.class);
        List<CsvEntry> entries = Collections.emptyList();
        Mockito.when(csvEntryRepository.findByFile(mockFile)).thenReturn(entries);
        assertThrows(ServiceException.class, () -> {
            csvService.getDataByFile(mockFile);
        });
    }
}
