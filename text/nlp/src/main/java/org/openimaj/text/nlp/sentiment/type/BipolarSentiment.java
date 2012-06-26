package org.openimaj.text.nlp.sentiment.type;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulate a simple sentiment that something is positive, negative or neutral. 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class BipolarSentiment implements Sentiment{
	/**
	 * The states of a bipolar sentiment
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 *
	 */
	public static enum State{
		/**
		 * positive sentiment
		 */
		POSITIVE,
		/**
		 * negative sentiment
		 */
		NEGATIVE,
		/**
		 * Neither positive nore negative, neutral sentiment (or objective for example)
		 */
		NEUTRAL
	}
	/**
	 * a neutral sentiment instance
	 */
	public static final BipolarSentiment NEUTRAL = new BipolarSentiment(State.NEUTRAL);
	/**
	 * a negative sentiment instance
	 */
	public static final BipolarSentiment NEGATIVE = new BipolarSentiment(State.NEGATIVE);
	/**
	 * a positive sentiment instance
	 */
	public static final BipolarSentiment POSITIVE = new BipolarSentiment(State.POSITIVE);
	private State state;
	
	/**
	 * Initialize sentiment as {@link State#NEUTRAL}
	 */
	public BipolarSentiment() {
		this(State.NEUTRAL);
	}
	/**
	 * Instantiate the sentiment
	 * @param positive
	 */
	public BipolarSentiment(State positive){
		this.state = positive;
	}
	
	/**
	 * @return sentiment == NEGATIVE
	 */
	public boolean negative() {
		return state == State.NEGATIVE;
	}
	
	/**
	 * @return positive
	 */
	public boolean positive() {
		return state == State.POSITIVE;
	}
	
	/**
	 * @return positive
	 */
	public boolean neutral() {
		return state == State.NEUTRAL;
	}
	
	/**
	 * @return the bipolar sentiment
	 */
	public State sentiment(){
		return state;
	}
	
	@Override
	public Map<String, ?> asMap() {
		HashMap<String, Object> ret = new HashMap<String,Object>();
		ret.put("state", state);
		return ret;
	}
	@Override
	public void fromMap(Map<String, ?> map) throws UnrecognisedMapException {
		if(!map.containsKey("state")){
			throw new UnrecognisedMapException("state");
		}
		this.state = (State) map.get("state");
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof BipolarSentiment)) return false;
		BipolarSentiment that = (BipolarSentiment) obj;
		return that.state == this.state;
	}
	
	@Override
	public String toString() {
		return this.state.name();
	}

}
