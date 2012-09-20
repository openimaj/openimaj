package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.namedentity.YagoEntityCandidateFinderFactory.YagoEntityCandidateFinder;

/**
 * {@link EntityAnnotator} wrapper for a {@link YagoEntityCandidateFinder}
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class YagoEntityCandidateAnnotator
		extends
		EntityAnnotator {	
	private YagoEntityCandidateFinder ycf;
	
	
	/**
	 * Default Constructor
	 * @param ycf does the work of finding candidates
	 */
	public YagoEntityCandidateAnnotator(YagoEntityCandidateFinder ycf){
		super();
		this.ycf=ycf;
	}
	
	@Override
	public Set<HashMap<String, Object>> getAnnotations() {
		return null;
	}

	@Override
	public List<ScoredAnnotation<HashMap<String, Object>>> annotate(
			List<String> tokens) {
		List<ScoredAnnotation<HashMap<String, Object>>> annos = new ArrayList<ScoredAnnotation<HashMap<String,Object>>>();
		for(List<NamedEntity> entList : ycf.getCandidates(tokens)){
			for(NamedEntity ent:entList){
				HashMap<String,Object> annotation = new HashMap<String, Object>();
				annotation.put(EntityAnnotator.URI, ent.rootName);
				annotation.put(EntityAnnotator.START_TOKEN, ent.startToken);
				annotation.put(EntityAnnotator.END_TOKEN, ent.stopToken);
				annotation.put(EntityAnnotator.TYPE, ent.type);
				annos.add(new ScoredAnnotation<HashMap<String,Object>>(annotation, 1));
			}			
		}
		return annos;
	}	

}
