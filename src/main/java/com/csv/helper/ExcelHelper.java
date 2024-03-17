package com.csv.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
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



	/**
	 * Static method to convert an Excel file (provided as an InputStream) to a list of CsvEntry objects.
	 *
	 * @param inputStream An InputStream object representing the content of the Excel file.
	 * @param file A File object (its purpose in this context needs clarification).
	 * @return A List containing CsvEntry objects parsed from the Excel data.
	 * @throws IOException Thrown for any issues during Excel file processing.
	 */
	public static List<CsvEntry> convertExcelToCsv(InputStream inputStream, File file) throws IOException {
//		Creates an empty list to store CSV entries.
		List<CsvEntry> entries = new ArrayList<>();
//		Creates an XSSFWorkbook object to read the Excel file.
		XSSFWorkbook workbook = null;

		try {
			// Open Excel workbook
			workbook = new XSSFWorkbook(inputStream);

			// Loop through sheets
			for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
				// Get current sheet
				XSSFSheet sheet = workbook.getSheetAt(sheetIndex);
				String sheetName = sheet.getSheetName();
				System.out.println("Sheet name: " + sheetName);

				// Loop through rows, skipping the header row (assuming the first row contains column names).
				Iterator<Row> rowIterator = sheet.iterator();
				int rowIndex = 0;
				while (rowIterator.hasNext()) {
					Row row = rowIterator.next();

					if (rowIndex == 0) {
						rowIndex++;
						// Skip header row
						continue;
					}

					// Loop through cells in the current row.
					Iterator<Cell> cellIterator = row.iterator();
					int columnIndex = 0;
					// Create a new object to store data from the current row.
					CsvEntry entry = new CsvEntry();

					while (cellIterator.hasNext()) {
						Cell cell = cellIterator.next();
						switch (columnIndex) {
							case 0:
								// Currently processing only the first column. Modify this logic as needed.
								if (cell.getCellType() == CellType.BLANK || cell.getStringCellValue().trim().isEmpty()) {
									entry.setFinalColumn("Data is missing");
								}else {
									entry.setFinalColumn(cell.getStringCellValue());
								}
								// Ignore other columns for now.
								break;
							default:
								break;
						}
						columnIndex++;

					}
					// Set the file associated with the CSV entry
					entry.setFile(file);
					// Add CSV entry to the list
					entries.add(entry);
				}
			}
		} catch (IOException e) {
			throw new IOException("Failed to read Excel file: " + e.getMessage(), e);
		} finally {
			if (workbook != null) {
				try {
					// Close workbook
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// Return list of CSV entries
		return entries;
	}

	public static String[] HEADERS = { "finalColumn" };

	public static String SHEET_NAME = "csv_upload";


	/**
	 * Utility method to convert a list of CsvEntry objects to a ByteArrayInputStream containing Excel data.
	 *
	 * @param entries The list of CsvEntry objects representing the data to be converted.
	 * @return ByteArrayInputStream containing the Excel data in bytes.
	 * @throws RuntimeException Thrown for any issues during Excel file creation.
	 */
	public static ByteArrayInputStream dataToExcel(List<CsvEntry> entries) {
		Workbook workbook = null;
		ByteArrayOutputStream out = null;

		try {
			// Create a new workbook and output stream.
			workbook = new XSSFWorkbook();
			out = new ByteArrayOutputStream();
			// Define a constant for the sheet name
			Sheet sheet = workbook.createSheet(SHEET_NAME);

			// Create header row
			Row headerRow = sheet.createRow(0);
			for (int headerIndex = 0; headerIndex < HEADERS.length; headerIndex++) {
				Cell headerCell = headerRow.createCell(headerIndex);
				headerCell.setCellValue(HEADERS[headerIndex]);
			}

			// Create data rows, assuming only one column from the CsvEntry objects.
			int dataRowIndex = 1;
			for (CsvEntry entry : entries) {
				Row dataRow = sheet.createRow(dataRowIndex++);
				// Assuming only one column
				Cell dataCell = dataRow.createCell(0);
				dataCell.setCellValue(entry.getFinalColumn());
			}
			// Write the workbook to the output stream and convert it to a ByteArrayInputStream.
			workbook.write(out);
			return new ByteArrayInputStream(out.toByteArray());

		} catch (IOException e) {
			throw new RuntimeException("Failed to create Excel file: " + e.getMessage(), e);
		} finally {
			// Close the workbook and output stream
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
