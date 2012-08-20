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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openimaj.text.nlp.namedentity.NGramGenerator.StringNGramGenerator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Factory object for : -creating YagoEntityCandidateFinders in various ways.
 * -creating Yago Entity Alias text files in various ways.
 */
public class YagoEntityCandidateFinderFactory {

	private boolean verbose = false;
	private int uniqueCount = 0;

	/**
	 * Default constructor
	 * 
	 * @param verbose
	 *            = true to print everything
	 */
	public YagoEntityCandidateFinderFactory(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Creates a text file of Entity Aliases from a given Yago SparqlEndpoint.
	 * 
	 * @param endPoint
	 *            = url of endpoint.
	 * @param listFile
	 *            = location of the text file
	 * @throws IOException
	 */
	public void createMapFileFromSparqlEndpoint(String endPoint, String listFile)
			throws IOException {
		FileWriter w = null;
		w = new FileWriter(listFile);
		/*
		 * Get the companies
		 */
		print("Getting Entity Set...");
		HashSet<String> companyUris = getCompanyUris(endPoint);
		print("Total Entities: " + uniqueCount);
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
			w.write("+" + uri + "\n");
			for (String alias : aliases) {
				w.write("." + alias + "\n");
			}
		}
		w.flush();
		w.close();
	}

	/**
	 * Returns a {@link YagoEntityCandidateFinder} given the url of a sparql
	 * endpoint.
	 * 
	 * @param endPoint
	 * @return {@link YagoEntityCandidateFinder}
	 */
	public YagoEntityCandidateFinder createFromSparqlEndpoint(String endPoint) {
		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		/*
		 * Get the companies
		 */
		print("Getting Entity Set...");
		HashSet<String> companyUris = getCompanyUris(endPoint);
		print("Total Entities: " + uniqueCount);

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
		return new YagoEntityCandidateFinder(result);
	}

	/**
	 * Returns a {@link YagoEntityCandidateFinder} given a path Yago Entity
	 * Alias textfile
	 * 
	 * @param pathToAliasFile
	 * @return {@link YagoEntityCandidateFinder}
	 * @throws IOException
	 */
	public YagoEntityCandidateFinder createFromAliasFile(String pathToAliasFile)
			throws IOException {
		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		int companies = 0;
		int aliasToMany = 0;
		int uniguqAliases = 0;
		InputStream fstream = new FileInputStream(pathToAliasFile);
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine = br.readLine();
		// Read File Line By Line
		String companyUri = null;
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
		print("Companies :" + companies);
		print("oneToMany aliases : " + aliasToMany);
		print("Unique aliases : " + uniguqAliases);
		return new YagoEntityCandidateFinder(result);
	}

	private HashSet<String> getCompanyUris(String endPoint) {
		SparqlTransitiveClosure st = new SparqlTransitiveClosure(endPoint);
		HashSet<String> companyUris = new HashSet<String>();
		for (String uri : YagoQueryUtils.WORDNET_ORGANISATION_ROOT_URIS) {
			print("Getting from: " + uri);
			companyUris.addAll(st.getAllTransitiveLeavesOf(uri,
					"rdfs:subClassOf", "rdf:type"));
		}
		uniqueCount = companyUris.size();
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

	/**
	 * Class that uses an Alias HashMap to find candidate Entities for a list of
	 * tokens.
	 */
	public class YagoEntityCandidateFinder {

		private HashMap<String, ArrayList<String>> aliasMap;
		private IgnoreTokenStripper ss;

		private YagoEntityCandidateFinder(
				HashMap<String, ArrayList<String>> aliasMap) {
			ss = new IgnoreTokenStripper(IgnoreTokenStripper.Language.English);
			this.aliasMap = aliasMap;
		};

		/**
		 * Gets candidate entities.
		 * 
		 * @param tokens
		 * @return A list of a list of {@link NamedEntity}s that are matched to
		 *         the same tokens. ( A token ngram can match to multiple
		 *         entities )
		 */
		public List<List<NamedEntity>> getCandidates(List<String> tokens) {
			// get Ngram entities
			Map<Integer, List<NamedEntity>> m1 = getNgramEntities(1, tokens);
			print("Unigrams");
			for (int ind : m1.keySet()) {
				print(ind + " : " + m1.get(ind));
			}
			Map<Integer, List<NamedEntity>> m2 = getNgramEntities(2, tokens);
			print("Bigrams");
			for (int ind : m2.keySet()) {
				print(ind + " : " + m2.get(ind));
			}
			Map<Integer, List<NamedEntity>> m3 = getNgramEntities(3, tokens);
			print("Trigrams");
			for (int ind : m3.keySet()) {
				print(ind + " : " + m3.get(ind));
			}
			// check for single token collisions
			for (int i : m1.keySet()) {
				boolean collision = false;
				for (int j : m2.keySet()) {
					if (j > i)
						break;
					if (i == j || i == j + 1) {
						collision = true;
						break;
					}
				}
				if (!collision)
					for (int j : m3.keySet()) {
						if (j > i)
							break;
						if (i >= j && i <= j + 2) {
							collision = true;
							break;
						}
					}
				if (!collision)
					m3.put(i, m1.get(i));
			}
			// check for bigram collisions
			for (int i : m2.keySet()) {
				boolean collision = false;
				for (int j : m3.keySet()) {
					if (j > i)
						break;
					if (i == j || i == j + 1) {
						collision = true;
						break;
					}
				}
				if (!collision)
					m3.put(i, m2.get(i));
			}
			ArrayList<List<NamedEntity>> rr = new ArrayList<List<NamedEntity>>();
			for (List<NamedEntity> entList : m3.values()) {
				rr.add(entList);
			}
			return rr;
		}

		private HashMap<Integer, List<NamedEntity>> getNgramEntities(int n,
				List<String> baseTokens) {
			List<String[]> ngrams = new StringNGramGenerator().getNGrams(
					baseTokens, n);
			List<String> tokens = new ArrayList<String>();
			for (int i = 0; i < ngrams.size(); i++) {
				tokens.add(StringUtils.join(ngrams.get(i), " "));
			}
			HashMap<Integer, List<NamedEntity>> result = new HashMap<Integer, List<NamedEntity>>();
			// Try and match ngrams
			for (int i = 0; i < tokens.size(); i++) {
				String token = tokens.get(i);
				if (!ss.isIgnoreToken(token)) {
					ArrayList<String> matches = getYagoCandidates(token);
					if (matches != null) {
						ArrayList<NamedEntity> subRes = new ArrayList<NamedEntity>();
						for (String match : matches) {
							NamedEntity ne = new NamedEntity();
							ne.rootName = match;
							ne.startToken = i;
							ne.stopToken = i - 1 + n;
							ne.type = NamedEntity.Type.Organisation;
							subRes.add(ne);
						}
						result.put(i, subRes);
					}
				}
			}
			return result;
		}

		private ArrayList<String> getYagoCandidates(String token) {
			if (aliasMap.containsKey(token))
				return aliasMap.get(token);
			else
				return null;
		}

	}

}
