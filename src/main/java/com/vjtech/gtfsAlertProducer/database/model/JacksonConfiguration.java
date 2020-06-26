package com.vjtech.gtfsAlertProducer.database.model;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfiguration {
	@Bean
	public JtsModule jtsModule() {
		return new JtsModule();
	}
}

