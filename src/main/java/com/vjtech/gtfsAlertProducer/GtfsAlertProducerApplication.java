package com.vjtech.gtfsAlertProducer;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.vjtech.gtfsAlertProducer.database.model.Agency;
import com.vjtech.gtfsAlertProducer.repository.AgencyRepository;
import com.vjtech.gtfsAlertProducer.services.JobZoneResponse;
import com.vjtech.gtfsAlertProducer.services.model.AccessTokenResponse;
import com.vjtech.gtfsAlertProducer.services.model.JobZoneRequest;
import com.vjtech.gtfsAlertProducer.services.session.GtfsService;
import com.vjtech.gtfsAlertProducer.services.session.ISessionService;
import com.vjtech.gtfsAlertProducer.services.session.SessionInMemoryDatasource;
import com.vjtech.gtfsAlertProducer.services.session.SessionService;

@SpringBootApplication()
public class GtfsAlertProducerApplication {

	private static final Logger log = LoggerFactory.getLogger(GtfsAlertProducerApplication.class);
	
	@Value("https://romamobilita.it/sites/default/files/rome_rtgtfs_service_alerts_feed.pb")
	String Alert_url_address;

	@Autowired
	ScheduledFuture<?> myschedultedTask;

	public static void main(String[] args) {
		log.info("Starting");
		SpringApplication.run(GtfsAlertProducerApplication.class, args);
	}

	// @Autowired
	// private RetrofitService service;

	@Autowired
	private GtfsService gtfsService;

	@Autowired
	AgencyRepository agencyRepository;

	@Autowired
	SessionInMemoryDatasource sessionDataSource;

	@Bean
	AccessTokenResponse getTokenResponse(SessionService service) throws IOException {
		return service.getAccessToken();
	}

	@Bean
	public CommandLineRunner demo(AccessTokenResponse tokenResponse) {

		return (args) -> {
			log.info("Inizio demo");

			getJson("agency.csv");
			getJson("routes.csv");

			// Response service.getAccessToken()
			// AccessTokenResponse tokenResponse = service.getAccessToken();
			sessionDataSource.setAccessToken(tokenResponse.accessToken);
			sessionDataSource.setRefreshToken(tokenResponse.refreshToken);

			log.info("************ ACCESS TOKEN *************");
			log.info(tokenResponse.accessToken);
			log.info("***************************************");

			List<Agency> agencyList = agencyRepository.findAll();
			agencyList.forEach((Agency el) -> {
				log.info(el.toString());
			});

			/*
			 * Date date = new Date(); LocalDate ld = date.toInstant()
			 * .atZone(ZoneId.systemDefault()) .toLocalDate();
			 */


			/*
			 * extracting points from database List<Object[]> points_list =
			 * agencyRepository.findPointsByRouteId(249); points_list.forEach((Object obj)
			 * -> { Object[] arr = (Object[]) obj; BigDecimal lat = new
			 * BigDecimal(arr[0].toString()); BigDecimal lon = new
			 * BigDecimal(arr[1].toString()); log.info(lat.toString());
			 * log.info(lon.toString()); });
			 */

			log.info("Fine demo");
		};
	}

	@Bean
	ScheduledFuture<?> startBackGroundThread() {
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
		ScheduledFuture<?> task = scheduledExecutorService.scheduleAtFixedRate(() -> MyTaskToRun(), 0, 30,
				TimeUnit.SECONDS);
		return task;
	}
	

	void InvioMessagioFromAlert(Alert alert) throws IOException {
		
		long timeStampStart = alert.getActivePeriod(0).getStart();
		long timeStampEnd = alert.getActivePeriod(0).getEnd();		
		
		/*
		timeStampStart.toInstant()
        .atZone(ZoneId.of("UTC"))
        .toLocalDate();
        */
		
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
				Locale.ENGLISH);
		String formattedDateStart = outputFormatter.format(new Timestamp(timeStampStart).toLocalDateTime());
		String formattedDateEnd = outputFormatter.format(new Timestamp(timeStampEnd).toLocalDateTime());
		
		@SuppressWarnings("serial")
		JobZoneRequest zoneRequest = new JobZoneRequest() {
			{
				body = HtmlEscape.unescapeHtml(alert.getDescriptionText().getTranslation(0).getText());
				startDate = formattedDateStart; // esempio : "2019-09-05T10:31:00+02:00";
				endDate = formattedDateEnd;
				categoryCode = "CTG21";
				multiArea = "false";
				subject = HtmlEscape.unescapeHtml(alert.getHeaderText().getTranslation(0).getText());
				transitEnabled = 0;
				url = "";
				areaCode = Arrays.asList("AR00031");
				mail = "romamobilita@whereapp.it";
			}
		}; // create it as a bean ?

		JobZoneResponse jzr = gtfsService.createJobZone(zoneRequest);
		log.info("************STATUS:");
		log.info(jzr.status);		
	}

	void MyTaskToRun() {

		try {
			//Read the new alert file and cycle over the messages...
			
			log.info("sono schedulato...");
			URL url = new URL(Alert_url_address);
			FeedMessage feed = FeedMessage.parseFrom(url.openStream());
			Alert alert;
			for (FeedEntity entity : feed.getEntityList()) {
				log.info(entity.getTripUpdate().toString());
				alert = entity.getAlert();
				if (!entity.getIsDeleted()) {
					log.info(new Date(alert.getActivePeriod(0).getStart()).toString());
					log.info(new Date(alert.getActivePeriod(0).getEnd()).toString());
					log.info(alert.getInformedEntity(0).getRouteId().toString());
					log.info(HtmlEscape.unescapeHtml(alert.getHeaderText().getTranslation(0).getText()));
					log.info(HtmlEscape.unescapeHtml(alert.getDescriptionText().getTranslation(0).getText()));
					log.info(alert.getInformedEntity(0).getRouteId());
					log.info("--------------");
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage());
		}

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

	public void getJson(String filename) throws Exception {
		File input = new ClassPathResource(filename).getFile();
		// File input = new File("./src/main/resources/agency.csv");
		try {
			CsvSchema csv = CsvSchema.emptySchema().withHeader();
			CsvMapper csvMapper = new CsvMapper();
			MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader().forType(Map.class).with(csv)
					.readValues(input);
			List<Map<?, ?>> list = mappingIterator.readAll();
			log.info(list.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
