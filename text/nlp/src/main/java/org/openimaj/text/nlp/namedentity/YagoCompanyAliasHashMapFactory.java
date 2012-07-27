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
import java.util.Set;

import org.openimaj.io.FileUtils;
import org.openimaj.text.nlp.namedentity.YagoCompanyIndexFactory.YagoCompanyIndex;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

public class YagoCompanyAliasHashMapFactory {

	public static String aliasListFile = "/org/openimaj/text/namedentity/yagomaps/alias.txt";
	public static boolean verbose = false;

	/**
	 * Validate the (local) ouput from an String and return the corresponding
	 * file.
	 * 
	 * @param out
	 *            where the file will go
	 * @param overwrite
	 *            whether to overwrite existing files
	 * @param contin
	 *            whether an existing output should be continued (i.e. ignored
	 *            if it exists)
	 * @return the output file location, deleted if it is allowed to be deleted
	 * @throws IOException
	 *             if the file exists, but can't be deleted
	 */
	public static File validateLocalOutput(String out, boolean overwrite,
			boolean contin) throws IOException {
		if (out == null) {
			throw new IOException("No output specified");
		}
		File output = new File(out);
		if (output.exists()) {
			if (overwrite) {
				if (!FileUtils.deleteRecursive(output))
					throw new IOException("Couldn't delete existing output");
			} else if (!contin) {
				throw new IOException("Output already exists, didn't remove");
			}
		}
		return output;
	}

	public static void createListFileFromSparqlEndpoint(String endPoint) {
		File f = null;
		FileWriter w = null;
		try {
			f = validateLocalOutput(YagoCompanyAliasHashMapFactory.class.getResource(aliasListFile).getFile(), true, false);
			f.createNewFile();
			w = new FileWriter(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (f != null && w != null) {

			/*
			 * Get the companies
			 */
			HashSet<String> companyUris = new HashSet<String>();
			Query q = QueryFactory.create(YagoCompanyIndexFactory
					.wordnetCompanyQuery());
			QueryExecution qexec = QueryExecutionFactory.sparqlService(
					endPoint, q);
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
			SparqlQueryPager sqp = new SparqlQueryPager(endPoint);
			
			ArrayList<QuerySolution> qresults = sqp.pageQuery(YagoCompanyIndexFactory
					.subClassWordnetCompanyQuery());
			for (QuerySolution soln : qresults) {				
				String uri = soln.getResource("company").getURI();
				companyUris.add(uri);
			}
			System.out.println("companies: "+companyUris.size());
					
				
			
			/*
			 * Get Aliases
			 */
			for (String uri : companyUris) {
				// Aliases
				ArrayList<String> aliases = new ArrayList<String>();
				q = QueryFactory.create(YagoCompanyIndexFactory
						.isCalledAlliasQuery(uri));
				qexec = QueryExecutionFactory.sparqlService(endPoint, q);
				try {
					ResultSet results = qexec.execSelect();
					for (; results.hasNext();) {
						QuerySolution soln = results.nextSolution();
						String a = soln.getLiteral("alias").toString();
						aliases.add(a.substring(0, a.indexOf("^^http")));
					}
				} finally {
					qexec.close();
				}
				
				
				qresults = sqp.pageQuery(YagoCompanyIndexFactory
						.labelAlliasQuery(uri));
				for (QuerySolution soln : qresults) {				
					String a = soln.getLiteral("alias").toString();
					aliases.add(a.substring(0, a.indexOf("^^http")));
				}
				
				/*q = QueryFactory.create(YagoCompanyIndexFactory
						.labelAlliasQuery(uri));
				qexec = QueryExecutionFactory.sparqlService(endPoint, q);
				try {
					ResultSet results = qexec.execSelect();
					for (; results.hasNext();) {
						QuerySolution soln = results.nextSolution();
						String a = soln.getLiteral("alias").toString();
						aliases.add(a.substring(0, a.indexOf("^^http")));
					}
				} finally {
					qexec.close();
				}*/
				
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
		}
		try {
			w.flush();
			w.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static HashMap<String, String> createFromSparqlEndpoint(
			String endPoint) {
		HashMap<String, String> result = new HashMap<String, String>();
		/*
		 * Get the companies
		 */
		HashMap<String, String> companyUris = new HashMap<String, String>();
		Query q = QueryFactory.create(YagoCompanyIndexFactory
				.wordnetCompanyQuery());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endPoint, q);
		try {
			ResultSet results = qexec.execSelect();
			for (; results.hasNext();) {
				QuerySolution soln = results.nextSolution();
				String uri = soln.getResource("company").getURI();
				String company = uri.substring(uri.lastIndexOf("/") + 1)
						.replaceAll("_", " ").trim();
				if (companyUris.keySet().contains(company)) {
					if (uri.equals(companyUris.get(company)))
						print("Duplicate names : " + companyUris.get(company)
								+ " : " + uri);
				} else
					companyUris.put(company, uri);
			}

		} finally {
			qexec.close();
		}

		/*
		 * Get Alias
		 */
		int collisions = 0;
		for (String uri : companyUris.values()) {
			// Aliases
			ArrayList<String> aliases = new ArrayList<String>();
			q = QueryFactory.create(YagoCompanyIndexFactory
					.isCalledAlliasQuery(uri));
			qexec = QueryExecutionFactory.sparqlService(endPoint, q);
			try {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext();) {
					QuerySolution soln = results.nextSolution();
					String a = soln.getLiteral("alias").toString();
					aliases.add(a.substring(0, a.indexOf("^^http")));
				}
			} finally {
				qexec.close();
			}
			q = QueryFactory.create(YagoCompanyIndexFactory
					.labelAlliasQuery(uri));
			qexec = QueryExecutionFactory.sparqlService(endPoint, q);
			try {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext();) {
					QuerySolution soln = results.nextSolution();
					String a = soln.getLiteral("alias").toString();
					aliases.add(a.substring(0, a.indexOf("^^http")));
				}
			} finally {
				qexec.close();
			}

			for (String alias : aliases) {
				String company = uri.substring(uri.lastIndexOf("/") + 1)
						.replaceAll("_", " ").trim();
				if (result.keySet().contains(alias)
						&& !result.get(alias).equals(company)) {
					print("Collision: " + alias + " to :" + company + " and :"
							+ result.get(alias));
					collisions++;
				} else
					result.put(alias, company);
			}
		}
		print("Collisions : " + collisions);
		return result;
	}

	public static HashMap<String, ArrayList<String>> createFromListFile() {
		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		int dupCompanies = 0;
		int aliasToMany = 0;
		int uniqueComps = 0;
		int uniguqAliases = 0;
		try {
			// Open the file that is the first
			// command line parameter
			InputStream fstream = YagoCompanyAliasHashMapFactory.class.getResourceAsStream(aliasListFile);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine = br.readLine();
			// Read File Line By Line
			String companyUri = null;

			HashSet<String> allUris = new HashSet<String>();
			while (strLine != null) {
				if (!strLine.startsWith("+") && !strLine.startsWith("."))
					strLine = br.readLine();
				if (strLine.startsWith("+")) {
					print("Found Company...");
					companyUri = strLine.substring(1);
					if (!allUris.contains(companyUri)) {
						allUris.add(companyUri);
						uniqueComps++;
					} else
						dupCompanies++;
					strLine = br.readLine();
				}
				while (strLine != null && strLine.startsWith(".")) {

					String alias = strLine.substring(1);
					print("    processing Alias..." + alias);
					String company = uriToName(companyUri);
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
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		print("Duplicate companies : " + dupCompanies);
		print("Unique companies : " + uniqueComps);
		print("oneToMany aliases : " + aliasToMany);
		print("Unique aliases : " + uniguqAliases);
		return result;
	}

	public static void main(String[] args) {

		// HashMap<String,String> aliasmap =
		// YagoCompanyAliasHashMapFactory.createFromSparqlEndpoint(YagoCompanyIndexFactory.YAGO_SPARQL_ENDPOINT);

		YagoCompanyAliasHashMapFactory.createListFileFromSparqlEndpoint(
				YagoCompanyIndexFactory.YAGO_SPARQL_ENDPOINT);

		/*
		 * HashMap<String, ArrayList<String>> map =
		 * YagoCompanyAliasHashMapFactory
		 * .createFromListFile(YagoCompanyAliasHashMapFactory.aliasListFile);
		 */
	}

	private static String uriToName(String uri) {
		return uri.substring(uri.lastIndexOf("/") + 1).replaceAll("_", " ")
				.trim();
	}

	private static void print(String message) {
		if (verbose)
			System.out.println(message);
	}

}
