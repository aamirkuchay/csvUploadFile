package com.csv.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "csv_entry")
public class CsvEntry {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	private String finalColumn;
	
	@ManyToOne
	@JoinColumn(name = "file_id")
	private File file;

	public CsvEntry(String data1) {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFinalColumn() {
		return finalColumn;
	}

	public void setFinalColumn(String finalColumn) {
		this.finalColumn = finalColumn;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public CsvEntry(Long id, String finalColumn, File file) {
		super();
		this.id = id;
		this.finalColumn = finalColumn;
		this.file = file;
	}

	public CsvEntry() {
		super();
	}
	
	

	
	
	

}
