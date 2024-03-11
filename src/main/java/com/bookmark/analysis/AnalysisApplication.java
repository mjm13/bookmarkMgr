package com.bookmark.analysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class AnalysisApplication {
	public static void main(String[] args) {
		SpringApplication.run(AnalysisApplication.class, args);
	}

}
