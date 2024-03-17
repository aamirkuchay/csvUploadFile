package com.csv;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "CSV Upload",
				description = "we are uploading the csv file",
				version = "v1",
				contact = @Contact(name = "Aamir kuchay", email = "a@gmail.com",url = "https://github.com/aamirkuchay/csvUploadFile"),
				license =@License(
						name = "Apache 2.O",
						url = "https://www.ht.com"
				)
		),
		externalDocs = @ExternalDocumentation(
				description = "CSV Upload Rest Api Documents",
				url = "https://github.com/aamirkuchay/csvUploadFile"
		)
)
public class CsvuploadfileApplication {

	public static void main(String[] args) {
		SpringApplication.run(CsvuploadfileApplication.class, args);
	}

}
