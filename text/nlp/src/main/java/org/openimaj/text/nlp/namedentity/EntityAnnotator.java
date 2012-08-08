package org.openimaj.text.nlp.namedentity;

import java.util.HashMap;
import java.util.List;

import org.openimaj.feature.IdentityFeatureExtractor;
import org.openimaj.ml.annotation.AbstractAnnotator;

public abstract class EntityAnnotator extends
AbstractAnnotator<List<String>, HashMap<String, Object>, IdentityFeatureExtractor<List<String>>>{
	
	public enum EntityType{
		Organisation
	}
	public static String URIS ="URIS";
	public static String START_TOKEN ="START";
	public static String END_TOKEN ="END";
	public static String TYPE ="TYPE";
	public static String URI ="URI";
	public static String SCORE ="SCORE";

	public EntityAnnotator(){
		super(new IdentityFeatureExtractor<List<String>>());
	}

}
