package org.openimaj.rdf.storm.tool.staticdata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.Option;
import org.openimaj.rdf.storm.sparql.topology.builder.datasets.SDBStaticDataset;
import org.openimaj.rdf.storm.sparql.topology.builder.datasets.StaticRDFDataset;
import org.openjena.riot.Lang;
import org.openjena.riot.RiotLoader;
import org.openjena.riot.SysRIOT;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;

/**
 * Loads the RDF from the provided files into the requested database using the
 * appropriate driver
 * and other required configuration information.
 * 
 * The database name is constructed from the rdf file name.
 * 
 * If the database exists it is not overwritten and assumed intact unless the
 * overwrite command is set
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class SDBStaticDataMode implements StaticDataMode {

	@Option(
			name = "--sdb-url",
			aliases = "-sdburl",
			required = false,
			usage = "The URL used to access the database. Defaults to a mysql localhost",
			metaVar = "STRING")
	private String url = "jdbc:mysql://localhost:3306";
	@Option(
			name = "--sdb-username",
			aliases = "-sdbu",
			required = false,
			usage = "The username used to access the database",
			metaVar = "STRING")
	private String username = "root";

	@Option(
			name = "--sdb-pass",
			aliases = "-sdbp",
			required = false,
			usage = "The password used ",
			metaVar = "STRING")
	private String password = "";
	private String driver = "com.mysql.jdbc.Driver";

	@Option(
			name = "--sdb-refresh",
			aliases = "-sdbr",
			required = false,
			usage = "Force a refresh of the database if it exists already, otherwise use it as is.")
	private boolean refresh = false;

	private static final String jdbcUnicode = "useUnicode=true&characterEncoding=UTF-8";
	private final static Logger logger = Logger.getLogger(SDBStaticDataMode.class);

	@Override
	public Map<String, StaticRDFDataset> datasets(Map<String, String> datasetNameLocations) {
		initSDB();
		Map<String, StaticRDFDataset> ret = new HashMap<String, StaticRDFDataset>();
		for (Entry<String, String> es : datasetNameLocations.entrySet()) {
			StaticRDFDataset dataset = prepareDataset(es.getKey(), es.getValue());
			ret.put(es.getKey(), dataset);
		}
		return ret;
	}

	private void initSDB() {
		// load database driver
		try {
			Class.forName(driver);
			logger.debug("JDBC driver load successfully!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private StaticRDFDataset prepareDataset(String name, String location) {
		logger.debug("Preparing static data using Jena SDB");
		SysRIOT.wireIntoJena();
		String dbName = String.format("jeandb_%s", name);
		String dbURL = String.format("%s/%s?%s", url, dbName, jdbcUnicode);
		logger.debug("Attempting to connect to: " + dbURL);
		Connection connection;
		try {
			connection = DriverManager.getConnection(url, username, password);

			DatabaseMetaData meta = connection.getMetaData();
			ResultSet rs = meta.getCatalogs();
			List<String> list = new ArrayList<String>();
			while (rs.next()) {
				String listofDatabases = rs.getString("TABLE_CAT");
				list.add(listofDatabases);
			}
			boolean createDB = true;
			if (list.contains(dbName)) {
				logger.debug("Database exists...");
				if (refresh) {
					logger.debug("Forcing database removal...");
					String hrappSQL = String.format("DROP DATABASE %s", dbName);
					Statement statement = connection.createStatement();
					statement.executeUpdate(hrappSQL);
				} else {
					logger.debug("No remvoing existing database...");
					createDB = false;
				}
			}
			if (createDB) {
				try {
					logger.debug("Creating database...");
					StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.MySQL);
					String hrappSQL = String.format("CREATE DATABASE %s", dbName);
					Statement statement = connection.createStatement();
					statement.executeUpdate(hrappSQL);
					logger.debug("Database created!...creating layout...");
					SDBConnection sdbConnection = new SDBConnection(dbURL, username, password);
					Store store = SDBFactory.connectStore(sdbConnection, storeDesc);
					store.getTableFormatter().create();
					logger.debug("Done!...populating temporary model...");
					Dataset dataset = SDBFactory.connectDataset(store);
					//					String[] lines = FileUtils.readlines(new File(location),"UTF-8");
					//					for (String string : lines) {
					//						try{
					//							RiotLoader.read(new ByteArrayInputStream(string.getBytes()),dataset.asDatasetGraph(),Lang.TURTLE,"");
					//						}
					//						catch(Throwable e){
					//							logger.error("Failed to add triple:\n" + string);
					//						}
					//					}
					String fileURL = location;
					logger.debug("...Loading triples from: " + fileURL);
					Model tmpModel = ModelFactory.createDefaultModel();
					// RiotLoader.read(fileURL, dataset.asDatasetGraph(),Lang.TURTLE);
					RiotLoader.read(location, tmpModel.getGraph(), Lang.NTRIPLES);
					logger.debug("Done!...populating SQL model...");
					dataset.getDefaultModel().add(tmpModel);
					logger.debug("Done!");
					store.close();
				} catch (Throwable e) {
					logger.error("Something went wrong while creating the database, trying to cleanup");
					//					String hrappSQL = String.format("DROP DATABASE %s", dbName);
					//					Statement statement = connection.createStatement();
					//					statement.executeUpdate(hrappSQL);
					logger.error("Done!");
					throw e;
				}

			}
			return new SDBStaticDataset(dbURL, username, password);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}
