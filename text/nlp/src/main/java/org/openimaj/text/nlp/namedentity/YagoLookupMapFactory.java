package org.openimaj.text.nlp.namedentity;

import java.io.BufferedReader;
import java.io.DataInputStream;
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

/**
 * Construct an organisation alias lookup using the yago ontology
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class YagoLookupMapFactory {

	private boolean verbose = false;
	private int uniqueCount = 0;

	/**
	 * @param verbose
	 *            whether the printing should be verbose
	 */
	public YagoLookupMapFactory(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Construct an alias lookup using an endpoint and the output file
	 * 
	 * @param endPoint
	 * @param listFile
	 * @throws IOException
	 */
	public void createListFileFromSparqlEndpoint(String endPoint, String listFile) throws IOException {
		FileWriter w = null;
		w = new FileWriter(listFile);
		/*
		 * Get the companies
		 */
		print("Getting Entity Set...");
		final HashSet<String> companyUris = getCompanyUris(endPoint);
		print("Total Entities: " + uniqueCount);
		/*
		 * Get Aliases
		 */
		int count = 0;
		for (final String uri : companyUris) {
			count++;
			if ((count % 1000) == 0)
				print("Processed " + count + " out of " + uniqueCount);
			// Aliases
			final ArrayList<String> aliases = getAliases(uri, endPoint);
			try {
				w.write("+" + uri + "\n");
				for (final String alias : aliases) {
					w.write("." + alias + "\n");
				}
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			w.flush();
			w.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Construct a lookup and output the lookup as a hashmap
	 * 
	 * @param endPoint
	 * @return the lookup hashmap
	 */
	public HashMap<String, ArrayList<String>> createFromSparqlEndpoint(String endPoint) {
		final HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		/*
		 * Get the companies
		 */
		print("Getting Entity Set...");
		final HashSet<String> companyUris = getCompanyUris(endPoint);
		print("Total Entities: " + uniqueCount);

		/*
		 * Get Alias
		 */
		int count = 0;
		for (final String uri : companyUris) {
			count++;
			if ((count % 1000) == 0)
				print("Processed " + count + " out of " + uniqueCount);
			// Aliases
			final ArrayList<String> aliases = getAliases(uri, endPoint);
			for (final String alias : aliases) {
				final String company = YagoQueryUtils.yagoResourceToString(uri);
				if (result.keySet().contains(alias)) {
					result.get(alias).add(company);
				} else {
					final ArrayList<String> c = new ArrayList<String>();
					c.add(company);
					result.put(alias, c);
				}
			}
		}
		return result;
	}

	/**
	 * Read a lookup from an output file
	 * 
	 * @param listFile
	 * @return the lookup hashmap
	 * @throws IOException
	 */
	public HashMap<String, ArrayList<String>> createFromListFile(String listFile) throws IOException {
		final HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		int companies = 0;
		int aliasToMany = 0;
		int uniguqAliases = 0;
		final InputStream fstream = new FileInputStream(listFile);
		// Get the object of DataInputStream
		final DataInputStream in = new DataInputStream(fstream);
		final BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine = br.readLine();
		// Read File Line By Line
		String companyUri = null;
		// final HashSet<String> allUris = new HashSet<String>();
		while (strLine != null) {
			if (!strLine.startsWith("+") && !strLine.startsWith(".")) {
				strLine = br.readLine();
				continue;
			}
			if (strLine.startsWith("+")) {
				companyUri = strLine.substring(1);
				strLine = br.readLine();
				companies++;
			}
			while (strLine != null && strLine.startsWith(".")) {
				final String alias = strLine.substring(1);
				final String company = YagoQueryUtils.yagoResourceToString(companyUri);
				if (result.containsKey(alias)) {
					if (!result.get(alias).contains(company)) {
						result.get(alias).add(company);
						aliasToMany++;
					}
				} else {
					final ArrayList<String> comps = new ArrayList<String>();
					comps.add(company);
					result.put(alias, comps);
					uniguqAliases++;
				}
				strLine = br.readLine();
			}
		}
		// Close the input stream
		in.close();
		print("Companies :" + companies);
		print("oneToMany aliases : " + aliasToMany);
		print("Unique aliases : " + uniguqAliases);
		return result;
	}

	private HashSet<String> getCompanyUris(String endPoint) {
		final SparqlTransitiveClosure st = new SparqlTransitiveClosure(endPoint);
		final HashSet<String> companyUris = new HashSet<String>();
		for (final String uri : YagoQueryUtils.WORDNET_ORGANISATION_ROOT_URIS) {
			print("Getting from: " + uri);
			companyUris.addAll(st.getAllTransitiveLeavesOf(uri, "rdfs:subClassOf", "rdf:type"));
		}
		uniqueCount = companyUris.size();
		return companyUris;
	}

	private ArrayList<String> getAliases(String uri, String endPoint) {
		final ArrayList<String> aliases = new ArrayList<String>();
		final Query q = QueryFactory.create(YagoQueryUtils.isCalledAlliasQuery(uri));
		final QueryExecution qexec = QueryExecutionFactory.sparqlService(endPoint, q);
		try {
			final ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				final QuerySolution soln = results.nextSolution();
				final String a = soln.getLiteral("alias").toString();
				aliases.add(YagoQueryUtils.yagoLiteralToString(a));
			}
		} finally {
			qexec.close();
		}
		final SparqlQueryPager sqp = new SparqlQueryPager(endPoint);
		final ArrayList<QuerySolution> qresults = sqp.pageQuery(YagoQueryUtils.labelAlliasQuery(uri));
		for (final QuerySolution soln : qresults) {
			final String a = soln.getLiteral("alias").toString();
			aliases.add(YagoQueryUtils.yagoLiteralToString(a));
		}
		return aliases;
	}

	private void print(String message) {
		if (verbose)
			System.out.println(message);
	}
}
