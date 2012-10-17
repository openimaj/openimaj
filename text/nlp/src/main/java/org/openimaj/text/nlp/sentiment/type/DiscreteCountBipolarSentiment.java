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
import java.util.Map;

import org.openimaj.util.math.ObjectArithmetic;
import org.openimaj.util.math.ScalarArithmetic;

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
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class DiscreteCountBipolarSentiment implements Sentiment, BipolarSentimentProvider, WeightedBipolarSentimentProvider, ScalarArithmetic<DiscreteCountBipolarSentiment, Integer>, ObjectArithmetic<DiscreteCountBipolarSentiment>{
	
	/**
	 * A single positive count
	 */
	public static final DiscreteCountBipolarSentiment POSITIVE = new DiscreteCountBipolarSentiment(1,0,0);
	/**
	 * a single negative count
	 */
	public static final DiscreteCountBipolarSentiment NEGATIVE = new DiscreteCountBipolarSentiment(0,1,0);
	/**
	 * a single neutral count
	 */
	public static final DiscreteCountBipolarSentiment NEUTRAL = new DiscreteCountBipolarSentiment(0,0,1);
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
	 * @param positive the positive 
	 * @param negative the negative
	 * @param neutral the neutral 
	 */
	public DiscreteCountBipolarSentiment(int positive, int negative, int neutral){
		this.positive = positive;
		this.negative = negative;
		this.neutral = neutral;
		this.total = positive + negative + neutral;
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
	public BipolarSentiment bipolar(double deltaThresh) {
		if(this.positive > this.negative  * deltaThresh){
			if(this.positive > this.neutral * deltaThresh){
				return BipolarSentiment.POSITIVE;
			}
			else if(this.neutral > this.positive  * deltaThresh){
				return BipolarSentiment.NEUTRAL;
			}
		}
		else{
			if(this.negative > this.neutral  * deltaThresh){
				return BipolarSentiment.NEGATIVE;
			}
			else if(this.neutral > this.negative *  deltaThresh){
				return BipolarSentiment.NEUTRAL;
			}
		}
		return null;
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
	
	@Override
	protected DiscreteCountBipolarSentiment clone() {
		try {
			return new DiscreteCountBipolarSentiment(this.positive, this.negative,this.neutral, this.total);
		} catch (InvalidSentimentException e) {
			return null;
		}
	}
	
	@Override
	public DiscreteCountBipolarSentiment addInplace(DiscreteCountBipolarSentiment s) {
		this.positive += s.positive;
		this.negative += s.negative;
		this.neutral += s.neutral;
		return this;
	}
	@Override
	public DiscreteCountBipolarSentiment multiplyInplace(DiscreteCountBipolarSentiment s) {
		this.positive *= s.positive;
		this.negative *= s.negative;
		this.neutral *= s.neutral;
		return this;
	}
	@Override
	public DiscreteCountBipolarSentiment divideInplace(DiscreteCountBipolarSentiment s) {
		this.positive /= s.positive;
		this.negative /= s.negative;
		this.neutral /= s.neutral;
		return this;
	}
	@Override
	public DiscreteCountBipolarSentiment add(DiscreteCountBipolarSentiment s) {
		DiscreteCountBipolarSentiment c = this.clone();
		return c.addInplace(s);
	}
	@Override
	public DiscreteCountBipolarSentiment subtract(DiscreteCountBipolarSentiment s) {
		return this.add(s.multiply(-1));
	}
	@Override
	public DiscreteCountBipolarSentiment subtractInplace(DiscreteCountBipolarSentiment s) {
		return this.addInplace(s.multiply(-1));
	}
	@Override
	public DiscreteCountBipolarSentiment multiply(DiscreteCountBipolarSentiment s) {
		return this.clone().multiplyInplace(s);
	}
	@Override
	public DiscreteCountBipolarSentiment divide(DiscreteCountBipolarSentiment s) {
		return this.clone().divideInplace(s);
	}
	
	@Override
	public DiscreteCountBipolarSentiment addInplace(Integer s) {
		this.positive += s;
		this.negative += s;
		this.neutral += s;
		return this;
	}
	@Override
	public DiscreteCountBipolarSentiment multiplyInplace(Integer  s) {
		this.positive *= s;
		this.negative *= s;
		this.neutral *= s;
		return this;
	}
	@Override
	public DiscreteCountBipolarSentiment divideInplace(Integer  s) {
		this.positive /= s;
		this.negative /= s;
		this.neutral /= s;
		return this;
	}
	@Override
	public DiscreteCountBipolarSentiment add(Integer  s) {
		DiscreteCountBipolarSentiment c = this.clone();
		return c.addInplace(s);
	}
	@Override
	public DiscreteCountBipolarSentiment subtract(Integer  s) {
		return this.add(s * -1);
	}
	@Override
	public DiscreteCountBipolarSentiment subtractInplace(Integer  s) {
		return this.addInplace(s * -1);
	}
	@Override
	public DiscreteCountBipolarSentiment multiply(Integer s) {
		return this.clone().multiplyInplace(s);
	}
	@Override
	public DiscreteCountBipolarSentiment divide(Integer s) {
		return this.clone().divideInplace(s);
	}
	
	@Override
	public String toString() {
		String frmt = "Positive == %d\nNegative == %d\nNetural == %d";
		return String.format(frmt,positive,negative,neutral);
	}

}
