package com.lemondouble.lemonToolbox;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LemonToolboxApplication {

	public static void main(String[] args) {
		SpringApplication.run(LemonToolboxApplication.class, args);

	}

	// 초기화시
	// insert into service_count values('LEARNME', 0);
}
