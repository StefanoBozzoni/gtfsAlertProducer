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
import java.util.List;
import java.util.Locale;
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
import org.springframework.stereotype.Component;
import org.unbescape.html.HtmlEscape;

import com.google.transit.realtime.GtfsRealtime.Alert;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.vjtech.gtfsAlertProducer.Utils.AppUtils;
import com.vjtech.gtfsAlertProducer.Utils.UnzipFiles;
import com.vjtech.gtfsAlertProducer.database.model.Points;
import com.vjtech.gtfsAlertProducer.database.model.Routes;
import com.vjtech.gtfsAlertProducer.database.model.ZetaRoute;
import com.vjtech.gtfsAlertProducer.repository.GtfsRepository;
import com.vjtech.gtfsAlertProducer.repository.RoutesRepository;
import com.vjtech.gtfsAlertProducer.services.model.CreateAreaRequest;
import com.vjtech.gtfsAlertProducer.services.model.CreateAreaResponse;
import com.vjtech.gtfsAlertProducer.services.model.PostMessageByAreaRequest;
import com.vjtech.gtfsAlertProducer.services.model.PostMessageByAreaResponse;
import com.vjtech.gtfsAlertProducer.services.session.GtfsService;
import com.vjtech.gtfsAlertProducer.services.session.SessionInMemoryDatasource;

@Component
public class MessageProducer {

	private static final Logger log = LoggerFactory.getLogger(GtfsAlertProducerApplication.class);

	private static final int CONNECT_TIMEOUT = 3000;

	private static final int READ_TIMEOUT = 3000;

	@Value("https://romamobilita.it/sites/default/files/rome_rtgtfs_service_alerts_feed.pb")
	String Alert_url_address;

	@Autowired
	private GtfsService gtfsService;

	@Autowired
	private GtfsRepository gtfsRepository;

	@Autowired
	RoutesRepository routesRepository;

	@Autowired
	SessionInMemoryDatasource sessionDataSource;

	@Value("${app.sender_id}")
	int senderId;

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

	private void createNewAreas() throws IOException {

		log.info("Inizio CreateArea");

		URL url = new URL(Alert_url_address);
		FeedMessage feed = FeedMessage.parseFrom(url.openStream());

		// for (FeedEntity entity : feed.getEntityList()) {
		for (int i = 0; i < feed.getEntityCount(); i++) {

			FeedEntity entity = feed.getEntityList().get(i);
			Alert alert = entity.getAlert();

			// recupero informazioni alert e route
			long timeStampStart = alert.getActivePeriod(0).getStart();
			long timeStampEnd = alert.getActivePeriod(0).getEnd();

			String startMsgDate = AppUtils.getDateStringFromPosixTimeStamp(timeStampStart);
			String endMsgDate   = AppUtils.getDateStringFromPosixTimeStamp(timeStampEnd);

			String messageTitle = HtmlEscape.unescapeHtml(alert.getHeaderText().getTranslation(0).getText());
			String messageBody = HtmlEscape.unescapeHtml(alert.getDescriptionText().getTranslation(0).getText());

			log.info(messageTitle);
			log.info(messageBody);

			for (int j = 0; j < alert.getInformedEntityCount(); j++) {

				Integer currIdRoute = Integer.parseInt(alert.getInformedEntity(j).getRouteId());

				// preleva la descrizione della route
				Optional<Routes> foundRoute = routesRepository.findById(currIdRoute);
				if (!foundRoute.isPresent())
					return; // TODO: da modificare

				Routes currRoute = foundRoute.get();
				String currRouteShortName = currRoute.getRouteShortName();

				String areaStr = getAreaAsString(currIdRoute);
				GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
				WKTReader reader = new WKTReader(geometryFactory);

				LineString line = null;
				Geometry bufferLine = null;

				try {
					line = (LineString) reader.read(areaStr);
					bufferLine = line.buffer(0.002);
				} catch (org.locationtech.jts.io.ParseException p) {
					log.info("parse exception");
				}

				ZetaRoute zr = gtfsRepository.findZetaRouteByIdroute(currIdRoute);

				if (zr == null || (zr != null && zr.getIdarea() == null)) {
					log.info(bufferLine.toText());
					@SuppressWarnings("serial")
					CreateAreaRequest areaRequest = new CreateAreaRequest() {
						{
							senderId = 2;
							areaName = currRouteShortName + " prova";
						}
					};

					areaRequest.textArea = bufferLine.toText();
					if (zr != null)
						areaRequest.areaId = zr.getIdarea();

					// Il servizio seguente inserisce l'area se areaRequest.areaId é null altrimenti
					// restituisce l'area associata all'idRoute Inviata
					CreateAreaResponse response = gtfsService.createAreaAstext(areaRequest);

					// Inserisce o aggiorna su DB (tabella zeta_route) l'area, l'IdRoute e la
					// descrizione della Route
					if (zr == null) // se non c'era il record lo inserisco
						gtfsRepository.insertNewArea(currIdRoute, bufferLine, "Linea: " + currRouteShortName,
								response.areaId);
					else { // ...altrimenti aggiorno
						gtfsRepository.updateArea(zr, bufferLine, "Linea: " + currRouteShortName, response.areaId);
					}

					sendMessagesToNewWhereApp(startMsgDate, endMsgDate, messageTitle, messageBody, response.areaId);

				}
				break; // TODO: to remove, just for test

			} // for (int j=0...

			break; // TODO: to remove, just for test

		} // for (int i=0...

		// zetaRoutesRepository.insertRoute(249, "prova", line );
	}

	private void sendMessagesToNewWhereApp(String startDate, String endDate, String title, String body, Integer areaId)
			throws IOException {
		// Invio il servizio di notifica di whereapp
		PostMessageByAreaRequest messageRequest = new PostMessageByAreaRequest();
		messageRequest.setStartDate(startDate);
		messageRequest.setEndDate(endDate);
		messageRequest.setSenderId(senderId);
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
		return "LINESTRING(" + points_str + ")";
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
	


}
