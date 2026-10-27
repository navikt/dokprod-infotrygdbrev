package no.nav.dokprod.infotrygdbrev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DokprodInfotrygdbrevApplication {

	static void main(String[] args) {
		SpringApplication.run(DokprodInfotrygdbrevApplication.class, args);
	}

}
