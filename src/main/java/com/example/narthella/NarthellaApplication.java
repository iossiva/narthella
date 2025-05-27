package com.example.narthella;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(scanBasePackages = { "com.example.narthella" })
public class NarthellaApplication {
	public static void main(String[] args) {
		SpringApplication.run(NarthellaApplication.class, args);
	}

}
