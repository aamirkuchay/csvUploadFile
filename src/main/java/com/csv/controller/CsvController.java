package com.csv.controller;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.csv.exception.ResourceNotFoundException;
import com.csv.dto.ErrorResponse;
import com.csv.dto.UploadFileResponseBody;
import com.csv.service.CsvEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csv.entity.File;
import com.csv.helper.ExcelHelper;
import com.csv.repository.FileRepository;
@Tag(name = "Rest Api of Csv File Upload", description = "We are uploading the csv file and get that file by id")
@RestController
@RequestMapping("/api/v1/user")
public class CsvController {
	
	@Autowired
	private FileRepository fileRepository;

    @Autowired
    private CsvEntryService csvService;

    Logger logger = LoggerFactory.getLogger(CsvController.class);


    @Operation(
            summary = "Upload the csv file",
            description = "you have you choose the csv file"
    )
    @ApiResponse(responseCode = "201", description = "HTTP Status Created")

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        String[] uniqueId = new String[1];
        try {
            CompletableFuture<String> future = csvService.uploadAndProcessFile(file);

            // Handle the case when the upload and processing operation is completed
            future.thenAccept(id -> {
                uniqueId[0] = id;
                // Do any post-processing here if needed
                System.out.println("File processing completed asynchronously. Unique ID: " + uniqueId);
            });

            UploadFileResponseBody responseBody = new UploadFileResponseBody(uniqueId[0]);
            return ResponseEntity
                    .accepted()
                    .body(responseBody);
        } catch (ServiceException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    /**
     * GET endpoint to download a CSV file associated with a specific file ID.
     *
     * @param id The ID of the file to download the CSV for.
     * @return ResponseEntity object containing the CSV data or error message.
     * @throws ResourceNotFoundException Thrown if the file with the provided ID is not found.
     */

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadCsv(@PathVariable String id) {
        try {
            ByteArrayInputStream csvData = csvService.downloadCsvFile(id);

            // Prepare the response for downloading the CSV file.
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=csv.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(new InputStreamResource(csvData));
        } catch (ResourceNotFoundException e) {
            // Handle case where the file is not found (expected behavior)
            // Explicitly return 404
            return ResponseEntity.notFound().build();
        } catch (ServiceException e) {
            // Handle other service-related exceptions
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            // Handle unexpected exceptions more gracefully
            // Log the error details
            logger.error("Internal server error while downloading file (ID: " + id + ")", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }


}
