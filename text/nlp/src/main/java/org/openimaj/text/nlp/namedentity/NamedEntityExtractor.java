package org.openimaj.text.nlp.namedentity;

import java.util.List;
import java.util.Map;

public abstract interface NamedEntityExtractor {
	
	public  Map<Integer,NamedEntity> getEntities(List<String> tokens);

}
