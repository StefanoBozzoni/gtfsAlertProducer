package com.vjtech.gtfsAlertProducer;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.util.Strings;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.unbescape.html.HtmlEscape;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.protobuf.TextFormat.ParseException;
import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.vjtech.gtfsAlertProducer.Utils.AppUtils;
import com.vjtech.gtfsAlertProducer.Utils.Constants;
import com.vjtech.gtfsAlertProducer.Utils.UnzipFiles;
import com.vjtech.gtfsAlertProducer.controllers.TablesLoader;
import com.vjtech.gtfsAlertProducer.database.model.Points;
import com.vjtech.gtfsAlertProducer.database.model.Routes;
import com.vjtech.gtfsAlertProducer.database.model.ZetaRoute;
import com.vjtech.gtfsAlertProducer.repository.GtfsRepository;
import com.vjtech.gtfsAlertProducer.repository.RoutesRepository;
import com.vjtech.gtfsAlertProducer.services.JobZoneResponse;
import com.vjtech.gtfsAlertProducer.services.model.CreateAreaRequest;
import com.vjtech.gtfsAlertProducer.services.model.CreateAreaResponse;
import com.vjtech.gtfsAlertProducer.services.model.JobZoneRequest;
import com.vjtech.gtfsAlertProducer.services.model.PostMessageByAreaRequest;
import com.vjtech.gtfsAlertProducer.services.model.PostMessageByAreaResponse;
import com.vjtech.gtfsAlertProducer.services.session.GtfsService;
import com.vjtech.gtfsAlertProducer.services.session.ApplicationBean;

@Component
public class MessageProducer {

	private static final Logger log = LoggerFactory.getLogger(MessageProducer.class);

	private static final int CONNECT_TIMEOUT = 30000;

	private static final int READ_TIMEOUT = 30000;

	@Value("${app.alert_url_address}")
	String alert_url_address;

	@Autowired
	private GtfsService gtfsService;

	@Autowired
	private GtfsRepository gtfsRepository;

	@Autowired
	RoutesRepository routesRepository;

	@Autowired
	ApplicationBean applicationBean;
	
	@Autowired
	TablesLoader tablesLoader;

	@Value("${app.sender_id}")
	int appSenderId;
	
	@Value("${app.remote_gtfs_md5_url}")
	String remote_gtfs_md5_url;

	@Value("${app.local_gtfs_md5_filepath}")
	String local_gtfs_md5_filepath;
	
	@Value("${app.remote_gtfs_zip_url}")
	String remote_gtfs_zip_url;
	
	@Value("${app.local_gtfs_zip_path}")
	String local_gtfs_zip_path;

	@Value("${app.local_unzip_dir}")
	String local_unzip_dir;	

	@Value("${app.load_tables_at_start}")
	Boolean load_tables_at_start;	
	
	public void checkRemoteFileUpdates() {

		try {
			if (!load_tables_at_start) applicationBean.loadMd5ChecksumFromDisk();
			
			//donwload md5 checksum file
			FileUtils.copyURLToFile(new URL(remote_gtfs_md5_url), new File(local_gtfs_md5_filepath), CONNECT_TIMEOUT, READ_TIMEOUT);

			String md5CheckSum = "";
			try {
				md5CheckSum = new String(Files.readAllBytes(Paths.get(local_gtfs_md5_filepath)));
			} catch (IOException e) {
				md5CheckSum=applicationBean.getMd5Checksum();
				e.printStackTrace();
			}
			
			log.info("*****MD5*******");
			log.info(md5CheckSum);

			if (!md5CheckSum.equals(applicationBean.getMd5Checksum()) || true) {
				log.info("download Files");
				
				try {
					downloadFiles(); //scarica i file dal link di Roma mobilità				
					applicationBean.getTaskScheduler().cancel(false); //stoppa il processo di scheduling
					tablesLoader.loadTables(); //carica le tabelle dai files scaricati
					sendMessages();  //Sends messages to whereapp					
					applicationBean.setMd5Checksum(md5CheckSum); //memorizza il nuovo checksum
					applicationBean.persistMd5Checksum(); //...e lo scrive su disco
				} catch(Exception e) {
					//Riavvia lo scheduling
					log.info("Errore durante il processo di acquisizione tabelle ed invio messaggi: "+e.getMessage());
				}										
				finally {
					log.info("restarting scheduler");
					applicationBean.restartScheduler();
				}
			}

		} catch (Exception e) {
			log.info(e.getMessage());
		}

	}

	private void downloadFiles() throws IOException {
		File dest_file = new File(local_gtfs_zip_path);
		dest_file.delete();		
		FileUtils.copyURLToFile(new URL(remote_gtfs_zip_url),dest_file, CONNECT_TIMEOUT, READ_TIMEOUT);
	
		log.info("file "+remote_gtfs_zip_url+" scaricato");
		try {
			UnzipFiles.unzip(local_gtfs_zip_path, local_unzip_dir);
			log.info("file "+local_gtfs_zip_path+" scompattato");
		} catch(Exception e) {
			log.info("errore nello scompattamento del file "+local_gtfs_zip_path+" "+e.getMessage());
		}
		
	}

	private void sendMessages() throws IOException {

		log.info("Inizio CreateArea");
		URL url = new URL(alert_url_address);
		FeedMessage feed = FeedMessage.parseFrom(url.openStream());

		// for (FeedEntity entity : feed.getEntityList()) {
		for (int i = 0; i < feed.getEntityCount(); i++) {

			FeedEntity entity = feed.getEntityList().get(i);
			Alert alert = entity.getAlert();
			Integer currIdAlert = Integer.parseInt(entity.getId());
			
			log.info("***********AREA: "+currIdAlert);

			// recupero informazioni alert e route
			long timeStampStart = alert.getActivePeriod(0).getStart();
			long timeStampEnd = alert.getActivePeriod(0).getEnd();

			String startMsgDate = AppUtils.getDateStringFromPosixTimeStamp(timeStampStart);
			String endMsgDate = AppUtils.getDateStringFromPosixTimeStamp(timeStampEnd);

			String messageTitle = HtmlEscape.unescapeHtml(alert.getHeaderText().getTranslation(0).getText());
			String messageBody = HtmlEscape.unescapeHtml(alert.getDescriptionText().getTranslation(0).getText());

			log.info(messageTitle);

			for (int j = 0; j < alert.getInformedEntityCount(); j++) {
				
				String routeIdStr = alert.getInformedEntity(j).getRouteId();
				
				if (routeIdStr.isEmpty()) {
					log.info("Routes non presenti nell'alert ! Attualmente queste alert non sono gestite.");
					break;
				}
				Integer currIdRoute;
				try {
					currIdRoute = Integer.parseInt(routeIdStr);
				}
				catch (NumberFormatException e){
					log.info("Errore nel parsing della routeId "+routeIdStr);
					break;
				}

				// preleva la descrizione della route
				Optional<Routes> foundRoute = routesRepository.findById(currIdRoute);
				if (!foundRoute.isPresent()) break; 

				Routes currRoute = foundRoute.get();
				String currRouteShortName = currRoute.getRouteShortName();

				String areaStr = getAreaAsString(currIdRoute);
				
				if (areaStr.isEmpty()) {
					log.info("Punti dell'area non trovati!");
					break;
				}
				
				GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
				WKTReader reader = new WKTReader(geometryFactory);

				LineString line = null;
				Geometry bufferLine = null;

				try {
					line = (LineString) reader.read(areaStr);
					bufferLine = line.buffer(0.002);
				} catch (org.locationtech.jts.io.ParseException p) {
					log.info("geography parse exception");
				}

				ZetaRoute zr = gtfsRepository.findZetaRouteByIdroute(currIdRoute);

				Integer zrIdAlert=0;
				if (zr == null)
					zrIdAlert = 0;
				else {
					if (zr.getIdalert_last() == null) {
						zrIdAlert = 0;
					} else
						zrIdAlert = zr.getIdalert_last();
				}

				log.info("************ELABORATO***********: "+gtfsRepository.isAlertElaborated(currIdAlert).toString());
				
				//se non trova il record, oppure lo trova con un area non definita o se l'idalert è successivo a quello già inviato 
				if (zr == null || (zr != null && (zr.getIdarea() == null || !gtfsRepository.isAlertElaborated(currIdAlert) ))) {  // currIdAlert>zrIdAlert
					log.info(bufferLine.toText());
					@SuppressWarnings("serial")
					CreateAreaRequest areaRequest = new CreateAreaRequest() {
						{
							senderId = appSenderId;
							areaName = currRouteShortName;
						}
					};

					areaRequest.textArea = bufferLine.toText();
					if (zr != null)
						areaRequest.areaId = zr.getIdarea();

					// Il servizio seguente inserisce l'area se areaRequest.areaId é null altrimenti
					// restituisce l'area associata all'idRoute Inviata
					CreateAreaResponse response = gtfsService.createAreaAstext(areaRequest);
					
					try {
						
						// Inserisce o aggiorna su DB (tabella zeta_route) l'area, l'IdRoute e la descrizione della Route
						if (zr == null) // se il record non c'è, lo inserisco
							gtfsRepository.insertNewArea(currIdRoute, bufferLine, currRouteShortName, response.areaId, currIdAlert);
						else { // ...altrimenti aggiorno
							gtfsRepository.updateArea(zr, bufferLine, currRouteShortName, response.areaId, currIdAlert);
						}
						
						//Invio messaggio di alert
						sendMessagesToNewWhereApp(startMsgDate, endMsgDate, messageTitle, messageBody, response.areaId);
						gtfsRepository.registerAlert(currIdAlert, Constants.ELABORATA);
						
					} catch(Exception e) {						
						
						gtfsRepository.registerAlert(currIdAlert, Constants.DA_ELABORARE);
						log.info("Errore nell'invio del messaggio:" + e.getMessage());
						
					}
				}

			} // for (int j=0...

		} // for (int i=0...

	}

	private void sendMessagesToNewWhereApp(String startDate, String endDate, String title, String body, Integer areaId)
			throws IOException {
		// Invio il servizio di notifica di whereapp
		PostMessageByAreaRequest messageRequest = new PostMessageByAreaRequest();
		messageRequest.setStartDate(startDate);
		messageRequest.setEndDate(endDate);
		messageRequest.setSenderId(appSenderId);
		messageRequest.setLanguage("IT");
		messageRequest.setCategoryCode("CTG21"); // Trasporto pubblico
		messageRequest.setSubject(title);
		messageRequest.setBody(body);
		messageRequest.setMultiArea(false);
		messageRequest.setAreaIds(new ArrayList<Integer>(Arrays.asList(areaId)));
		PostMessageByAreaResponse responsePost = gtfsService.postMessageByArea(messageRequest);
		log.info("Messaggio inviato" + responsePost.toString());
	}

	private String getAreaAsString(long idRoute) throws IOException {
		List<String> listPoints = getGeoPoints(idRoute).stream()
				.map((Points el) -> el.getLongitude() + " " + el.getLatitude()).collect(Collectors.toList());
		String points_str = Strings.join(listPoints, ',');
		log.info(points_str);
		return points_str.isEmpty()?"":"LINESTRING(" + points_str + ")";
	}

	private List<Points> getGeoPoints(long IdRoute) {
		List<Object[]> object_list = routesRepository.findPointsByRouteId2(IdRoute);
		List<Points> points_list = object_list.stream().map((Object[] el) -> {
			BigDecimal lat = new BigDecimal(el[0].toString());
			BigDecimal lon = new BigDecimal(el[1].toString());
			return new Points(lon, lat);
		}).collect(Collectors.toList());

		return points_list;
	}

	private void displayAlertsDates() throws IOException {
		log.info("sono schedulato...");
		URL url = new URL(alert_url_address);
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

				DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
						Locale.ITALIAN);
				String formattedDateStart = outputFormatter
						.format(new Timestamp(timeStampStart * 1000L).toLocalDateTime());
				String formattedDateEnd = outputFormatter.format(new Timestamp(timeStampEnd * 1000L).toLocalDateTime());

				log.info(formattedDateStart);
				log.info(formattedDateEnd);

			}
		}
	}

	@Deprecated
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

	@Deprecated
	private void sendMessagesToWhereApp() throws IOException {
		log.info("sono schedulato...");
		URL url = new URL(alert_url_address);
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

}
