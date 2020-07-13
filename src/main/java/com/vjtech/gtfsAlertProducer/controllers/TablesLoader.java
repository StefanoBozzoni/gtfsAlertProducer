package com.vjtech.gtfsAlertProducer.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.json.JSONObject;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.postgresql.util.ByteStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vjtech.gtfsAlertProducer.MessageProducer;
import com.vjtech.gtfsAlertProducer.database.model.Agency;
import com.vjtech.gtfsAlertProducer.database.model.Routes;
import com.vjtech.gtfsAlertProducer.repository.AgencyRepository;
import com.vjtech.gtfsAlertProducer.repository.RoutesRepository;
import com.vjtech.gtfsAlertProducer.repository.ShapesRepository;
import com.vjtech.gtfsAlertProducer.repository.TripsRepository;
import com.vjtech.gtfsAlertProducer.services.session.ApplicationBean;

@Component
public class TablesLoader {

	private static final Logger log = LoggerFactory.getLogger(StopScheduler.class);

	private enum TableType {
		AGENCY, ROUTES, TRIPS, SHAPES
	};

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

	@Value("${app.local_unzip_dir}")
	String local_unzip_dir;

	public TablesLoader() {
		log.info("Controller TableLoader started");
	}

	// @GetMapping(value = "/loadTables", produces = MediaType.TEXT_HTML_VALUE)
	// @ResponseBody

	public void loadTables() throws Exception {
		gson = new GsonBuilder().setLenient().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();

		agencyRepository.deleteAllInBatch();
		getAndWriteJson(local_unzip_dir+"/agency.txt", TableType.AGENCY);

		routesRepository.deleteAllInBatch();
		getAndWriteJson(local_unzip_dir+"/routes.txt", TableType.ROUTES);

		tripsRepository.deleteAllInBatch();
		tripsRepository.flush();
		getAndWriteJson(local_unzip_dir + "/trips.txt", TableType.TRIPS);
		tripsRepository.flush();

		shapesRepository.deleteAllInBatch();
		shapesRepository.flush();
		getAndWriteJson(local_unzip_dir + "/shapes.txt", TableType.SHAPES);
		shapesRepository.flush();

	}

	@Autowired
	private Environment env;

	@Autowired
	DataSource datasource;

	@Transactional
	private void loadFile(String connUrl, String myUid, String myPwd, String tableName, String fileName)
			throws SQLException, FileNotFoundException, IOException, InterruptedException, Exception {

		// Connection conn = datasource.getConnection();

		// try (Connection conn = DriverManager.getConnection(connUrl, myUid, myPwd)) {
		/*
		BaseConnection pgConnection = datasource.getConnection().unwrap(BaseConnection.class);
		CopyManager copyManager = new CopyManager(pgConnection);
		CopyIn cp = copyManager.copyIn("COPY " + tableName + " FROM STDIN (FORMAT csv, HEADER)");
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		cp.writeToCopy((ByteStreamWriter) reader);
		*/
		
		
		/*
		String str;
		char[] cbuf = new char[1024];
		try {
		    while ( !( str = reader.readLine()).isEmpty()) {
		    	byte[] bytes = str.getBytes();
		        cp.writeToCopy(bytes, 0, bytes.length);
		    }
		    cp.endCopy();
		} finally { // see to it that we do not leave the connection locked
		    if(cp.isActive())
		        cp.cancelCopy();
		}
		*/
		
		BaseConnection pgConnection = datasource.getConnection().unwrap(BaseConnection.class);
		BufferedReader reader = new BufferedReader(new FileReader(fileName));
		long rowsInserted = new CopyManager(pgConnection).copyIn(
				"COPY " + tableName + " FROM STDIN (FORMAT csv, HEADER)", reader);
		log.info(String.format("Table %s : %d row(s) inserted%n", tableName, rowsInserted));
		reader.close();
		
		pgConnection.cancelQuery();
		DataSourceUtils.releaseConnection(pgConnection, datasource);
		Thread.sleep(60000);

	}

	public void getAndWriteJson(String filename, TableType tableType) throws Exception {
		File input = new File(filename);
		try {

			String dbConnectionUrl = env.getProperty("spring.datasource.url");
			String dbPassword = env.getProperty("spring.datasource.password");
			String dbUserName = env.getProperty("spring.datasource.username");

			// Try Download file
			if (tableType == TableType.TRIPS)
				loadFile(dbConnectionUrl, dbUserName, dbPassword, "app24pa_romamobilita.trips", filename);
			else if (tableType == TableType.SHAPES)
				loadFile(dbConnectionUrl, dbUserName, dbPassword, "app24pa_romamobilita.shapes",
						local_unzip_dir + "/shapes.txt");
			else {

				CsvSchema csv = CsvSchema.emptySchema().withHeader();
				CsvMapper csvMapper = new CsvMapper();
				MappingIterator<Map<?, ?>> mappingIterator = csvMapper.reader().forType(Map.class).with(csv)
						.readValues(input);

				// List<Trips> trips_list = new ArrayList<Trips>();
				int i = 0;
				int j = 0;
				while (mappingIterator.hasNext()) {
					// trips_list.clear();
					i = 0;
					while (mappingIterator.hasNext() && ++i < 500) {
						Map<?, ?> mss = mappingIterator.next();

						JSONObject json = new JSONObject(mss);
						String jsonResultStr = json.toString();

						if (!jsonResultStr.trim().isEmpty()) {

							if (tableType == TableType.AGENCY) {
								Agency agency = gson.fromJson(jsonResultStr, Agency.class);
								log.info(agency.toString());
								agencyRepository.save(agency);
							}

							if (tableType == TableType.ROUTES) {
								Routes routes = gson.fromJson(jsonResultStr, Routes.class);
								log.info(routes.toString());
								routesRepository.save(routes);
							}

							/*
							 * if (tableType == TableType.TRIPS) { Trips trips =
							 * gson.fromJson(jsonResultStr, Trips.class); trips_list.add(trips); }
							 */

						}
					}

					/*
					 * if (tableType == TableType.TRIPS) { tripsRepository.saveAll(trips_list);
					 * log.info("Trips - salvataggio..." + String.valueOf(j)); }
					 */

					j += i;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// TODO: this method isn't used, but could be useful to convert a whole csv to
	// json, using jackson library
	public void getJson(String filename) throws Exception {
		// File input = new ClassPathResource(filename).getFile();
		File input = new File(filename); // "./src/main/resources/routes.csv"
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
