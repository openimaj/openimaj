package org.openimaj.text.nlp.sentiment.model;

import java.util.List;

import org.openimaj.math.model.Model;
import org.openimaj.text.nlp.sentiment.type.Sentiment;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <T> The type of sentiment this model returns
 * @param <C> the type of model this model clones into
 */
public interface SentimentModel<T extends Sentiment,C extends SentimentModel<T,C>> extends Model<List<String>,T>{

	@Override
	public C clone() ;
}
