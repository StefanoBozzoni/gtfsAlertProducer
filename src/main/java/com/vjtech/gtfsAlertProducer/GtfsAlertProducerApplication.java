package com.vjtech.gtfsAlertProducer;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.util.Strings;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
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
import com.google.protobuf.TextFormat.ParseException;
import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.vjtech.gtfsAlertProducer.Utils.UnzipFiles;
import com.vjtech.gtfsAlertProducer.database.model.Agency;
import com.vjtech.gtfsAlertProducer.database.model.Points;
import com.vjtech.gtfsAlertProducer.database.model.Routes;
import com.vjtech.gtfsAlertProducer.database.model.ZetaRoute;
import com.vjtech.gtfsAlertProducer.repository.AgencyRepository;
import com.vjtech.gtfsAlertProducer.repository.GtfsRepository;
import com.vjtech.gtfsAlertProducer.repository.RoutesRepository;
import com.vjtech.gtfsAlertProducer.repository.ZetaRouteRepository;
import com.vjtech.gtfsAlertProducer.services.JobZoneResponse;
import com.vjtech.gtfsAlertProducer.services.model.AccessTokenResponse;
import com.vjtech.gtfsAlertProducer.services.model.CreateAreaRequest;
import com.vjtech.gtfsAlertProducer.services.model.CreateAreaResponse;
import com.vjtech.gtfsAlertProducer.services.model.JobZoneRequest;
import com.vjtech.gtfsAlertProducer.services.model.PostMessageByAreaRequest;
import com.vjtech.gtfsAlertProducer.services.model.PostMessageByAreaResponse;
import com.vjtech.gtfsAlertProducer.services.session.GtfsService;
import com.vjtech.gtfsAlertProducer.services.session.ISessionService;
import com.vjtech.gtfsAlertProducer.services.session.SessionInMemoryDatasource;
import com.vjtech.gtfsAlertProducer.services.session.SessionService;

@SpringBootApplication()
public class GtfsAlertProducerApplication {

	private static final Logger log = LoggerFactory.getLogger(GtfsAlertProducerApplication.class);

	private static final int CONNECT_TIMEOUT = 3000;

	private static final int READ_TIMEOUT = 3000;

	@Value("https://romamobilita.it/sites/default/files/rome_rtgtfs_service_alerts_feed.pb")
	String Alert_url_address;

	//@Autowired
	//ScheduledFuture<?> mainTaskScheduler;

	public static void main(String[] args) {
		log.info("Starting");
		SpringApplication.run(GtfsAlertProducerApplication.class, args);
	}


	@Autowired
	private GtfsService gtfsService;
	
	@Autowired
	private GtfsRepository gtfsRepository;

	@Autowired
	RoutesRepository routesRepository;
	
	@Autowired
	SessionInMemoryDatasource sessionDataSource;

	@Bean
	AccessTokenResponse getTokenResponse(SessionService service) throws IOException {
		return service.getAccessToken();
	}

	@Bean
	public CommandLineRunner demo(AccessTokenResponse tokenResponse) {

		return (args) -> {
			log.info("Avvio scheduler...");

			//getJson("agency.csv");
			//getJson("routes.csv");			
			//displayAlertsDates();

			sessionDataSource.setAccessToken(tokenResponse.accessToken);
			sessionDataSource.setRefreshToken(tokenResponse.refreshToken);
			sessionDataSource.setTaskScheduler(startBackGroundThread());

			log.info("************ ACCESS TOKEN *************");
			log.info(tokenResponse.accessToken);
			log.info("***************************************");

			//mainTaskScheduler.notify();
			log.info("Scheduler avviato");
			
		};
	}

	//@Bean
	ScheduledFuture<?> startBackGroundThread() throws InterruptedException {
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
		ScheduledFuture<?> task = scheduledExecutorService.scheduleAtFixedRate(() -> checkRemoteFileUpdates(), 0, 30, TimeUnit.SECONDS);     
		//task.wait();
		return task;
	}

	private List<Points> getGeoPoints(long IdRoute) { 	  
	  List<Object[]> object_list = routesRepository.findPointsByRouteId2(IdRoute);	  	  
	  List<Points> points_list = object_list.stream().map( 
			 (Object[] el) -> {
				 	BigDecimal lat = new BigDecimal(el[0].toString()); 
				 	BigDecimal lon = new BigDecimal(el[1].toString()); 
				 	return new Points(lon, lat);
			 }).collect(Collectors.toList());
	  		
	  return points_list;
	}

	void InvioMessagioFromAlert(Alert alert) throws IOException {

		long timeStampStart = alert.getActivePeriod(0).getStart();
		long timeStampEnd = alert.getActivePeriod(0).getEnd();

		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH);
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

	void checkRemoteFileUpdates() {

		try {
			// Read the new alert file and cycle over the messages...
			// scarica il file
			FileUtils.copyURLToFile(new URL("https://romamobilita.it/sites/default/files/rome_static_gtfs.zip.md5"),
					new File("c:/temp/rome_static_gtfs.txt"), CONNECT_TIMEOUT, READ_TIMEOUT);

			String md5CheckSum = "";
			try {
				md5CheckSum = new String(Files.readAllBytes(Paths.get("c:/temp/rome_static_gtfs.txt")));
			} catch (IOException e) {
				e.printStackTrace();
			}
			log.info("***************");
			log.info(md5CheckSum);

			if (!md5CheckSum.equals(sessionDataSource.getMd5Checksum())) {
				log.info("download Files");
				sessionDataSource.setMd5Checksum(md5CheckSum);
				downloadFiles();
				createNewAreas();
				// sendMessagesToWhereApp
			}

		} catch (Exception e) {
			log.info(e.getMessage());
		}

	}

	private void downloadFiles() throws IOException {
		FileUtils.copyURLToFile(new URL("https://romamobilita.it/sites/default/files/rome_static_gtfs.zip"),
				new File("c:/temp/rome_static_gtfs.zip"), CONNECT_TIMEOUT, READ_TIMEOUT);
		UnzipFiles.unzip("c:/temp/rome_static_gtfs.zip", "c:/temp/prova");
	}

	private String getAreaAsString(long idRoute) throws IOException {
		
		List<String> listPoints = getGeoPoints(idRoute).stream().map( (Points el) -> el.getLongitude()+" "+el.getLatitude()).collect(Collectors.toList());
		String points_str = Strings.join(listPoints, ',');
		log.info(points_str);
		return "LINESTRING("+points_str+")";
	}

	
	@Value("${app.sender_id}") int senderId;
	
	private void createNewAreas() throws IOException {
		
		log.info("Inizio CreateArea");
		
		URL url = new URL(Alert_url_address);
		FeedMessage feed = FeedMessage.parseFrom(url.openStream());		
		//for (FeedEntity entity : feed.getEntityList()) {

		FeedEntity entity = feed.getEntityList().get(0);
		Alert alert = entity.getAlert();
		
		//recupero informazioni alert e route
		
		long timeStampStart = alert.getActivePeriod(0).getStart();
		long timeStampEnd = alert.getActivePeriod(0).getEnd();

		String startMsgDate = getDateStringFromPosixTimeStamp(timeStampStart);
		String endMsgDate = getDateStringFromPosixTimeStamp(timeStampEnd);
		
		String messageTitle = HtmlEscape.unescapeHtml(alert.getHeaderText().getTranslation(0).getText());
		String messageBody =  HtmlEscape.unescapeHtml(alert.getDescriptionText().getTranslation(0).getText());
		
		log.info(messageTitle);
		log.info(messageBody);
		
		Integer currIdRoute   = Integer.parseInt(alert.getInformedEntity(0).getRouteId());
		
		//preleva la descrizione della route
		Optional<Routes> foundRoute = routesRepository.findById(currIdRoute);
		if (!foundRoute.isPresent()) return;  //TODO: da modificare
		
		Routes currRoute = foundRoute.get();
		String currRouteShortName= currRoute.getRouteShortName();
		
		String areaStr = getAreaAsString(currIdRoute);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		WKTReader reader = new WKTReader( geometryFactory );

		LineString line=null;
		Geometry bufferLine=null;
		try {
			line = (LineString) reader.read(areaStr);
			bufferLine = line.buffer(0.002);
		}
		catch (org.locationtech.jts.io.ParseException p) {
			log.info("parse exception");			
		}
		
		ZetaRoute zr = gtfsRepository.findZetaRouteByIdroute(currIdRoute);				
		if (zr==null || (zr!=null && zr.getIdarea()==null)) {	
			log.info(bufferLine.toText());
			@SuppressWarnings("serial")
			CreateAreaRequest areaRequest = new CreateAreaRequest() {
				{				
					senderId = 2;
					areaName = currRouteShortName;
				}
			};			
			areaRequest.textArea= bufferLine.toText();
			if (zr!=null) areaRequest.areaId= zr.getIdarea();
			
			//Il servizio seguente inserisce l'area se areaRequest.areaId é null altrimenti restituisce l'area associata all'idRoute Inviata 
 		    CreateAreaResponse response= gtfsService.createAreaAstext(areaRequest); 
 		     
			//Inserisce o aggiorna su DB (tabella zeta_route) l'area,  l'IdRoute e la descrizione della Route
			if (zr==null) //se non c'era il record lo inserisco
			    gtfsRepository.insertNewArea(currIdRoute, bufferLine, "Linea: "+currRouteShortName, response.areaId);
			else {  //...altrimenti aggiorno
				gtfsRepository.updateArea(zr, bufferLine, "Linea: "+currRouteShortName, response.areaId);
			}
		
			//Invio il servizio di notifica di whereapp
			PostMessageByAreaRequest messageRequest = new PostMessageByAreaRequest();
			messageRequest.setStartDate(startMsgDate);
			messageRequest.setEndDate(endMsgDate);
			messageRequest.setSenderId(senderId);
			messageRequest.setLanguage("IT");
			messageRequest.setCategoryCode("CTG21");
			messageRequest.setSubject(messageTitle);
			messageRequest.setBody(messageBody);
			messageRequest.setMultiArea(false);
			messageRequest.setAreaIds(new ArrayList<Integer>(Arrays.asList(response.areaId)));
			PostMessageByAreaResponse responsePost = gtfsService.postMessageByArea(messageRequest);
			log.info("Messaggio inviato"+responsePost.toString());
		}
		
		//zetaRoutesRepository.insertRoute(249, "prova", line );
	}
	
	private void displayAlertsDates() throws IOException {
		log.info("sono schedulato...");
		URL url = new URL(Alert_url_address);
		FeedMessage feed = FeedMessage.parseFrom(url.openStream());
		Alert alert;
		for (FeedEntity entity : feed.getEntityList()) {
			log.info(entity.getTripUpdate().toString());
			alert = entity.getAlert();
			
			if (!entity.getIsDeleted()) {
				
				long timeStampStart = alert.getActivePeriod(0).getStart();
				long timeStampEnd = alert.getActivePeriod(0).getEnd();
				
				log.info(alert.getInformedEntity(0).getRouteId().toString());
				
				log.info(String.valueOf(timeStampStart));
				log.info(String.valueOf(timeStampEnd));

				DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ITALIAN);
				String formattedDateStart = outputFormatter.format(new Timestamp(timeStampStart*1000L).toLocalDateTime());
				String formattedDateEnd = outputFormatter.format(new Timestamp(timeStampEnd*1000L).toLocalDateTime());

				log.info(formattedDateStart);
				log.info(formattedDateEnd);

			}
		}
	}
	
	private String getDateStringFromPosixTimeStamp(long timeStamp) {
		DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ITALIAN);
		String formattedDate = outputFormatter.format(new Timestamp(timeStamp*1000L).toLocalDateTime());
		return formattedDate;
	}

	private void sendMessagesToWhereApp() throws IOException {
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
			//Map<?,?> mss = mappingIterator.next();
			List<Map<?, ?>> list = mappingIterator.readAll();
			log.info(list.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
