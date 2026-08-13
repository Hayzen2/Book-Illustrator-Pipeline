package com.example.BookIllustrator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BookIllustratorApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookIllustratorApplication.class, args);
	}

}
