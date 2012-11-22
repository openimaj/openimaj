package org.openimaj.rdf.storm;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.openimaj.time.Timer;
import org.openimaj.util.parallel.GlobalExecutorPool.DaemonThreadFactory;
import org.openimaj.util.parallel.Operation;
import org.openimaj.util.parallel.Parallel;
import org.openjena.riot.Lang;
import org.openjena.riot.RiotLoader;
import org.openjena.riot.SysRIOT;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.sparql.util.ModelUtils;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SDBPlayground {




	static String url = "jdbc:mysql://localhost:3306";
	static String name = "users";
	static String dbName = String.format("jeandb_%s", name );
	static String dbURL = String.format("%s/%s", url, dbName);
	static String username = "root";
	static String password = "";
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
	 * @param args
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
//		createTestDatabase();

		Parallel.forIndex(0, 1000, 1, new Operation<Integer>(){

			@Override
			public void perform(Integer object) {
				System.out.println("Started call on thread " + Thread.currentThread().getId());
				String qString = "PREFIX  dc:   <http://purl.org/dc/elements/1.1/> " +
				"PREFIX  sioc: <http://rdfs.org/sioc/ns#>" +
				"PREFIX  sibp: <http://www.ins.cwi.nl/sib/person/>" +
				"PREFIX  foaf: <http://xmlns.com/foaf/0.1/>" +
				"" +
				"SELECT DISTINCT  *" +
				"WHERE" +
					"{ ?1 sioc:moderator_of ?2" +
					"{ { " +
					"SELECT  ?0 ?1" +
					"WHERE" +
						"{   { ?0 foaf:knows ?1 }" +
						"UNION" +
						"{ " +
							"?0 foaf:knows ?user2 ." +
							"?user2 foaf:knows ?1" +
						"}" +
						"FILTER ( ?0 != ?1 )" +
						"}" +
					"}" +
					"?0 sioc:account_of sibp:p941" +
					"}}";
				System.out.println("Getting connection: " + Thread.currentThread().getId());

				System.out.println("Creating storeDesc: " + Thread.currentThread().getId());
				StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.MySQL);
				System.out.println("Creating SDBConnection: " + Thread.currentThread().getId());
				final SDBConnection sdbConnection = SDBConnectionFactory.create(dbURL, username, password);
				System.out.println("Connecting to store: " + Thread.currentThread().getId());
				Store store = SDBFactory.connectStore(sdbConnection, storeDesc);
				System.out.println("creating dataset: " + Thread.currentThread().getId());
				Dataset dataset = SDBFactory.connectDataset(store);
				QuerySolutionMap sol = new QuerySolutionMap();
				sol.add("?2", ModelUtils.convertGraphNodeToRDFNode(Node.createURI("http://www.ins.cwi.nl/sib/forum/fo1928"), dataset.getDefaultModel()));
				sol.add("?0", ModelUtils.convertGraphNodeToRDFNode(Node.createURI("http://www.ins.cwi.nl/sib/user/u941"), dataset.getDefaultModel()));
				Query query = QueryFactory.create(qString);
				dataset.getDefaultModel().enterCriticalSection(Lock.READ);
				System.out.println("Started timer" + Thread.currentThread().getId());
				Timer t = Timer.timer();
				QueryExecution exec = QueryExecutionFactory.create(query , dataset, sol );
				ResultSet rs = exec.execSelect();
				ResultSetFormatter.out(rs) ;
				dataset.getDefaultModel().leaveCriticalSection();
				System.out.println("Query took: " + (t.duration() / 1000f) + "s");
			}

		},
		(ThreadPoolExecutor) Executors.newFixedThreadPool(4, new DaemonThreadFactory())
				);
	}

	private static void createTestDatabase() throws Throwable{
		SysRIOT.wireIntoJena();

		Connection connection;
		connection = DriverManager.getConnection(url, username, password);
		DatabaseMetaData meta = connection.getMetaData();
		java.sql.ResultSet sqlrs = meta.getCatalogs();
		List<String> list = new ArrayList<String>();
		while (sqlrs.next()) {
			String listofDatabases = sqlrs.getString("TABLE_CAT");
			list.add(listofDatabases);
		}
		if (list.contains(dbName)) {
			String hrappSQL = String.format("DROP DATABASE %s", dbName);
			Statement statement = connection.createStatement();
			statement.executeUpdate(hrappSQL);
		}
		StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.MySQL);
		String hrappSQL = String.format("CREATE DATABASE %s", dbName);
		Statement statement = connection.createStatement();
		statement.executeUpdate(hrappSQL);
		SDBConnection sdbConnection = new SDBConnection(dbURL, username, password);
		Store store = SDBFactory.connectStore(sdbConnection, storeDesc);
		store.getTableFormatter().create();
		String fileURL = "file:///Users/ss/Development/java/openimaj/trunk/tools/ReteStormTool/src/test/resources/osn_users.nt";
		Model tmpModel = ModelFactory.createDefaultModel();
		RiotLoader.read(fileURL, tmpModel.getGraph(), Lang.NTRIPLES);
		Dataset dataset = SDBFactory.connectDataset(store);
		dataset.getDefaultModel().add(tmpModel);
		store.close();
	}
}
