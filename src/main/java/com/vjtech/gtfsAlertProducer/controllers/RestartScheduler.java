package com.vjtech.gtfsAlertProducer.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vjtech.gtfsAlertProducer.GtfsAlertProducerApplication;
import com.vjtech.gtfsAlertProducer.services.session.SessionInMemoryDatasource;

@RestController
public class RestartScheduler {
	
	private static final Logger log = LoggerFactory.getLogger(StopScheduler.class);

	@Autowired
	SessionInMemoryDatasource sessionDataSource;

	@GetMapping("/restartScheduler")
	public void restartScheduler() {
		sessionDataSource.getTaskScheduler().cancel(true);		
		//TODO: still to implement...
		log.info("Scheduler restarted");
	}
	
}
