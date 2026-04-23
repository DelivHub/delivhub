package com.sparta.delivhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class DelivhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(DelivhubApplication.class, args);
	}

}
