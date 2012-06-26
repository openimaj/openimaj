package org.openimaj.text.nlp.sentiment.type;

import java.util.Map;

/**
 * A generic sentiment. As sentiments can be very simply (+/-), weighted positive negative or generic things such as mood, the root
 * mainly provide the ability to be returned as a map of results (useful for writing to JSON). However, individual instances are 
 * also expected to list the various forms a given sentiment type can take.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface Sentiment {
	/**
	 * @return return a map representing this sentiment, may be more than just the held sentiments, but extra data
	 */
	public Map<String,?> asMap();
	
	/**
	 * @param map the map containing this sentiment
	 * @throws UnrecognisedMapException 
	 */
	public void fromMap(Map<String,?> map) throws UnrecognisedMapException;
}
