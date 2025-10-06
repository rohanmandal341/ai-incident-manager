package com.rohan.incidentmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IncidentManagerApplication {
	public static void main(String[] args) {
		SpringApplication.run(IncidentManagerApplication.class, args);
	}
}
