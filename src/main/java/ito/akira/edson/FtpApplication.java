package ito.akira.edson;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableIntegration
@EnableScheduling
public class FtpApplication {

	public static void main(String[] args) {
		SpringApplication.run(FtpApplication.class, args);
	}

}
