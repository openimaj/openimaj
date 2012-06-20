package org.openimaj.text.nlp.sentiment.type;

import java.util.HashMap;
import java.util.Map;

/**
 * A Discrete count bipolar sentiment is one which is positive, negative or netural by some counts.
 * 
 * The sentiment holds values for counts of words considered to be one of the three and the total number of words the sentiment was decided
 * against. 
 * 
 * An assumption is made that a single term can only have a single sentiment, therefore
 * positive + negative + netural &lt; total
 * though perhaps not equal as some terms may be none of the 3 (stop words etc.)
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class DiscreteCountBipolarSentiment implements Sentiment, BipolarSentimentProvider, WeightedBipolarSentimentProvider{
	
	private int positive,negative,neutral;
	private int total;
	
	/**
	 * all weights set to 0
	 */
	public DiscreteCountBipolarSentiment() {}
	/**
	 * @param positive the positive 
	 * @param negative the negative
	 * @param neutral the neutral
	 * @param total the total number of terms
	 * @throws InvalidSentimentException 
	 */
	public DiscreteCountBipolarSentiment(int positive, int negative, int neutral, int total) throws InvalidSentimentException {
		this.positive = positive;
		this.negative = negative;
		this.neutral = neutral;
		this.total = total;
		if(positive + neutral + negative > total){
			throw new InvalidSentimentException("total counts was less than the total of positive, negative and neutral, this is impossible");
		}
	}
	
	/**
	 * @return positive
	 */
	public int positive(){
		return positive;
	}
	
	/**
	 * @return negative
	 */
	public int negative(){
		return negative;
	}
	
	/**
	 * @return neutral
	 */
	public int neutral(){
		return neutral;
	}
	
	@Override
	public BipolarSentiment bipolar() {
		if(this.positive > this.negative){
			if(this.positive > this.neutral){
				return BipolarSentiment.POSITIVE;
			}
			else{
				return BipolarSentiment.NEUTRAL;
			}
		}
		else{
			if(this.negative > this.neutral){
				return BipolarSentiment.NEGATIVE;
			}
			else{
				return BipolarSentiment.NEUTRAL;
			}
		}
	}

	@Override
	public Map<String, ?> asMap() {
		HashMap<String, Integer> ret = new HashMap<String,Integer>();
		ret.put("positive", positive);
		ret.put("negative", negative);
		ret.put("neutral", neutral);
		ret.put("total", total);
		return ret;
	}

	@Override
	public void fromMap(Map<String, ?> map) throws UnrecognisedMapException {
		if(!map.containsKey("positive") || !map.containsKey("negative") || !map.containsKey("neutral") ){
			throw new UnrecognisedMapException("positive","negative","neutral");
		}
		this.positive = (Integer) map.get("positive");
		this.negative = (Integer) map.get("negative");
		this.neutral = (Integer) map.get("neutral");
		this.total = (Integer) map.get("total");
	}
	@Override
	public WeightedBipolarSentiment weightedBipolar() {
		return new WeightedBipolarSentiment(
			this.positive / (double)this.total,
			this.negative / (double)this.total,
			this.neutral  / (double)this.total
		);
	}

}
