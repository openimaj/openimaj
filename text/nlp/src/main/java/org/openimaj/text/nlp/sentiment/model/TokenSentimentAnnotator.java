package org.openimaj.text.nlp.sentiment.model;

import java.util.Set;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.ml.annotation.AbstractAnnotator;
import org.openimaj.text.nlp.sentiment.type.BipolarSentiment;

/**
 * A sentiment annotator which can deal with word tokens
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), 
 *
 * @param <CLONETYPE>
 * @param <EXTRACTOR> The feature extractor used to go from String to features
 */
public abstract class TokenSentimentAnnotator <
			EXTRACTOR extends FeatureExtractor<?,String>,
			CLONETYPE extends SentimentAnnotator<String,EXTRACTOR,CLONETYPE>
		>
		extends AbstractAnnotator<String,BipolarSentiment, EXTRACTOR>
		implements SentimentAnnotator<String, EXTRACTOR,CLONETYPE>{
		
				
		/**
		* @param extractor
		*/
		public TokenSentimentAnnotator(EXTRACTOR extractor) {
			super(extractor);
		}
		
		@Override
		public Set<BipolarSentiment> getAnnotations() {
			return BipolarSentiment.listBipolarSentiment();
		}
}
