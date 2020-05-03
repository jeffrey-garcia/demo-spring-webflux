package com.jeffrey.example.demoapp;

import com.jeffrey.example.demolib.shutdown.annotation.EnableGracefulShutdown;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableGracefulShutdown
@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}