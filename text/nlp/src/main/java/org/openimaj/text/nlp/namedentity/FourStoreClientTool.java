package org.openimaj.text.nlp.namedentity;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.HashMap;

import uk.co.magus.fourstore.client.Store;

import com.google.gson.Gson;

/**
 * Wrapper for the java 4Store client (Copyright (c) 2009, Magus Ltd) to put the
 * results of a sparql query into a hashmap
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class FourStoreClientTool {

	private Store mystore;

	/**
	 * Constructor
	 * 
	 * @param fourStoreEndPoint
	 */
	public FourStoreClientTool(String fourStoreEndPoint) {
		try {
			mystore = new Store("http://lod.openlinksw.com/sparql/");
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Takes a String sparql query to return a set of results.
	 * 
	 * @param query
	 * @return ArrayList<HashMap<String,Node>> where each hashmap is a tuple(or
	 *         part of tuple) that matches the select. The String in the hashmap
	 *         is the name of the variable(s) returned by the query mapped to a
	 *         Node object that records the type and value of the variable
	 *         binding.
	 */
	public ArrayList<HashMap<String, Node>> query(String query) {
		String result = null;
		QueryResult queryResult;
		try {
			result = mystore.query(query, Store.OutputFormat.JSON);
			System.out.println(result);
		} catch (final MalformedURLException e) {

			e.printStackTrace();
		} catch (final ProtocolException e) {

			e.printStackTrace();
		} catch (final IOException e) {

			e.printStackTrace();
		}
		if (result != null) {
			final Gson gson = new Gson();
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

	/**
	 * Container class for a single rdf value and its type.
	 * 
	 * @author laurence
	 * 
	 */
	public class Node {
		/**
		 * Type of the rdf object.
		 */
		public String type;
		/**
		 * Value of the rdf object.
		 */
		public String value;
	}

}
