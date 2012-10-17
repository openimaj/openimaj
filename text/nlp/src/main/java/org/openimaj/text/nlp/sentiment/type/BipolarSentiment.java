/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.text.nlp.sentiment.type;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulate a simple sentiment that something is positive, negative or
 * neutral.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class BipolarSentiment implements Sentiment {
	/**
	 * The states of a bipolar sentiment
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static enum State {
		/**
		 * positive sentiment
		 */
		POSITIVE,
		/**
		 * negative sentiment
		 */
		NEGATIVE,
		/**
		 * Neither positive nore negative, neutral sentiment (or objective for
		 * example)
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
	 * 
	 * @param positive
	 */
	public BipolarSentiment(State positive) {
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
	public State sentiment() {
		return state;
	}

	@Override
	public Map<String, ?> asMap() {
		final HashMap<String, Object> ret = new HashMap<String, Object>();
		ret.put("state", state);
		return ret;
	}

	@Override
	public void fromMap(Map<String, ?> map) throws UnrecognisedMapException {
		if (!map.containsKey("state")) {
			throw new UnrecognisedMapException("state");
		}
		this.state = (State) map.get("state");
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BipolarSentiment))
			return false;
		final BipolarSentiment that = (BipolarSentiment) obj;
		return that.state == this.state;
	}

	@Override
	public String toString() {
		return this.state.name();
	}

	/**
	 * @return a list of the {@link BipolarSentiment} instances
	 */
	public static Set<BipolarSentiment> listBipolarSentiment() {
		final Set<BipolarSentiment> ret = new HashSet<BipolarSentiment>();
		ret.add(POSITIVE);
		ret.add(NEUTRAL);
		ret.add(NEGATIVE);
		return ret;
	}

}
