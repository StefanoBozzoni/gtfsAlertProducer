package com.vjtech.gtfsAlertProducer.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.transaction.Transactional;

import org.json.JSONObject;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyIn;
import org.postgresql.copy.CopyManager;
import org.postgresql.copy.CopyOperation;
import org.postgresql.core.BaseConnection;
import org.postgresql.core.Encoding;
import org.postgresql.core.QueryExecutor;
import org.postgresql.util.GT;
import org.postgresql.util.PSQLException;
import org.postgresql.util.PSQLState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	@Autowired
	private Environment env;

	@Autowired
	DataSource datasource;

	public TablesLoader() {
		log.info("Controller TableLoader started");
	}

	public void loadTables() throws Exception {
		gson = new GsonBuilder().setLenient().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();

		agencyRepository.deleteAllInBatch();
		getAndWriteJson(local_unzip_dir + "/agency.txt", TableType.AGENCY);

		routesRepository.deleteAllInBatch();
		getAndWriteJson(local_unzip_dir + "/routes.txt", TableType.ROUTES);

		tripsRepository.deleteAllInBatch();
		tripsRepository.flush();
		getAndWriteJson(local_unzip_dir + "/trips.txt", TableType.TRIPS);
		tripsRepository.flush();

		shapesRepository.deleteAllInBatch();
		shapesRepository.flush();
		// this.jdbcTemplate.update("delete from shapes");
		getAndWriteJson(local_unzip_dir + "/shapes.txt", TableType.SHAPES);
		shapesRepository.flush();
	}

	private void loadFile(String connUrl, String myUid, String myPwd, String tableName, String fileName)
			throws SQLException, FileNotFoundException, IOException, InterruptedException, Exception {

		// Connection conn = datasource.getConnection();
		/*
		 * BaseConnection pgConnection =
		 * datasource.getConnection().unwrap(BaseConnection.class); CopyManager
		 * copyManager = new CopyManager(pgConnection); CopyIn cp2 =
		 * copyManager.copyIn("COPY " + tableName + " FROM STDIN (FORMAT csv, HEADER)");
		 * //cp2.writeToCopy(writer);
		 * 
		 * String str; char[] cbuf = new char[1024]; try { while ( !( str =
		 * reader.readLine()).isEmpty()) { byte[] bytes = str.getBytes();
		 * cp2.writeToCopy(bytes, 0, bytes.length); } cp2.endCopy(); } finally { // see
		 * to it that we do not leave the connection locked if(cp2.isActive())
		 * cp2.cancelCopy(); }
		 */

		// try (Connection conn = DriverManager.getConnection(connUrl, myUid, myPwd)) {
		// try (BaseConnection pgConnection =
		// datasource.getConnection().unwrap(BaseConnection.class)) {
		try (Connection pgConnection = DriverManager.getConnection(connUrl, myUid, myPwd)) {
			pgConnection.setAutoCommit(false);
			pgConnection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
			/*
			 * Statement stat = pgConnection.createStatement(); String sqlDelete =
			 * "Delete from "+tableName; stat.execute(sqlDelete); pgConnection.commit();
			 * stat.close();
			 */
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			CopyManager copyManager = new CopyManager((BaseConnection) pgConnection); //((BaseConnection) pgConnection).getCopyAPI();
			long rowsInserted = copyManager.copyIn("COPY " + tableName + " FROM STDIN (FORMAT csv, HEADER)", reader);
			log.info(String.format("Table %s : %d row(s) inserted%n", tableName, rowsInserted));
			reader.close();
			pgConnection.commit();
			pgConnection.setAutoCommit(false); // non rimuovere, sembra che questa istruzione impedisca che la query
												// successiva si pianti (è un bug di postgres jdbc)
			pgConnection.setAutoCommit(true);

			copyManager = null;
			reader = null;
			DataSourceUtils.releaseConnection(pgConnection, datasource);
			pgConnection.close();
		}
	}

	/*
	 * public long copyIn(final String sql, Reader from, int bufferSize,
	 * BaseConnection connection) throws SQLException, IOException {
	 * 
	 * Encoding encoding = connection.getEncoding();
	 * 
	 * char[] cbuf = new char[bufferSize]; int len; CopyIn cp = copyIn(sql,
	 * connection); try { while ((len = from.read(cbuf)) >= 0) { if (len > 0) {
	 * byte[] buf = encoding.encode(new String(cbuf, 0, len)); cp.writeToCopy(buf,
	 * 0, buf.length); } } return cp.endCopy(); } finally { // see to it that we do
	 * not leave the connection locked if (cp.isActive()) { cp.cancelCopy(); } } }
	 */

	/*
	 * public CopyIn copyIn(String sql, BaseConnection connection) throws
	 * SQLException { QueryExecutor queryExecutor = connection.getQueryExecutor();
	 * CopyOperation op = queryExecutor.startCopy(sql, connection.getAutoCommit());
	 * if (op == null || op instanceof CopyIn) { return (CopyIn) op; } else {
	 * op.cancelCopy(); throw new
	 * PSQLException(GT.tr("Requested CopyIn but got {0}", op.getClass().getName()),
	 * PSQLState.WRONG_OBJECT_TYPE); } }
	 */

	public void getAndWriteJson(String filename, TableType tableType) throws Exception {
		File input = new File(filename);
		try {

			String dbConnectionUrl = env.getProperty("spring.datasource.url");
			String dbPassword = env.getProperty("spring.datasource.password");
			String dbUserName = env.getProperty("spring.datasource.username");

			// Try Download file
			if (tableType == TableType.TRIPS) {
				loadFile(dbConnectionUrl, dbUserName, dbPassword, "app24pa_romamobilita.trips", filename);
				Thread.sleep(60000);
			} else if (tableType == TableType.SHAPES) {
				loadFile(dbConnectionUrl, dbUserName, dbPassword, "app24pa_romamobilita.shapes",
						local_unzip_dir + "/shapes.txt");
				Thread.sleep(60000);
			} else {
				Connection conn = DataSourceUtils.getConnection(datasource);
				conn.setAutoCommit(false);

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

						}
					}

					j += i;
				}
				conn.commit();
				conn.setAutoCommit(false);

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
