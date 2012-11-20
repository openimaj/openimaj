package org.openimaj.rdf.storm.tool.staticdata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.rdf.storm.sparql.topology.builder.datasets.SDBStaticDataset;
import org.openimaj.rdf.storm.sparql.topology.builder.datasets.StaticRDFDataset;
import org.openjena.riot.RiotLoader;
import org.openjena.riot.SysRIOT;

import com.hp.hpl.jena.query.Dataset;
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

	private String url = "jdbc:mysql://localhost:3306";
	private String username = "root";
	private String password = "";
	private String driver = "com.mysql.jdbc.Driver";
	private boolean refresh = true;

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
			System.out.println("JDBC driver load successfully!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private StaticRDFDataset prepareDataset(String name, String location) {
		SysRIOT.wireIntoJena();
		String dbName = String.format("jeandb_%s", name);
		String dbURL = String.format("%s/%s", url, dbName);
		Connection connection;
		try {
			connection = DriverManager.getConnection(url, username, password);
			Statement statement = connection.createStatement();

			DatabaseMetaData meta = connection.getMetaData();
			ResultSet rs = meta.getCatalogs();
			List<String> list = new ArrayList<String>();
			while (rs.next()) {
				String listofDatabases = rs.getString("TABLE_CAT");
				list.add(listofDatabases);
			}
			boolean createDB = true;
			if (list.contains(dbName)) {
				if (refresh) {
					String hrappSQL = String.format("DROP DATABASE %s", dbName);
					statement.executeUpdate(hrappSQL);
				} else {
					createDB = false;
				}
			}
			if (createDB) {
				StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesHash, DatabaseType.MySQL);
				String hrappSQL = String.format("CREATE DATABASE %s", dbName);
				statement.executeUpdate(hrappSQL);
				SDBConnection sdbConnection = new SDBConnection(dbURL, username, password);
				Store store = SDBFactory.connectStore(sdbConnection, storeDesc);
				store.getTableFormatter().create();
				Dataset dataset = SDBFactory.connectDataset(store);
				RiotLoader.read(location, dataset.asDatasetGraph());
				store.close();
			}
			return new SDBStaticDataset(dbURL, username, password);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
