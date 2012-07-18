package org.openimaj.text.nlp.namedentity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Very simple gazatteer based entity extraction. Checks if the tokens match
 * anything in the gazatteer.
 */
public class AnnieCompanyExtractor implements NamedEntityExtractor {

	private String companyList = "/org/openimaj/text/namedentity/anniegazetteer/company.lst";
	private ArrayList<String> companies;

	
	/**
	 * default constructor
	 */
	public AnnieCompanyExtractor() {
		buildCompanyGazetteer();
	}

	private void buildCompanyGazetteer() {
		companies = new ArrayList<String>();
		try {
			InputStream fstream = AnnieCompanyExtractor.class
					.getResourceAsStream(companyList);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				String pure = strLine.trim().toLowerCase();
				companies.add(pure);
			}
			// Close the input stream
			in.close();
			Collections.sort(companies);
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	@Override
	public Map<Integer, String> getEntities(List<String> tokens) {
		HashMap<Integer, String> result = new HashMap<Integer, String>();
		for (int i = 0; i < tokens.size(); i++) {
			String match = tokens.get(i).toLowerCase();
			if (Collections.binarySearch(companies, match)>0) {
				result.put(i, match);
			}
		}
		return result;
	}

}
