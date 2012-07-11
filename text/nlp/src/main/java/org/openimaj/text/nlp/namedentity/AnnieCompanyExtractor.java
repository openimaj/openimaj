package org.openimaj.text.nlp.namedentity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnieCompanyExtractor implements NamedEntityExtractor {

	private String companyList = "src/main/resources/org/openimaj/text/namedentity/anniegazetteer/company.lst";
	private ArrayList<String> companies;

	public AnnieCompanyExtractor() {		
		buildCompanyGazetteer();
	}

	private void buildCompanyGazetteer() {
		companies = new ArrayList<String>();
		try {
			FileInputStream fstream = new FileInputStream(companyList);
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
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	@Override
	public Map<Integer, NamedEntity> getEntities(List<String> tokens) {
		HashMap<Integer,NamedEntity> result = new HashMap<Integer, NamedEntity>();
		for (int i = 0; i < tokens.size(); i++) {
			String match =tokens.get(i).toLowerCase();
			if(companies.contains(match)){
				result.put(i, new NamedEntity(match,"Company"));
			}
		}
		return result;
	}	

	
}
