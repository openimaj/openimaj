package org.openimaj.text.nlp.namedentity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openimaj.text.nlp.namedentity.NGramGenerator.StringNGramGenerator;
import org.openimaj.text.nlp.tokenisation.ReversableToken;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

/**
 * Factory object for : -creating {@link YagoEntityCandidateFinder} in various
 * ways. -creating Yago Entity Alias text files in various ways.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
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
		String entity_Uri = null;
		while (strLine != null) {
			if (!strLine.startsWith("+") && !strLine.startsWith(".")) {
				strLine = br.readLine();
				continue;
			}
			if (strLine.startsWith("+")) {
				entity_Uri = strLine.substring(1);
				strLine = br.readLine();
				companies++;
			}
			while (strLine != null && strLine.startsWith(".")) {
				String alias = strLine.substring(1);
				if (result.containsKey(alias)) {
					if (!result.get(alias).contains(entity_Uri)) {
						result.get(alias).add(entity_Uri);
						aliasToMany++;
					}
				} else {
					ArrayList<String> comps = new ArrayList<String>();
					comps.add(entity_Uri);
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
		private ArrayList<Integer> ngrams;

		private YagoEntityCandidateFinder(
				HashMap<String, ArrayList<String>> aliasMap) {
			ss = new IgnoreTokenStripper(IgnoreTokenStripper.Language.English);
			this.aliasMap = aliasMap;
			this.setNgrams( 1, 2, 3, 4 , 5);
		};

		public void setNgrams(Integer... ngrams) {
			HashSet<Integer> ngUnique = new HashSet<Integer>(
					Arrays.asList(ngrams));
			ArrayList<Integer> n = new ArrayList<Integer>(ngUnique);
			Collections.sort(n);
			this.ngrams = n;
		}

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
			HashMap<Integer, Map<Integer, List<NamedEntity>>> ngramEntities = new HashMap<Integer, Map<Integer, List<NamedEntity>>>();
			for (int i = 0; i < ngrams.size(); i++) {
				ngramEntities.put(ngrams.get(i),
						getNgramEntities(ngrams.get(i), tokens));
			}
			// Resolve Collisions
			Map<Integer, List<NamedEntity>> top = ngramEntities.get(ngrams
					.get(ngrams.size() - 1));
			// For each map of ngram Entities starting from smallest ngram...
			for (int i = 0; i < ngrams.size(); i++) {
				int lowSize = ngrams.get(i);
				Map<Integer, List<NamedEntity>> lowEnts = ngramEntities
						.get(lowSize);
				// ...for each ngram Entity in the map...
				for (int startTokenLow : lowEnts.keySet()) {
					int endTokenLow = startTokenLow + lowSize - 1;
					boolean collision = false;
					// ...check that it does not collide with a larger ngram
					// entity...
					breakLoop: for (int j = i + 1; j < ngrams.size(); j++) {

						int highSize = ngrams.get(j);
						for (int startTokenHigh : ngramEntities.get(highSize)
								.keySet()) {
							int endTokenHigh = startTokenHigh + highSize - 1;
							if ((startTokenLow <= endTokenHigh && startTokenLow >= startTokenHigh)
									|| (endTokenLow >= startTokenHigh && endTokenLow <= endTokenHigh)) {
								collision = true;
								break breakLoop;
							}
						}
					}
					if (!collision) {
						top.put(startTokenLow, lowEnts.get(startTokenLow));
					}
				}
			}
			ArrayList<List<NamedEntity>> rr = new ArrayList<List<NamedEntity>>();
			for (List<NamedEntity> entList : top.values()) {
				rr.add(entList);
			}
			return rr;
		}
		
		/**
		 * Gets candidate entities.
		 * 
		 * @param tokens
		 * @return A list of a list of {@link NamedEntity}s that are matched to
		 *         the same tokens. ( A token ngram can match to multiple
		 *         entities )
		 */
		public List<List<NamedEntity>> getCandidatesFromReversableTokenList(List<? extends ReversableToken> tokens) {
			// get Ngram entities
			HashMap<Integer, Map<Integer, List<NamedEntity>>> ngramEntities = new HashMap<Integer, Map<Integer, List<NamedEntity>>>();
			for (int i = 0; i < ngrams.size(); i++) {
				ngramEntities.put(ngrams.get(i),
						getNgramEntitiesFromRTL(ngrams.get(i), tokens));
			}
			// Resolve Collisions
			Map<Integer, List<NamedEntity>> top = ngramEntities.get(ngrams
					.get(ngrams.size() - 1));
			// For each map of ngram Entities starting from smallest ngram...
			for (int i = 0; i < ngrams.size(); i++) {
				int lowSize = ngrams.get(i);
				Map<Integer, List<NamedEntity>> lowEnts = ngramEntities
						.get(lowSize);
				// ...for each ngram Entity in the map...
				for (int startTokenLow : lowEnts.keySet()) {
					int endTokenLow = startTokenLow + lowSize - 1;
					boolean collision = false;
					// ...check that it does not collide with a larger ngram
					// entity...
					breakLoop: for (int j = i + 1; j < ngrams.size(); j++) {

						int highSize = ngrams.get(j);
						for (int startTokenHigh : ngramEntities.get(highSize)
								.keySet()) {
							int endTokenHigh = startTokenHigh + highSize - 1;
							if ((startTokenLow <= endTokenHigh && startTokenLow >= startTokenHigh)
									|| (endTokenLow >= startTokenHigh && endTokenLow <= endTokenHigh)) {
								collision = true;
								break breakLoop;
							}
						}
					}
					if (!collision) {
						top.put(startTokenLow, lowEnts.get(startTokenLow));
					}
				}
			}
			ArrayList<List<NamedEntity>> rr = new ArrayList<List<NamedEntity>>();
			for (List<NamedEntity> entList : top.values()) {
				rr.add(entList);
			}
			return rr;
		}

		private Map<Integer, List<NamedEntity>> getNgramEntitiesFromRTL(
				Integer n, List<? extends ReversableToken> baseTokens) {
			NGramGenerator<ReversableToken> ngen = new NGramGenerator<ReversableToken>(ReversableToken.class);
			List<ReversableToken[]> ngrams = ngen.getNGrams(
					baseTokens, n);
			List<String> tokens = new ArrayList<String>();
			for (int i = 0; i < ngrams.size(); i++) {
				tokens.add(ngrams.get(0)[0].reverse(Arrays.asList(ngrams.get(0))));
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
			if (aliasMap.containsKey(token)) {
				//System.out.println(aliasMap.get(token));
				ArrayList<String> result = aliasMap.get(token);
				return result;
			} else
				return null;
		}

	}

}
