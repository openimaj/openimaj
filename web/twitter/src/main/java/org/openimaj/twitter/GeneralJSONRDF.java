package org.openimaj.twitter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.openimaj.io.FileUtils;
import org.openimaj.twitter.USMFStatus.User;
import org.openjena.riot.SysRIOT;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.QuerySolutionMap;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * Holds an internal Jena Graph of the USMF status. The default language used is
 * NTriples
 *
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class GeneralJSONRDF extends GeneralJSON {
	private static final String ITEM_QUERY_FILE = "/org/openimaj/twitter/usmf_query.sparql";
	private static final String INSERT_DATA = "INSERT DATA";
	private static Query itemQuery;
	private static Map<String, Query> queryCache;

	static {
		SysRIOT.wireIntoJena();
	}

	private Model m;

	public GeneralJSONRDF() {


	}

	@Override
	public void readASCII(final Scanner in) throws IOException {
		StringBuffer b = new StringBuffer();
		while (in.hasNext()) {
			b.append(in.next());
		}
		InputStream stream = new ByteArrayInputStream(b.toString().getBytes("UTF-8"));
		m = ModelFactory.createDefaultModel();
		m.read(stream, "", "NTRIPLES");
		m.close();
	}

	@Override
	public void fillUSMF(USMFStatus status) {
		// Read the top level item query
		ResultSet itemQuery = QueryExecutionFactory.create(queryCache(ITEM_QUERY_FILE), m).execSelect();
		if(!itemQuery.hasNext()) return;
		QuerySolution qs = itemQuery.next();
		RDFNode se = qs.get("?socialEvent");
		QuerySolutionMap qsm = new QuerySolutionMap();
		qsm.addAll(qs);
		RDFNode user = qsm.get("?user");
		if(user != null){

		}
		status.user = new User();
		// Perform the SPARQL
	}

	private static Query queryCache(String queryFile) {
		if(queryCache == null){
			queryCache = new HashMap<String,Query>();
		}
		Query q = queryCache.get(queryFile);
		if(q == null){
			queryCache.put(queryFile,q = QueryFactory.create(readQuery(queryFile)));
		}
		return q;
	}

	private static String readQuery(String qf) {
		try {
			return FileUtils.readall(GeneralJSONRDF.class.getResourceAsStream(qf));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void fromUSMF(USMFStatus status) {
		prepareModel();
//		m.add(
//			ResourceFactory.createResource("dc:wangSub"),
//			ResourceFactory.createProperty("dc:wangPre"),
//			"wangObj"
//		);
		UpdateRequest up = constructInsertLiteral("dc:wangSub","dc:wangPre", "cheese");
		UpdateAction.execute(up, m);
		m.write(System.out);
	}

	private UpdateRequest constructInsertLiteral(String s, String p, String o) {

		return constructInsert(String.format("<%s> %s \"%s\"",s , p ,o));
	}

	private UpdateRequest constructInsert(String insert) {

		UpdateRequest up = new UpdateRequest();
		up.setPrefixMapping(m);
		UpdateFactory.parse(up,INSERT_DATA + "{ " + insert + " }");
		return up;
	}

	private void prepareModel() {
		m = ModelFactory.createDefaultModel();
		m.read(GeneralJSONRDF.class.getResourceAsStream("rdf/base_usmf.rdf"), "");
	}

}
