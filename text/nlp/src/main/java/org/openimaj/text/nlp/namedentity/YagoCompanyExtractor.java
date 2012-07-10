package org.openimaj.text.nlp.namedentity;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.magus.fourstore.client.Store;

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
	 * the list.
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

	public class YagoCompanyQueryTool {
		private Store store;
		private String endPoint = "http://193.131.98.57:8080";
		private String lastToken=null;
		private boolean lastWasAlias=false;
		private ArrayList<String> rootNames;

		public YagoCompanyQueryTool() {
			try {
				store = new Store(endPoint);
			} catch (MalformedURLException e) {
				System.out.println("Could not connect to sparql end point");
				e.printStackTrace();
			}
		}

		public boolean isCompanyAlias(String token) {
			lastToken=token;
			boolean result = false;
			// See if the token means anything in Yago.
			String means = null;
			try {
				//TODO: write query
				means = store.query("SELECT ?x WHERE { "+token+" means ?x }");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if (means != null) {
				String[] meanings = means.split(",");

				String c = null;
				try {
					//TODO: write query
					c = store.query("SELECT ?x WHERE { ?x type company }");
				} catch (Exception e) {					
					e.printStackTrace();
				}
				String[] cc = c.split(",");
				ArrayList<String> companies = new ArrayList<String>(Arrays.asList(cc));
				ArrayList<String> hits = new ArrayList<String>();
				for (String meaning : meanings) {
					if(companies.contains(meaning)){
						result=true;
						hits.add(meaning);
					}
				}
				if(result==true){
					rootNames=hits;
				}
			}
			lastWasAlias=result;
			return result;
		}

		public ArrayList<String> getRootNameFromAlias(String token) {
			if(lastToken.equals(token)){
				if(lastWasAlias){
					return rootNames;
				}
				else return null;
			}
			else{
				isCompanyAlias(token);
				return getRootNameFromAlias(token);
			}
		}
	}

}
