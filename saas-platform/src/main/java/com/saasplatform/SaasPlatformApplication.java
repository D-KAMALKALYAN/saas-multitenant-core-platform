package com.saasplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
public class SaasPlatformApplication {
	public static void main(String[] args) {
		SpringApplication.run(SaasPlatformApplication.class, args);
	}
}
