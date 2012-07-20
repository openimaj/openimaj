package org.openimaj.text.nlp.namedentity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.event.ListSelectionEvent;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.openimaj.text.nlp.namedentity.FourStoreClientTool.Node;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;

/**
 * Builds YagoCompanyIndex from various rdf sources.
 * @author laurence
 *
 */
public class YagoCompanyIndexFactory {
	
	/**
	 * This is the endpoint provided by the Yago developers.
	 */
	public static String YAGO_SPARQL_ENDPOINT = "http://lod.openlinksw.com/sparql";
	
	/**
	 * Method will attempt to create a Lucene index in memory from the Yago Developers sparql endpont.
	 * @return YagoCompanyIndex built from Yago rdf.
	 * @throws IOException
	 */
	public static YagoCompanyIndex createInMemory() throws IOException{
		return createFromSparqlEndPoint(YAGO_SPARQL_ENDPOINT, null);
	}

	/**
	 * Creates a YagoCompayIndex from a pre-built lucene index.
	 * @param pathToIndex = path to the pre-built index.
	 * @return YagoCompanyIndex built from yago rdf
	 * @throws IOException
	 */
	public static YagoCompanyIndex createFromExistingIndex(String pathToIndex)
			throws IOException {
		YagoCompanyIndex r = new YagoCompanyIndex();
		File f = new File(pathToIndex);
		r.index = FSDirectory.open(f);
		return r;
	}
	
	 	 
	/**
	 * Creates a YagoCompanyIndex from a remote 4store database.
	 * The underlying Index is created in the directory at indexPath.
	 * @param fourStore = remote url for 4store
	 * @param indexPath = path to directory for index to be built. Leave null to build in memory.
	 * @return YagoCompanyIndex built from yago rdf
	 * @throws IOException
	 */
	public static YagoCompanyIndex createFromRemoteFourStore(String fourStore,
			String indexPath) throws IOException {
		YagoCompanyIndex yci = new YagoCompanyIndex();
		// if indexPath null put it in memory
		if (indexPath == null) {
			yci.index = new RAMDirectory();
		} else {
			File f = new File(indexPath);
			if (f.isDirectory()) {
				yci.index = new SimpleFSDirectory(f);
			}
		}
		QuickIndexer qi = new QuickIndexer(yci.index);
		FourStoreClientTool fst = new FourStoreClientTool(fourStore);
		
		/*
		 * Get the companies
		 */
		String cn = "company";
		ArrayList<HashMap<String, Node>> wURIS = fst.query(wordnetCompanyQuery());
		ArrayList<HashMap<String, Node>> subURIS = fst.query(subClassWordnetCompanyQuery());
		HashMap<String,String> filter = new HashMap<String, String>();
		for(HashMap<String,Node> res:wURIS){
			filter.put(res.get("company").value, " ");
		}
		for(HashMap<String,Node> res:subURIS){
			filter.put(res.get("company").value, " ");
		}
		ArrayList<String> companyUris = new ArrayList<String>(filter.keySet());
		
		/*
		 * Get Alias and Context and put them in the index
		 */
		for (String uri : companyUris) {
			//Aliases
			StringBuffer aliases = new StringBuffer();
			for(HashMap<String, Node> res : fst.query(isCalledAlliasQuery(uri))){
				String a = res.get("alias").value;
				aliases.append(a.substring(0, a.indexOf("^^http")) + ", ");
			}
			for(HashMap<String, Node> res : fst.query(labelAlliasQuery(uri))){
				String a = res.get("alias").value;
				aliases.append(a.substring(0, a.indexOf("^^http")) + ", ");
			}
			//Context
			StringBuffer context = new StringBuffer();
			for(HashMap<String, Node> res : fst.query(ownsContextQuery(uri))){
				String a = res.get("context").value;
				context.append(a.substring(0, a.indexOf("^^http")) + ", ");
			}
			for(HashMap<String, Node> res : fst.query(createdContextQuery(uri))){
				String a = res.get("context").value;
				context.append(a.substring(0, a.indexOf("^^http")) + ", ");
			}
			String[] values = {
					uri.substring(uri.lastIndexOf("/") + 1)
							.replaceAll("_", " ").trim(), aliases.toString(),
					context.toString() };
			qi.addDocumentFromFields(yci.names, values, yci.types);
		}
		qi.finalise();
		return yci;
	}

	/**
	 * Creates a YagoCompanyIndex from a locally held .rdfs file.
	 * The underlying Index is created in the directory at indexPath.
	 * @param rdfsPath = path to .rdfs file;
	 * @param indexPath = path to directory for index to be built. Leave null to build in memory.
	 * @return YagoCompanyIndex built from yago rdf
	 * @throws IOException
	 */
	public static YagoCompanyIndex createFromLocalRDFS(String rdfsPath,
			String indexPath) throws IOException {
		YagoCompanyIndex yci = new YagoCompanyIndex();
		// if indexPath null put it in memory
		if (indexPath == null) {
			yci.index = new RAMDirectory();
		} else {
			File f = new File(indexPath);
			if (f.isDirectory()) {
				yci.index = new SimpleFSDirectory(f);
			} else
				throw new IOException("indexPath error");
		}
		// create an empty model
		Model model = ModelFactory.createDefaultModel();

		// use the FileManager to find the input file
		InputStream in = FileManager.get().open(rdfsPath);
		if (in == null) {
			throw new IllegalArgumentException("File: " + rdfsPath
					+ " not found");
		}
		// read the RDF/XML file
		model.read(in, null);
		jenaBuild(model,null,yci);
		return yci;
	}	
	

	/**
	 * Creates a YagoCompanyIndex from a remote sparql endpoint.
	 * The underlying Index is created in the directory at indexPath.
	 * @param endPoint = remote sparql endpoint uri.
	 * @param indexPath = path to directory for index to be built. Leave null to build in memory.
	 * @return YagoCompanyIndex built from yago rdf
	 * @throws IOException
	 */
	public static YagoCompanyIndex createFromSparqlEndPoint(String endPoint,
			String indexPath) throws IOException {
		YagoCompanyIndex yci = new YagoCompanyIndex();
		// if indexPath null put it in memory
		if (indexPath == null) {
			yci.index = new RAMDirectory();
		} else {
			File f = new File(indexPath);
			if (f.isDirectory()) {
				yci.index = new SimpleFSDirectory(f);
			} else
				throw new IOException("indexPath error");
		}
		jenaBuild(null, endPoint, yci);
		return yci;
	}
	
	private static void jenaBuild(Model model,  String endPoint, YagoCompanyIndex yci) throws CorruptIndexException, IOException{
		/*
		 * Get the companies
		 */
		ArrayList<String> companyUris = new ArrayList<String>();
		Query q = QueryFactory.create(wordnetCompanyQuery());
		QueryExecution qexec;
		if(model==null) qexec=QueryExecutionFactory.sparqlService(endPoint, q);
		else qexec = QueryExecutionFactory.create(q, model);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				String uri = soln.getResource("company").getURI();
				companyUris.add(uri);
			}

		} finally {
			qexec.close();
		}
		q = QueryFactory.create(subClassWordnetCompanyQuery());
		if(model==null) qexec=QueryExecutionFactory.sparqlService(endPoint, q);
		else qexec = QueryExecutionFactory.create(q, model);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				String uri = soln.getResource("company").getURI();
				if (!companyUris.contains(uri))
					companyUris.add(uri);
			}
		} finally {
			qexec.close();
		}

		/*
		 * Get Alias and Context and put them in the index
		 */
		QuickIndexer qi = new QuickIndexer(yci.index);

		for (String uri : companyUris) {
			//Aliases
			StringBuffer aliases = new StringBuffer();
			q = QueryFactory.create(isCalledAlliasQuery(uri));
			if(model==null) qexec=QueryExecutionFactory.sparqlService(endPoint, q);
			else qexec = QueryExecutionFactory.create(q, model);
			try {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext();) {
					QuerySolution soln = results.nextSolution();
					String a = soln.getLiteral("alias").toString();
					aliases.append(a.substring(0, a.indexOf("^^http")) + ", ");
				}
			} finally {
				qexec.close();
			}
			q = QueryFactory.create(labelAlliasQuery(uri));
			if(model==null) qexec=QueryExecutionFactory.sparqlService(endPoint, q);
			else qexec = QueryExecutionFactory.create(q, model);
			try {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext();) {
					QuerySolution soln = results.nextSolution();
					String a = soln.getLiteral("alias").toString();
					aliases.append(a.substring(0, a.indexOf("^^http")) + ", ");
				}
			} finally {
				qexec.close();
			}
			//Context
			StringBuffer context = new StringBuffer();
			q = QueryFactory.create(ownsContextQuery(uri));
			if(model==null) qexec=QueryExecutionFactory.sparqlService(endPoint, q);
			else qexec = QueryExecutionFactory.create(q, model);
			try {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext();) {
					QuerySolution soln = results.nextSolution();
					String a = soln.getLiteral("context").toString();
					context.append(a.substring(0, a.indexOf("^^http")) + ", ");
				}
			} finally {
				qexec.close();
			}
			q = QueryFactory.create(createdContextQuery(uri));
			if(model==null) qexec=QueryExecutionFactory.sparqlService(endPoint, q);
			else qexec = QueryExecutionFactory.create(q, model);
			try {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext();) {
					QuerySolution soln = results.nextSolution();
					String a = soln.getResource("context").toString().substring(uri.lastIndexOf("/") + 1)
							.replaceAll("_", " ").trim();
					context.append(a + ", ");
				}
			} finally {
				qexec.close();
			}
			String[] values = {
					uri.substring(uri.lastIndexOf("/") + 1)
							.replaceAll("_", " ").trim(), aliases.toString(),
					context.toString() };
			qi.addDocumentFromFields(yci.names, values, yci.types);
		}
		qi.finalise();
	}
	
	/*
	 * Methods to return paramatised sparql query strings.
	 */

	private static String isCalledAlliasQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT ?alias WHERE {"
				+ " <"
				+ companyURI
				+ "> ?p <http://yago-knowledge.org/resource/wordnet_company_108058098> . "
				+ "?fact rdf:predicate <http://yago-knowledge.org/resource/isCalled> ."
				+ "?fact rdf:object   ?alias ." + "?fact rdf:subject <"
				+ companyURI + "> }";
	}

	private static String labelAlliasQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?alias WHERE {"
				+ " <"
				+ companyURI
				+ "> rdfs:label ?alias ."
				+ " <"
				+ companyURI
				+ "> ?p <http://yago-knowledge.org/resource/wordnet_company_108058098> . "
				+ "}";
	}

	private static String wordnetCompanyQuery() {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?company WHERE {"
				+ " ?company rdf:type <http://yago-knowledge.org/resource/wordnet_company_108058098> . "
				+ "}";
	}

	private static String subClassWordnetCompanyQuery() {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?company WHERE {"
				+ " ?subclass rdfs:subClassOf <http://yago-knowledge.org/resource/wordnet_company_108058098> . "
				+ " ?company rdf:type ?subclass . " + "}";
	}

	private static String ownsContextQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?context WHERE { " +
				"?fact rdf:object <"+companyURI+"> . " +
				"?fact rdf:predicate owns ." +
				"?fact rdf:subject ?context}";
	}

	private static String createdContextQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?context WHERE {" + 
				"?fact rdf:subject <"+companyURI+"> . " +
				"?fact rdf:predicate <http://yago-knowledge.org/resource/created> ." +
				"?fact rdf:object ?context}";
	}
	
	private static String anchorContextQuery(String companyURI) {
		return "PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
				+ "SELECT ?context WHERE {" + 
				"<"+companyURI+"> <http://yago-knowledge.org/resource/hasWikipediaAnchorText> ?context " +
				"}";
	}
	
	public static void main(String[] args){
		String apple = "http://yago-knowledge.org/resource/Apple_Inc.";
		//System.out.println(isCalledAlliasQuery(apple)); //works
		//System.out.println(labelAlliasQuery(apple)); //works
		//System.out.println(wordnetCompanyQuery()); //works
		//System.out.println(subClassWordnetCompanyQuery()); //works
		//System.out.println(ownsContextQuery(apple)); /** Does not work **/
		//System.out.println(createdContextQuery(apple)); //works
		//System.out.println(anchorContextQuery(apple)); /** Does not work **/
	}

	/**
	 * Class that uses an underlying lucene index to match tokens to companies.
	 * Use the enclosing factory class to instantiate.
	 * @author laurence
	 *
	 */
	public static class YagoCompanyIndex {

		private Directory index = null;
		private String[] names = { "Company", "Aliases", "Context" };
		private FieldType[] types;

		private YagoCompanyIndex() {
			FieldType ti = new FieldType();
			ti.setIndexed(true);
			ti.setTokenized(true);
			ti.setStored(true);
			FieldType n = new FieldType();
			n.setStored(true);
			types = new FieldType[3];
			types[0] = n;
			types[1] = ti;
			types[2] = ti;
		}

		/**
		 * Returns candidate companies for the single token provided.
		 * @param token = single possible named entity token.
		 * @return candidate companies
		 */
		public ArrayList<String> getCompanyListFromAliasToken(String token) {
			QuickSearcher qs = new QuickSearcher(index, new StandardAnalyzer(
					Version.LUCENE_40));
			ArrayList<String> results = new ArrayList<String>();
			try {
				// search on the alliases field
				ArrayList<HashMap<String, String>> companies = qs.search(
						names[1], token, 10);
				for (HashMap<String, String> hashMap : companies) {
					// put the value of the company field into results
					results.add(hashMap.get(names[0]));
				}
			} catch (ParseException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			return results;
		}
		
		/**
		 * Class returns candidate companies based on the whole string.
		 * @param contextString = can be untokenised.
		 * @return candidate companies
		 */
		public ArrayList<String> getCompanyListFromContext(String contextString){
			QuickSearcher qs = new QuickSearcher(index, new StandardAnalyzer(
					Version.LUCENE_40));
			ArrayList<String> results = new ArrayList<String>();
			try {
				// search on the alliases field
				ArrayList<HashMap<String, String>> companies = qs.search(
						names[1], contextString, 10);
				for (HashMap<String, String> hashMap : companies) {
					// put the value of the company field into results
					results.add(hashMap.get(names[0]));
				}
			} catch (ParseException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
			return results;
		}
	}
}
