package com.csv.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import com.csv.entity.CsvEntry;
import com.csv.entity.File;

public class ExcelHelper {

	public static boolean checkExcelFormat(MultipartFile file) throws IOException {
		// Check for null file
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File cannot be null or empty");
		}

		// Check content type and extension (for additional validation)
		String contentType = file.getContentType();
		String fileName = file.getOriginalFilename();

		if (contentType != null &&
				(contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
						contentType.equals("application/vnd.ms-excel"))) {
			return true;
		} else if (fileName != null && fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
			// Check for potential corrupted files by trying to open as Excel
			try {
				XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
				workbook.close();
				return true;
			} catch (IOException e) {
				return false;
			}
		} else {
			return false;
		}
	}


	public static List<CsvEntry> convertExcelToCsv(InputStream inputStream, File file) throws IOException {
		List<CsvEntry> entries = new ArrayList<>();
		XSSFWorkbook workbook = null;

		try {
			workbook = new XSSFWorkbook(inputStream);

			// Loop through sheets
			for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
				XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
				String sheetName = sheet.getSheetName();
				System.out.println("Sheet name: " + sheetName);

				// Loop through rows (skip header row)
				Iterator<Row> rowIterator = sheet.iterator();
				int rowIndex = 0;
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();

					if (rowIndex == 0) {
						rowIndex++;
						continue;
					}

					// Loop through cells in the row
					Iterator<Cell> cellIterator = row.iterator();
					int columnIndex = 0;
					CsvEntry entry = new CsvEntry();

					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();

						if (columnIndex == 0) {
							entry.setFinalColumn(cell.getStringCellValue());
						}
						columnIndex++;
					}
					entry.setFile(file);
					entries.add(entry);
				}
			}
		} catch (IOException e) {
			throw new IOException("Failed to read Excel file: " + e.getMessage(), e);
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return entries;
	}

	public static String[] HEADERS = { "finalColumn" };

	public static String SHEET_NAME = "csv_upload";

	public static ByteArrayInputStream dataToExcel(List<CsvEntry> entries) {
		Workbook workbook = null;
		ByteArrayOutputStream out = null;

		try {
			workbook = new XSSFWorkbook();
			out = new ByteArrayOutputStream();
			Sheet sheet = workbook.createSheet(SHEET_NAME);

			// Create header row
			Row headerRow = sheet.createRow(0);
			for (int headerIndex = 0; headerIndex < HEADERS.length; headerIndex++) {
				Cell headerCell = headerRow.createCell(headerIndex);
				headerCell.setCellValue(HEADERS[headerIndex]);
			}

			// Create data rows
			int dataRowIndex = 1;
			for (CsvEntry entry : entries) {
				Row dataRow = sheet.createRow(dataRowIndex++);
				// Assuming only one column
				Cell dataCell = dataRow.createCell(0);
				dataCell.setCellValue(entry.getFinalColumn());
			}

			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());

		} catch (IOException e) {
			throw new RuntimeException("Failed to create Excel file: " + e.getMessage(), e);
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					// Log the exception for debugging purposes
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					// Log the exception for debugging purposes
					e.printStackTrace();
				}
			}
		}
	}

}
