package org.openimaj.text.nlp.namedentity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EntityContextScorer<INPUT> {
	
	public abstract HashMap<String, Float> getScoredEntitiesFromContext(INPUT context);
	
	public abstract Map<String,Float> getScoresForEntityList(List<String> entityUris,INPUT context);

}
