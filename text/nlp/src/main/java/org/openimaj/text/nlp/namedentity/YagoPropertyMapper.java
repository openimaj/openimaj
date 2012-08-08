package org.openimaj.text.nlp.namedentity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YagoPropertyMapper {
	
	public enum PropertyType {
		FACT_LITERAL,
		FACT_RESOURCE,
		TRIPLE_LITERAL,
		TRIPLE_RESOURCE,
	}
	
	public YagoPropertyMapper(){
		
	}
	
	public Map<String,Map<String,String>> mapProperties(List<String> URIs, Map<String,PropertyType> properties){
		Map<String, Map<String, String>> result = new HashMap<String,Map<String,String>>();
		for(String uri : URIs){
			
		}
		return null;
	}

}
