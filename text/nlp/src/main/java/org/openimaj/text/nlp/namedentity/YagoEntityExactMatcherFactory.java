package org.openimaj.text.nlp.namedentity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.text.nlp.namedentity.YagoEntityCandidateFinderFactory.YagoEntityCandidateFinder;
import org.openimaj.text.nlp.namedentity.YagoEntityContextScorerFactory.YagoEntityContextScorer;
import org.openimaj.text.nlp.tokenisation.ReversableToken;

/**
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class YagoEntityExactMatcherFactory {
	
	public static YagoEntityExactMatcher getMatcher(){
		return getMatcher(EntityExtractionResourceBuilder.getDefaultRootPath());
	}
	
	
	public static YagoEntityExactMatcher getMatcher(String yagoEntityFolderPath){
		YagoEntityCandidateFinder ycf = null;
		try {
			ycf = new YagoEntityCandidateFinderFactory(false).createFromAliasFile(yagoEntityFolderPath+File.separator+EntityExtractionResourceBuilder.DEFAULT_ALIAS_NAME);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		YagoEntityContextScorer ycs = null;
		try {
			ycs = new YagoEntityContextScorerFactory(false).createFromIndexFile(yagoEntityFolderPath+File.separator+EntityExtractionResourceBuilder.DEFAULT_CONTEXT_NAME);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return new YagoEntityExactMatcher(ycs,ycf);
	}

	/**
	 * The class that will extract unique Entities from a given list of tokens.
	 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class YagoEntityExactMatcher {
		
		private YagoEntityContextScorer contextScorer;
		private YagoEntityCandidateFinder candidateFinder;

		/**
		 * Default constructor.
		 * @param contextScorer
		 * @param candidateFinder
		 */
		public YagoEntityExactMatcher(YagoEntityContextScorer contextScorer,
				YagoEntityCandidateFinder candidateFinder) {
			this.contextScorer = contextScorer;
			this.candidateFinder = candidateFinder;
		}

		
		public List<NamedEntity> matchExact(List<String> possibleEntityTokens, List<String> contextTokens) {
			List<NamedEntity> result = new ArrayList<NamedEntity>();
			// Check if any candidates are found
			List<List<NamedEntity>> candidates = candidateFinder
					.getCandidates(possibleEntityTokens);
			// If none found, return an empty.
			if (candidates.size() == 0) {
				return result;
			}
			// Use Context Scoring to disambiguate candidates
			for (List<NamedEntity> can : candidates) {
				ArrayList<String> companies = new ArrayList<String>();
				for (NamedEntity ent : can) {
					companies.add(ent.rootName);
				}
				//get the localised context for each list of named Entities
				Map<NamedEntity, Float> contextScores = contextScorer
						.getScoresForEntityList(companies, contextTokens);
				float topScore = 0;
				NamedEntity resEntity = null;
				for (NamedEntity entity : can) {
					if (contextScores.keySet().contains(entity)
							&& contextScores.get(entity) > topScore) {
						resEntity = entity;
						for(NamedEntity te :contextScores.keySet()){
							if(resEntity.equals(te)){
								resEntity.type = te.type;
							}
						}
						topScore = contextScores.get(entity);
					}
				}
				if (resEntity != null)result.add(resEntity);
			}
			return result;
		}
		
		public List<NamedEntity> matchExact(List<? extends ReversableToken> possibleEntityTokens, String context) {
			List<NamedEntity> result = new ArrayList<NamedEntity>();
			// Check if any candidates are found
			List<List<NamedEntity>> candidates = candidateFinder.
					getCandidatesFromReversableTokenList(possibleEntityTokens);
			// If none found, return an empty.
			if (candidates.size() == 0) {
				return result;
			}
			// Use Context Scoring to disambiguate candidates
			for (List<NamedEntity> can : candidates) {
				ArrayList<String> companies = new ArrayList<String>();
				for (NamedEntity ent : can) {
					companies.add(ent.rootName);
				}
				//get the localised context for each list of named Entities
				Map<NamedEntity, Float> contextScores = contextScorer
						.getScoresForEntityList(companies, context);
				float topScore = 0;
				NamedEntity resEntity = null;
				for (NamedEntity entity : can) {
					if (contextScores.keySet().contains(entity)
							&& contextScores.get(entity) > topScore) {
						resEntity = entity;
						for(NamedEntity te :contextScores.keySet()){
							if(resEntity.equals(te)){
								resEntity.type = te.type;
							}
						}
						topScore = contextScores.get(entity);
					}
				}
				if (resEntity != null)result.add(resEntity);
			}
			return result;
		}
	}

}
