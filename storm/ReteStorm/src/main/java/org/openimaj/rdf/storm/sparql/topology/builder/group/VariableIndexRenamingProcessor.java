package org.openimaj.rdf.storm.sparql.topology.builder.group;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class VariableIndexRenamingProcessor {

	private String[] varNames;
	private Map<String, Integer> nameIndex;

	/**
	 * @param element
	 * @param varNames
	 */
	public VariableIndexRenamingProcessor(String[] varNames) {
		this.varNames = varNames;
		this.nameIndex = new HashMap<String,Integer>();
		int index = 0;
		for (String oldName : varNames) {
			this.nameIndex.put(oldName,index++);
		}
		// Now sort them by length, longest first
		Arrays.sort(this.varNames,new Comparator<String>() {

			@Override
			public int compare(String o1, String o2) {
				return -1 * (new Integer(o1.length()).compareTo(o2.length()));
			}
		});
	}

	public String constructQueryString(String originalQuery){
		for (String oldName : varNames) {
			originalQuery = originalQuery.replaceAll("[?]" + oldName, "?" + this.nameIndex.get(oldName));
		}
		return originalQuery;
	}
}
