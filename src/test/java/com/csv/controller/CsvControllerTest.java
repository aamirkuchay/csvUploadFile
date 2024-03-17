package com.csv.controller;

import com.csv.entity.CsvEntry;
import com.csv.entity.File;
import com.csv.exception.ResourceNotFoundException;
import com.csv.repository.FileRepository;
import com.csv.response.ResponseBody;
import com.csv.service.CsvEntryService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;


@RunWith(MockitoJUnitRunner.class)
public class CsvControllerTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private CsvEntryService csvService;

    @InjectMocks
    private CsvController uploadController;

    @Test
    public void testUploadValidExcelFile() throws IOException {

        MultipartFile mockMultipartFile = mock(MultipartFile.class);
          when(mockMultipartFile.getContentType()).thenReturn("application/vnd.ms-excel");
         when(mockMultipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("Valid Excel Content".getBytes()));
        File mockFile = mock(File.class);

        when(fileRepository.save(mockFile)).thenReturn(mockFile);


        ResponseEntity<?> response = uploadController.uploadFile(mockMultipartFile);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
         assertTrue(response.getBody() instanceof ResponseBody);
    }

}
