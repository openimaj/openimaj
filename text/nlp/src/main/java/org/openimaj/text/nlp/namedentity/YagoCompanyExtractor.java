package org.openimaj.text.nlp.namedentity;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.text.nlp.namedentity.FourStoreClientTool.Node;

import uk.co.magus.fourstore.client.Store;

/**
 * This class aims to use the Yago2 knowledge base to determine weather a
 * tokenised string has any references to companies. getEntities() is the
 * callable method for the results.
 * 
 * @author Laurence Willmore <lgw1e10@ecs.soton.ac.uk>
 * 
 */
public class YagoCompanyExtractor implements NamedEntityExtractor {

	StopWordStripper ss;
	YagoCompanyQueryTool qt;

	public YagoCompanyExtractor() {
		super();
		ss = new StopWordStripper(StopWordStripper.ENGLISH);
		qt = new YagoCompanyQueryTool();
	}

	private ArrayList<NamedEntity> getYagoCandidates(String token) {
		ArrayList<NamedEntity> result = null;
		if (qt.isCompanyAlias(token)) {
			result = new ArrayList<NamedEntity>();
			for (String rootName : qt.getRootNameFromAlias(token)) {
				result.add(new NamedEntity(rootName, "Company"));
			}
		}
		return result;
	}

	/**
	 * Does nothing at the moment other then returning the first candidate in
	 * the list. Will hopefully have some real context checking for this
	 * selection in the future.
	 * 
	 * @param candidates
	 * @param context
	 * @return
	 */
	private Map<Integer, NamedEntity> contextFilter(
			Map<Integer, ArrayList<NamedEntity>> candidates,
			List<String> context) {
		HashMap<Integer, NamedEntity> result = new HashMap<Integer, NamedEntity>();
		for (Integer ind : candidates.keySet()) {
			result.put(ind, candidates.get(ind).get(0));
		}
		return result;
	}

	@Override
	public Map<Integer, NamedEntity> getEntities(List<String> tokens) {
		// Build a list of candidate entities for each non-stopword token.
		HashMap<Integer, ArrayList<NamedEntity>> candidates = new HashMap<Integer, ArrayList<NamedEntity>>();
		for (int i = 0; i < tokens.size(); i++) {
			String token = tokens.get(i);
			if (!ss.isStopWord(token)) {
				ArrayList<NamedEntity> matches = getYagoCandidates(token);
				if (matches != null) {
					candidates.put(i, matches);
				}
			}
		}
		// choose one of the candidates for each token
		return contextFilter(candidates, tokens);
	}

	/**
	 * A worker class that acts as a wrapper for queries to an RDF database to
	 * determine if a token is possibly a company
	 * 
	 * @author laurence
	 * 
	 */
	public class YagoCompanyQueryTool {

		private String endPoint = "http://193.131.98.57:8080";
		private String lastToken = null;
		private boolean lastWasAlias = false;
		private ArrayList<String> rootNames;
		private FourStoreClientTool fsct;

		public YagoCompanyQueryTool() {
			fsct = new FourStoreClientTool(endPoint);
		}

		/**
		 * 
		 * checks if the token is a known alias of a company
		 * 
		 * @param token
		 * @return true if this token could be a company.
		 */
		public boolean isCompanyAlias(String token) {
			lastToken = token;
			boolean result = false;
			// See if the token means anything in Yago.
			ArrayList<HashMap<String, Node>> m = null;
			try {
				m = fsct.query("SELECT ?meaning WHERE { " + token
						+ " means ?meaning }");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			// if it does mean something, check if that meaning is a company
			if (m != null) {
				ArrayList<String> hits = new ArrayList<String>();
				for (HashMap<String, Node> mNode : m) {
					String meaning = mNode.get("meaning").value;
					ArrayList<HashMap<String, Node>> companyHit = fsct
							.query("SELECT * WHERE { " + meaning
									+ " type company }");
					if (companyHit.size() > 0) {
						result = true;
						hits.add(meaning);
					}
				}
				if (result == true) {
					rootNames = hits;
				}
			}
			lastWasAlias = result;
			return result;
		}

		/**
		 * Returns the possible rootnames of the companies that a token could be
		 * referring to.
		 * 
		 * @param token
		 * @return list of company root names
		 */
		public ArrayList<String> getRootNameFromAlias(String token) {
			if (lastToken.equals(token)) {
				if (lastWasAlias) {
					return rootNames;
				} else
					return null;
			} else {
				isCompanyAlias(token);
				return getRootNameFromAlias(token);
			}
		}
	}

}
