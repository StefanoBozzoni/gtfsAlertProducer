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
	
	private enum TableType {AGENCY, ROUTES, TRIPS, SHAPES};

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
		getAndWriteJson("./src/main/resources/agency.csv", TableType.AGENCY);
		 		
		routesRepository.deleteAllInBatch();
		getAndWriteJson("./src/main/resources/routes.csv", TableType.ROUTES);
		
		tripsRepository.deleteAllInBatch();
		getAndWriteJson("./src/main/resources/trips.csv", TableType.TRIPS);
		
		//shapesRepository.deleteAllInBatch();
		getAndWriteJson("./src/main/resources/shapes.csv", TableType.SHAPES);

	}

	public void getAndWriteJson(String filename, TableType tableType) throws Exception {
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

						if (tableType==TableType.AGENCY) {
							Agency agency = gson.fromJson(jsonResultStr, Agency.class);
							log.info(agency.toString());
							agencyRepository.save(agency);
						}

						if (tableType==TableType.ROUTES) {
							Routes routes = gson.fromJson(jsonResultStr, Routes.class);
							log.info(routes.toString());
							routesRepository.save(routes);
						}

						if (tableType==TableType.TRIPS) {
							Trips trips = gson.fromJson(jsonResultStr, Trips.class);
							trips_list.add(trips);
						}
						
						if (tableType==TableType.SHAPES) {
							Shapes shapes = gson.fromJson(jsonResultStr, Shapes.class);
							
							ShapeId sid = new ShapeId(shapes.getShapeId(), shapes.getShapePtSequence() );
							if (shapesRepository.findById(sid).orElse(null) ==null) {
								shapes_list.add(shapes);
							}														
						}						
					}					
					i++;					
				}

				if (tableType==TableType.TRIPS) {
					tripsRepository.saveAll(trips_list);
					log.info("Trips - salvataggio..."+String.valueOf(j));
				}

				if (tableType==TableType.SHAPES) {
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

	// TODO: this method isn't used, but could be useful to convert a whole csv to json, using jackson library
	public void getJson(String filename) throws Exception {
		// File input = new ClassPathResource(filename).getFile();
		File input = new File(filename);  //"./src/main/resources/routes.csv"
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
