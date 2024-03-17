package com.csv.controller;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.csv.entity.File;
import com.csv.repository.FileRepository;
import com.csv.service.CsvEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
@ExtendWith(MockitoExtension.class)
public class CsvDownloadTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private CsvEntryService csvService;

    @InjectMocks
    private CsvController csvController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(csvController).build();
    }
    public static final MediaType APPLICATION_MS_EXCEL = MediaType.parseMediaType("application/vnd.ms-excel");

    @Test
    public void testDownloadCsv_ValidFile_NotProcessing() throws Exception {

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
}
