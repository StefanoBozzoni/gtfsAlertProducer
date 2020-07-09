package com.vjtech.gtfsAlertProducer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.vjtech.gtfsAlertProducer.services.model.AccessTokenResponse;
import com.vjtech.gtfsAlertProducer.services.session.ApplicationBean;
import com.vjtech.gtfsAlertProducer.services.session.SessionService;

@SpringBootApplication()
public class GtfsAlertProducerApplication {
	
	@Autowired
	MessageProducer messageProducer;

	private static final Logger log = LoggerFactory.getLogger(GtfsAlertProducerApplication.class);

	@Autowired
	ApplicationBean applicationBean;

	// @Autowired
	// ScheduledFuture<?> mainTaskScheduler;

	public static void main(String[] args) {
		log.info("Starting");
		SpringApplication.run(GtfsAlertProducerApplication.class, args);
	}

	@Bean
	AccessTokenResponse getTokenResponse(SessionService service) throws IOException {
		return service.getAccessToken();
	}

	@Bean
	public CommandLineRunner startScheduler(AccessTokenResponse tokenResponse) {

		return (args) -> {
			log.info("Avvio scheduler...");

			applicationBean.setAccessToken(tokenResponse.accessToken);
			applicationBean.setRefreshToken(tokenResponse.refreshToken);
			applicationBean.startScheduler(5);

			log.info("************ ACCESS TOKEN *************");
			log.info(tokenResponse.accessToken);
			log.info("***************************************");

			log.info("Scheduler avviato");

		};
	}


	// @EnableScheduling
	// @Scheduled(fixedRate = 5000)

	/*
	 * ScheduledExecutorService scheduledExecutorService =
	 * Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
	 * then create a scheduled task:
	 * 
	 * ScheduledFuture<?> task = scheduledExecutorService.scheduleAtFixedRate( () ->
	 * System.out.println("some task"), 0, 30, TimeUnit.SECONDS); and when you want
	 * to cancel the task, do the following:
	 * 
	 * task.cancel(true);
	 */


}
