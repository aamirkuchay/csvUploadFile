package com.csv.controller;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import com.csv.exception.ResourceNotFoundException;
import com.csv.response.ErrorResponse;
import com.csv.response.ResponseBody;
import com.csv.service.CsvEntryService;
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

@RestController
@RequestMapping("/api/")
public class CsvController {
	
	@Autowired
	private FileRepository fileRepository;

    @Autowired
    private CsvEntryService csvService;

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
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Please upload an Excel file.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Failed to upload file. Please try again later."));
        }
    }


    @GetMapping("download/{id}")
    public ResponseEntity<?> downloadCsv(@PathVariable String id) {
        try {
            File file = fileRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

            if (file.isProcessing()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Upload is still being processed");
            }

            String filename = "csv.xlsx";
            ByteArrayInputStream csvData = csvService.getDataByFile(file);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(new InputStreamResource(csvData));

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }



}
