package com.trackery.trackerybackapiserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TrackeryBackApiServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrackeryBackApiServerApplication.class, args);
	}
}
