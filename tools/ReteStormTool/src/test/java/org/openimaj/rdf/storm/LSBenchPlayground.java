package org.openimaj.rdf.storm;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.openimaj.io.FileUtils;
import org.openimaj.rdf.storm.utils.CsparqlUtils;
import org.openjena.riot.Lang;
import org.openjena.riot.RiotLoader;
import org.openjena.riot.SysRIOT;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.graph.compose.Polyadic;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBConnectionFactory;
import com.hp.hpl.jena.sdb.store.DatabaseType;
import com.hp.hpl.jena.sdb.store.LayoutType;

public class LSBenchPlayground {
	private static String driver = "com.mysql.jdbc.Driver";
	static {
		try {
			Class.forName(driver);
			System.out.println("JDBC driver load successfully!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		// Load the LSBench posts into a model
		SysRIOT.wireIntoJena();
		File streamFile = new File("/Users/ss/Experiments/retestormlsbench/data/1000/rdfPostStream1000.ntriples");
		URL fileURL = streamFile.toURI().toURL();
		Graph streamGraph = RiotLoader.loadGraph(fileURL.toString(), Lang.NTRIPLES);

		//		File staticFile = new File("/Users/ss/Experiments/retestormlsbench/data/1000/mr0_sibdataset1000.ntriples");
		//		fileURL = staticFile.toURI().toURL();
		//		Graph staticGraph = RiotLoader.loadGraph(fileURL.toString(), Lang.NTRIPLES);
		StoreDesc storeDesc = new StoreDesc(LayoutType.LayoutTripleNodesIndex, DatabaseType.MySQL);
		System.out.println("Creating SDBConnection: " + Thread.currentThread().getId());
		final SDBConnection sdbConnection = SDBConnectionFactory.create("jdbc:mysql://localhost:3306/jeandb_users_1000", "root", "");
		System.out.println("Connecting to store: " + Thread.currentThread().getId());
		Store store = SDBFactory.connectStore(sdbConnection, storeDesc);
		System.out.println("creating dataset: " + Thread.currentThread().getId());
		Dataset dataset = SDBFactory.connectDataset(store);

		Polyadic graph = new MultiUnion(new Graph[] { streamGraph, dataset.getDefaultModel().getGraph() });
		String queryStr = FileUtils.readall(LSBenchPlayground.class.getResourceAsStream("/lsbench/query3.csparql"));
		queryStr = CsparqlUtils.templaceQuery(queryStr, "replace", "TEST_FILE");
		Query q = CsparqlUtils.parse(queryStr).simpleQuery;
		QueryExecution exec = QueryExecutionFactory.create(q, ModelFactory.createModelForGraph(graph));
		ResultSet rs = exec.execSelect();
		ResultSetFormatter.out(rs);
	}
}
