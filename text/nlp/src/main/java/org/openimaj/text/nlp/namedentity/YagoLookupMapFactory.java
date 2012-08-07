package org.openimaj.text.nlp.namedentity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class YagoLookupMapFactory {

	private boolean verbose = false;
	private int uniqueCount = 0;

	public YagoLookupMapFactory(boolean verbose) {
		this.verbose = verbose;
	}

	public void createListFileFromSparqlEndpoint(String endPoint,
			String listFile) throws IOException {
		File f = null;
		FileWriter w = null;
		w = new FileWriter(listFile);
		/*
		 * Get the companies
		 */
		print("Getting Entity Set...");
		HashSet<String> companyUris = getCompanyUris(endPoint);
		print("Total Entities: "+uniqueCount);
		/*
		 * Get Aliases
		 */
		int count = 0;
		for (String uri : companyUris) {
			count++;
			if ((count % 1000) == 0)
				print("Processed " + count + " out of " + uniqueCount);
			// Aliases
			ArrayList<String> aliases = getAliases(uri, endPoint);
			try {
				w.write("+" + uri + "\n");
				for (String alias : aliases) {
					w.write("." + alias + "\n");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			w.flush();
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public HashMap<String, ArrayList<String>> createFromSparqlEndpoint(
			String endPoint) {
		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		/*
		 * Get the companies
		 */
		print("Getting Entity Set...");
		HashSet<String> companyUris = getCompanyUris(endPoint);
		print("Total Entities: "+uniqueCount);

		/*
		 * Get Alias
		 */
		int count = 0;
		for (String uri : companyUris) {
			count++;
			if ((count % 1000) == 0)
				print("Processed " + count + " out of " + uniqueCount);
			// Aliases
			ArrayList<String> aliases = getAliases(uri, endPoint);
			for (String alias : aliases) {
				String company = YagoQueryUtils.yagoResourceToString(uri);
				if (result.keySet().contains(alias)) {
					result.get(alias).add(company);
				} else {
					ArrayList<String> c = new ArrayList<String>();
					c.add(company);
					result.put(alias, c);
				}
			}
		}
		return result;
	}

	public HashMap<String, ArrayList<String>> createFromListFile(String listFile)
			throws IOException {
		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		int companies = 0;
		int aliasToMany = 0;		
		int uniguqAliases = 0;
		InputStream fstream = new FileInputStream(listFile);
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine = br.readLine();
		// Read File Line By Line
		String companyUri = null;
		HashSet<String> allUris = new HashSet<String>();
		while (strLine != null) {
			if (!strLine.startsWith("+") && !strLine.startsWith(".")){
				strLine = br.readLine();
				continue;
			}
			if (strLine.startsWith("+")) {				
				companyUri = strLine.substring(1);				
				strLine = br.readLine();
				companies++;
			}
			while (strLine != null && strLine.startsWith(".")) {
				String alias = strLine.substring(1);				
				String company = YagoQueryUtils
						.yagoResourceToString(companyUri);
				if (result.containsKey(alias)) {
					if (!result.get(alias).contains(company)) {
						result.get(alias).add(company);
						aliasToMany++;
					}
				} else {
					ArrayList<String> comps = new ArrayList<String>();
					comps.add(company);
					result.put(alias, comps);
					uniguqAliases++;
				}
				strLine = br.readLine();
			}
		}
		// Close the input stream
		in.close();
		print("Companies :"+companies);
		print("oneToMany aliases : " + aliasToMany);
		print("Unique aliases : " + uniguqAliases);
		return result;
	}

	private HashSet<String> getCompanyUris(String endPoint) {
		SparqlTransitiveClosure st = new SparqlTransitiveClosure(endPoint);
		HashSet<String> companyUris = new HashSet<String>();
		for(String uri : YagoQueryUtils.WORDNET_ORGANISATION_ROOT_URIS){
			print("Getting from: "+uri);
			companyUris.addAll(st.getAllTransitiveLeavesOf(uri, "rdfs:subClassOf", "rdf:type"));
		}
		uniqueCount=companyUris.size();
		return companyUris;
	}

	private ArrayList<String> getAliases(String uri, String endPoint) {
		ArrayList<String> aliases = new ArrayList<String>();
		Query q = QueryFactory.create(YagoQueryUtils.isCalledAlliasQuery(uri));
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endPoint, q);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				String a = soln.getLiteral("alias").toString();
				aliases.add(YagoQueryUtils.yagoLiteralToString(a));
			}
		} finally {
			qexec.close();
		}
		SparqlQueryPager sqp = new SparqlQueryPager(endPoint);
		ArrayList<QuerySolution> qresults = sqp.pageQuery(YagoQueryUtils
				.labelAlliasQuery(uri));
		for (QuerySolution soln : qresults) {
			String a = soln.getLiteral("alias").toString();
			aliases.add(YagoQueryUtils.yagoLiteralToString(a));
		}
		return aliases;
	}

	private void print(String message) {
		if (verbose)
			System.out.println(message);
	}
}
