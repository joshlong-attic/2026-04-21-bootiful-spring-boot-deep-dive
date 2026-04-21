package com.example.modulith;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.modulith.events.IncompleteEventPublications;

@SpringBootApplication
public class ModulithApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModulithApplication.class, args);
	}

//	@Bean
//	ApplicationRunner runner (IncompleteEventPublications eventPublications) {
//		return a -> eventPublications.resubmitIncompletePublications(
//				ep -> ep.
//		);
//	}


}

