package com.csv.controller;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import com.csv.exception.ResourceNotFoundException;
import com.csv.response.ErrorResponse;
import com.csv.response.ResponseBody;
import com.csv.service.CsvEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import com.csv.entity.File;
import com.csv.helper.ExcelHelper;
import com.csv.repository.FileRepository;
@Tag(name = "Rest Api of Csv File Upload", description = "We are uploading the csv file and get that file by id")
@RestController
@RequestMapping("/api/")
public class CsvController {
	
	@Autowired
	private FileRepository fileRepository;

    @Autowired
    private CsvEntryService csvService;


    @Operation(
            summary = "Upload the csv file",
            description = "you have you choose the csv file"
    )
    @ApiResponse(responseCode = "201", description = "HTTP Status Created")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (ExcelHelper.checkExcelFormat(file)) {
                String uniqueId = UUID.randomUUID().toString();
                File f = new File(uniqueId, true);

                fileRepository.save(f);
                csvService.save(file, f);
                return ResponseEntity.accepted().body(new ResponseBody(uniqueId));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Please upload an Excel file.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to upload file. Please try again later."));
        }
    }


    /**
     * GET endpoint to download a CSV file associated with a specific file ID.
     *
     * @param id The ID of the file to download the CSV for.
     * @return ResponseEntity object containing the CSV data or error message.
     * @throws ResourceNotFoundException Thrown if the file with the provided ID is not found.
     */
    @GetMapping("download/{id}")
    public ResponseEntity<?> downloadCsv(@PathVariable String id) {
        try {
            // Find the file by ID from the repository.
            File file = fileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

            // Check if the file is still being processed (upload not complete).
            if (file.isProcessing()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Upload is still being processed");
            }

            // Set a default filename with .xlsx extension
            String filename = "csv.xlsx";
            // Get the CSV data for this file.
            ByteArrayInputStream csvData = csvService.getDataByFile(file);
            // Prepare the response for downloading the CSV file.
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(new InputStreamResource(csvData));

        } catch (ResourceNotFoundException e) {
            // Handle case where the file is not found.
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Handle unexpected errors.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }



}
