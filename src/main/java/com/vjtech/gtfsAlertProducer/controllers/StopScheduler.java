package com.vjtech.gtfsAlertProducer.controllers;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vjtech.gtfsAlertProducer.GtfsAlertProducerApplication;
import com.vjtech.gtfsAlertProducer.services.session.ApplicationBean;

@RestController
public class StopScheduler {
	
	private static final Logger log = LoggerFactory.getLogger(StopScheduler.class);

	@Autowired
	ApplicationBean sessionDataSource;

	@GetMapping("/stopScheduler")
	public String stop() {
		sessionDataSource.getTaskScheduler().cancel(true);

		log.info("Scheduler stopped");

		Date data = new Date();
		
		return "<html>\n" + "<header><title>Stato scheduler:</title></header>\n" + "<body>\n" + "Arrestato, data:\n"+ data.toString() + "</body>\n" + "</html>";

	}
}
