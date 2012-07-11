package org.openimaj.text.nlp.namedentity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import uk.co.magus.fourstore.client.Store;

import com.google.gson.Gson;

public class FourStoreClientTool{

	private Store mystore; 

	public FourStoreClientTool(String fourStoreEndPoint) {
		try {
			mystore = new Store("http://lod.openlinksw.com/sparql/");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ArrayList<HashMap<String, Node>> query(String query) {
		String result = null;
		QueryResult queryResult;
		try {
			result = mystore.query("Select ?s WHERE { ?s ?p ?o } LIMIT 10",
					Store.OutputFormat.JSON);
			System.out.println(result);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result != null) {
			Gson gson = new Gson();
			queryResult = gson.fromJson(result, QueryResult.class);
			return queryResult.results.bindings;
		}
		return null;

	}

	private class QueryResult {		
		public Results results;
		private class Results {			
			public ArrayList<HashMap<String, Node>> bindings;			
		}
	}	
	public class Node {
		public String type;
		public String value;
	}

}
