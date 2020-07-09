package com.vjtech.gtfsAlertProducer.controllers;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vjtech.gtfsAlertProducer.MessageProducer;
import com.vjtech.gtfsAlertProducer.services.session.ApplicationBean;

@RestController
public class RestartScheduler {
	
	private static final Logger log = LoggerFactory.getLogger(StopScheduler.class);

	@Autowired
	ApplicationBean applicationBean;
	
	@Autowired
	MessageProducer messageProducer;

	@GetMapping(value="/restartScheduler",  produces = MediaType.TEXT_HTML_VALUE)
	public String restartScheduler(@RequestParam(value = "interval", required = false) Optional<Long> interval) {	
		try {
			applicationBean.restartScheduler(interval.orElse(applicationBean.getScheduler_interval()));
			log.info("Scheduler restarted");
			Date data = new Date();
			return "<html>\n" + "<header><title>Stato scheduler</title></header>\n" + "<body>\n" + "Avviato, data:\n"
					+ data.toString() + "</body>\n" + "</html>";

		} catch (InterruptedException e) {
			log.info("Could not restart scheduler");
			e.printStackTrace();
			return "<html></html>";
		}		
		

	}	
}
