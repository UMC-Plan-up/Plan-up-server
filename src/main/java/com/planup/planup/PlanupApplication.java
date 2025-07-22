package com.planup.planup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class PlanupApplication {
	public static void main(String[] args) {
		SpringApplication.run(PlanupApplication.class, args);
	}
}
