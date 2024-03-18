package com.csv;

import com.csv.entity.Role;
import com.csv.entity.User;
import com.csv.repository.UserRepository;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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
public class CsvuploadfileApplication implements CommandLineRunner {
	@Autowired
	private UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(CsvuploadfileApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		User adminAccount = userRepository.findByRole(Role.ADMIN);
		if (null == adminAccount) {
			User user = new User();
			user.setFirstName("Aamir");
			user.setLastName("Kuchay");
			user.setSecondName("admin");
			user.setEmail("a@gmail.com");
			user.setPassword(new BCryptPasswordEncoder().encode("admin"));
			user.setRole(Role.ADMIN);
			userRepository.save(user);
		}

	}
}
