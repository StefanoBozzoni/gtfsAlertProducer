package com.vjtech.gtfsAlertProducer.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vjtech.gtfsAlertProducer.GtfsAlertProducerApplication;
import com.vjtech.gtfsAlertProducer.MessageProducer;
import com.vjtech.gtfsAlertProducer.database.model.Agency;
import com.vjtech.gtfsAlertProducer.database.model.Routes;
import com.vjtech.gtfsAlertProducer.database.model.ShapeId;
import com.vjtech.gtfsAlertProducer.database.model.Shapes;
import com.vjtech.gtfsAlertProducer.database.model.Trips;
import com.vjtech.gtfsAlertProducer.repository.AgencyRepository;
import com.vjtech.gtfsAlertProducer.repository.RoutesRepository;
import com.vjtech.gtfsAlertProducer.repository.ShapesRepository;
import com.vjtech.gtfsAlertProducer.repository.TripsRepository;
import com.vjtech.gtfsAlertProducer.services.session.ApplicationBean;

@Component
public class TablesLoader {

	private static final Logger log = LoggerFactory.getLogger(StopScheduler.class);

	File input = null;
	Gson gson = null;

	@Autowired
	ApplicationBean sessionDataSource;

	@Autowired
	MessageProducer messageProduce;

	@Autowired
	AgencyRepository agencyRepository;

	@Autowired
	RoutesRepository routesRepository;

	@Autowired
	TripsRepository tripsRepository;

	@Autowired
	ShapesRepository shapesRepository;

	public TablesLoader() {
		log.info("Controller TableLoader started");
	}

	//@GetMapping(value = "/loadTables", produces = MediaType.TEXT_HTML_VALUE)
	//@ResponseBody
	
	public void loadTables() throws Exception {
		gson = new GsonBuilder().setLenient().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
		
		agencyRepository.deleteAllInBatch();
		getAndWriteJson("./src/main/resources/agency.csv", "Agency");
		 
		
		routesRepository.deleteAllInBatch();
		getAndWriteJson("./src/main/resources/routes.csv", "Routes");
		
		/*
		tripsRepository.deleteAllInBatch();
		getAndWriteJson("./src/main/resources/trips.csv", "Trips");
		
		shapesRepository.deleteAllInBatch();
		getAndWriteJson("./src/main/resources/shapes.csv", "Shapes");
		*/

		/*
		Date data = new Date();
		return "<html>\n" + "<header><title>Elaborato</title></header>\n" + "<body>\n" + "Hello world\n"
				+ data.toString() + "</body>\n" + "</html>";
		*/

	}

	// TODO: the following method will be useful to import csv files into tables
	public <T> void getAndWriteJson(String filename, String type) throws Exception {
		File input = new File(filename);
		try {
			CsvSchema csv = CsvSchema.emptySchema().withHeader();
			CsvMapper csvMapper = new CsvMapper();
			MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader().forType(Map.class).with(csv)
					.readValues(input);
			
			List<Trips>  trips_list  = new ArrayList<Trips>();
			List<Shapes> shapes_list = new ArrayList<Shapes>();

			int i = 0;
			int j = 0;
			while (mappingIterator.hasNext()) {
				
				trips_list.clear();
				shapes_list.clear();
				
				i=0;
				while (mappingIterator.hasNext() && i < 500) {
					Map<?, ?> mss = mappingIterator.next();

					JSONObject json = new JSONObject(mss);
					String jsonResultStr = json.toString();

					if (!jsonResultStr.trim().isEmpty()) {

						if (type.equals("Agency")) {
							Agency agency = gson.fromJson(jsonResultStr, Agency.class);
							log.info(agency.toString());
							agencyRepository.save(agency);
						}

						if (type.equals("Routes")) {
							Routes routes = gson.fromJson(jsonResultStr, Routes.class);
							log.info(routes.toString());
							routesRepository.save(routes);
						}

						if (type.equals("Trips")) {
							Trips trips = gson.fromJson(jsonResultStr, Trips.class);
							trips_list.add(trips);
						}
						
						if (type.equals("Shapes")) {
							Shapes shapes = gson.fromJson(jsonResultStr, Shapes.class);
							
							ShapeId sid = new ShapeId(shapes.getShapeId(), shapes.getShapePtSequence() );
							if (shapesRepository.findById(sid).orElse(null) ==null) {
								shapes_list.add(shapes);
							}														
						}						
					}					
					i++;					
				}

				if (type.equals("Trips")) {
					tripsRepository.saveAll(trips_list);
					log.info("Trips - salvataggio..."+String.valueOf(j));
				}

				if (type.equals("Shapes")) {
					if (shapes_list.size()!=0) {
					   shapesRepository.saveAll(shapes_list);
					   log.info("Shapes - salvataggio...");
					}
					log.info(""+String.valueOf(j));
				}				
				j+=i;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// TODO: the following method will be useful to import csv files into tables
	public void getJson(String filename) throws Exception {
		// File input = new ClassPathResource(filename).getFile();
		File input = new File(filename);  //"./src/main/resources/routes.csv"
		try {
			CsvSchema csv = CsvSchema.emptySchema().withHeader();
			CsvMapper csvMapper = new CsvMapper();
			MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader().forType(Map.class).with(csv)
					.readValues(input);
			// Map<?,?> mss = mappingIterator.next();
			List<Map<?, ?>> list = mappingIterator.readAll();

			log.info(list.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
