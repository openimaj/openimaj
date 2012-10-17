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

import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TObjectIntProcedure;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.text.nlp.sentiment.model.wordlist.util.TFF;
import org.openimaj.text.nlp.sentiment.model.wordlist.util.TFF.Clue;
import org.openimaj.text.nlp.sentiment.model.wordlist.util.TFF.Polarity;

/**
 * A Discrete count sentiment is one which a set of arbitrary sentiments are given a count in a given phrase .
 * 
 * The sentiments hold values for counts of words considered to be one of the N sentiments provided and the total number of words the sentiment was 
 * decided against is also provided. 
 * 
 * An assumption is made that a single term can only have a single sentiment, therefore
 * sum(sentiment_count) &lt; total
 * though perhaps not equal as some terms may be none of the N sentiments (stop words etc.)
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TFFCountSentiment implements Sentiment, BipolarSentimentProvider, WeightedBipolarSentimentProvider, DiscreteCountBipolarSentimentProvider{
	
	private static final class BipolarTFFPolarityIterator implements TObjectIntProcedure<TFF.Polarity> {
		public int negative = 0;
		public int neutral = 0;
		public int positive = 0;

		@Override
		public boolean execute(Polarity a, int b) {
			if(a.equals(Polarity.positive) || a.equals(Polarity.strongpos) || a.equals(Polarity.weakpos)){
				positive+=b;
			}
			else if(a.equals(Polarity.both)){
				positive+=b;
				negative+=b;
			}
			else if(a.equals(Polarity.neutral)){
				neutral+=b;
			}
			else{
				negative+=b;
			}
			return true;
		}
	}
	private TObjectIntHashMap<TFF.Polarity> sentiments;
	private int total;
	
	/**
	 * all weights set to 0
	 */
	public TFFCountSentiment() {
		sentiments = new TObjectIntHashMap<TFF.Polarity>();
		for (Polarity polarity : TFF.Polarity.values()) {
			sentiments.put(polarity, 0);
		}
	}
	
	/**
	 * @param total instnatiate with a total number of words
	 */
	public TFFCountSentiment(int total) {
		this();
		this.total = total;
	}

	/**
	 * @param entry
	 * @param increment
	 */
	public void incrementClue(TFF.Clue entry, int increment){
		this.sentiments.adjustOrPutValue(entry.polarity, increment, increment);
	}
	
	@Override
	public BipolarSentiment bipolar() {
		BipolarTFFPolarityIterator instance = new BipolarTFFPolarityIterator();
		this.sentiments.forEachEntry(instance);
		if(instance.positive > instance.negative){
			if(instance.positive > instance.neutral){
				return BipolarSentiment.POSITIVE;
			}
			else{
				return BipolarSentiment.NEUTRAL;
			}
		}
		else{
			if(instance.negative > instance.neutral){
				return BipolarSentiment.NEGATIVE;
			}
			else{
				return BipolarSentiment.NEUTRAL;
			}
		}
	}
	
	@Override
	public BipolarSentiment bipolar(double deltaThresh) {
		BipolarTFFPolarityIterator instance = new BipolarTFFPolarityIterator();
		this.sentiments.forEachEntry(instance);
		if(instance.positive > instance.negative * deltaThresh){
			if(instance.positive > instance.neutral * deltaThresh){
				return BipolarSentiment.POSITIVE;
			}
			else if(instance.neutral > instance.positive * deltaThresh){
				return BipolarSentiment.NEUTRAL;
			}
		}
		else{
			if(instance.negative > instance.neutral * deltaThresh){
				return BipolarSentiment.NEGATIVE;
			}
			else if(instance.neutral > instance.negative * deltaThresh){
				return BipolarSentiment.NEUTRAL;
			}
		}
		return null;
	}

	@Override
	public WeightedBipolarSentiment weightedBipolar() {
		BipolarTFFPolarityIterator instance = new BipolarTFFPolarityIterator();
		this.sentiments.forEachEntry(instance);
		return new WeightedBipolarSentiment(
			instance.positive / (double)this.total,
			instance.negative / (double)this.total,
			instance.neutral / (double)this.total
		);
	}
	@Override
	public DiscreteCountBipolarSentiment countBipolarSentiment() {
		BipolarTFFPolarityIterator instance = new BipolarTFFPolarityIterator();
		this.sentiments.forEachEntry(instance);
		try {
			return new DiscreteCountBipolarSentiment(
				instance.positive ,
				instance.negative ,
				instance.neutral ,
				this.total
			);
		} catch (InvalidSentimentException e) {
			return null; // should never happen
		}
	}
	@Override
	public Map<String, ?> asMap() {
		HashMap<String, Integer> ret = new HashMap<String,Integer>();
		for (Polarity polarity: TFF.Polarity.values()) {
			ret.put(polarity.name(), this.sentiments.get(polarity));
		}
		ret.put("total", total);
		return ret;
	}

	@Override
	public void fromMap(Map<String, ?> map) throws UnrecognisedMapException {
		for (Polarity polarity : TFF.Polarity.values()) {
			Object value = map.get(polarity.name());
			if(value == null) throw new UnrecognisedMapException("Could not find polarity: " + polarity);
			this.sentiments.put(polarity, (Integer)value);
		}
		if(!map.containsKey("total")) throw new UnrecognisedMapException("Could not find total");
		this.total = (Integer) map.get("total");
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof TFFCountSentiment)) return false;
		TFFCountSentiment that = (TFFCountSentiment) obj;
		if(this.total != that.total) return false;
		for (Object clue : this.sentiments.keys()) {
			if(this.sentiments.get(clue) != that.sentiments.get(clue))return false;
		}
		return true;
	}

	/**
	 * @param clue
	 * @return given a clue construct a {@link TFFCountSentiment} with 1 word and 1 clue and call {@link TFFCountSentiment#bipolar()}
	 */
	public static BipolarSentiment bipolar(Clue clue) {
		TFFCountSentiment count = new TFFCountSentiment(1);
		count.incrementClue(clue, 1);
		return count.bipolar();
	}

	

}
