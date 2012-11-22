package org.openimaj.rdf.storm.sparql.topology.builder.datasets;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;

/**
 * Given Database login instructions and a URL to a database create a
 * {@link QueryExecution} which
 * directly accesses a static database
 *
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SDBStaticDataset extends StaticRDFDatasetBase {

	/**
	 *
	 */
	private static final long serialVersionUID = -1666002950525540630L;
	private String url;
	private String username;
	private String password;
	private Dataset dataset;
	private static String driver = "com.mysql.jdbc.Driver";
	static {
		try {
			Class.forName(driver);
			System.out.println("JDBC driver load successfully!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param url
	 * @param username
	 * @param password
	 */
	public SDBStaticDataset(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
	}

	@Override
	public void prepare() {
		StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.MySQL);
		SDBConnection sdbConnection = new SDBConnection(url, username, password);
		Store store = SDBFactory.connectStore(sdbConnection, storeDesc);
		this.dataset = SDBFactory.connectDataset(store);
	}

	@Override
	public QueryExecution createExecution(Query query) {
		return QueryExecutionFactory.create(query, dataset);
	}

	@Override
	public QueryExecution createExecution(Query query, QuerySolution sol) {
		return QueryExecutionFactory.create(query, dataset, sol);
	}

}
