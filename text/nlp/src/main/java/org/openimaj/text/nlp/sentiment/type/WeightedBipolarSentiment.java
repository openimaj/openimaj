package org.openimaj.text.nlp.sentiment.type;

import java.util.HashMap;
import java.util.Map;

import org.openimaj.util.math.ObjectArithmetic;
import org.openimaj.util.math.ScalarArithmetic;

/**
 * A weighted bipolar sentiment is one which is positive, negative or netural to some degree.
 * 
 * The sentiment holds values for the three and the total number of words the sentiment was decided
 * against. In a simple case the numbers might be wieghted counts of words judged as being positive, negative or neutral.
 * 
 * In more complex implementations the numbers may represent probability estimates for the three states
 * 
 * No guarantee is made on the range of weights
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WeightedBipolarSentiment implements 
	Sentiment, 
	BipolarSentimentProvider, 
	ScalarArithmetic<WeightedBipolarSentiment, Double>,
	ObjectArithmetic<WeightedBipolarSentiment>

{
	
	private double positive,negative,neutral;
	
	/**
	 * all weights set to 0
	 */
	public WeightedBipolarSentiment() {}
	/**
	 * @param positive the positive 
	 * @param negative the negative
	 * @param neutral the neutral
	 */
	public WeightedBipolarSentiment(double positive, double negative, double neutral) {
		this.positive = positive;
		this.negative = negative;
		this.neutral = neutral;
	}
	
	/**
	 * @return positive
	 */
	public double positive(){
		return positive;
	}
	
	/**
	 * @return negative
	 */
	public double negative(){
		return negative;
	}
	
	/**
	 * @return neutral
	 */
	public double neutral(){
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
		if(this.positive > this.negative * deltaThresh){
			if(this.positive > this.neutral *deltaThresh){
				return BipolarSentiment.POSITIVE;
			}
			else if(this.neutral > this.positive * deltaThresh){
				return BipolarSentiment.NEUTRAL;
			}
		}
		else{
			if(this.negative > this.neutral * deltaThresh){
				return BipolarSentiment.NEGATIVE;
			}
			else if (this.neutral > this.negative * deltaThresh){
				return BipolarSentiment.NEUTRAL;
			}
		}
		return null;
	}

	@Override
	public Map<String, ?> asMap() {
		HashMap<String, Double> ret = new HashMap<String,Double>();
		ret.put("positive", positive);
		ret.put("negative", negative);
		ret.put("neutral", neutral);
		return ret;
	}

	@Override
	public void fromMap(Map<String, ?> map) throws UnrecognisedMapException {
		if(!map.containsKey("positive") || !map.containsKey("negative") || !map.containsKey("neutral") ){
			throw new UnrecognisedMapException("positive","negative","neutral");
		}
		this.positive = (Double) map.get("positive");
		this.negative = (Double) map.get("negative");
		this.neutral = (Double) map.get("neutral");
	}
	
	@Override
	public String toString() {
		String out = "+(%.4f),-(%.4f),~(%.4f)";
		
		return String.format(out,this.positive,this.negative,this.neutral);
	}
	
	// ARITHMATIC CODE FROM HERE 
	
	@Override
	public WeightedBipolarSentiment addInplace(WeightedBipolarSentiment that) {
		this.negative += that.negative;
		this.positive += that.positive;
		this.neutral += that.neutral;
		return this;
	}
	
	@Override
	public WeightedBipolarSentiment add(WeightedBipolarSentiment that) {
		return this.clone().add(that);
	}
	
	@Override
	public WeightedBipolarSentiment divide(WeightedBipolarSentiment that) {
		return this.clone().divideInplace(that);
	}
	
	@Override
	public WeightedBipolarSentiment divideInplace(WeightedBipolarSentiment that){
		this.negative /= that.negative;
		this.neutral /= that.neutral;
		this.positive /= that.positive;
		return this;
	}

	
	@Override
	public WeightedBipolarSentiment clone(){
		return new WeightedBipolarSentiment(this.positive,this.negative,this.neutral);
	}
	/**
	 * @return the total of the bipolar weightings
	 */
	public double total() {
		return this.negative + this.neutral + this.positive;
	}
	
	
	@Override
	public WeightedBipolarSentiment add(Double f) {
		return this.clone().addInplace(f);
	}
	
	@Override
	public WeightedBipolarSentiment addInplace(Double f) {
		this.negative += f;
		this.positive += f;
		this.neutral += f;
		return this;
	}
	
	
	@Override
	public WeightedBipolarSentiment divide(Double d){
		return this.clone().divideInplace(d);
	}
	@Override
	public WeightedBipolarSentiment divideInplace(Double d) {
		this.negative /= d;
		this.neutral /= d;
		this.positive /= d;
		return this;
	}
	@Override
	public WeightedBipolarSentiment subtract(WeightedBipolarSentiment s) {
		return this.clone().subtractInplace(s);
	}
	@Override
	public WeightedBipolarSentiment subtractInplace(WeightedBipolarSentiment f) {
		this.negative -= f.negative;
		this.positive -= f.positive;
		this.neutral -= f.neutral;
		return this;
	}
	
	@Override
	public WeightedBipolarSentiment multiply(WeightedBipolarSentiment that) {
		return this.clone().multiplyInplace(that);
	}
	
	
	
	@Override
	public WeightedBipolarSentiment multiplyInplace(WeightedBipolarSentiment that){
		this.negative *= that.negative;
		this.neutral *= that.neutral;
		this.positive *= that.positive;
		return this;
	}
	

	@Override
	public WeightedBipolarSentiment multiply(Double value) {
		return this.clone().multiplyInplace(value);
	}
	
	@Override
	public WeightedBipolarSentiment multiplyInplace(Double value){
		this.negative *= value;
		this.neutral *= value;
		this.positive *= value;
		return this;
	}
	@Override
	public WeightedBipolarSentiment subtract(Double s) {
		return this.add(-s);
	}
	@Override
	public WeightedBipolarSentiment subtractInplace(Double s) {
		return this.addInplace(-s);
	}
	/**
	 * @return whether any weight is NaN
	 */
	public boolean containsNaN() {
		return new Double(this.negative).isNaN() || new Double(this.neutral).isNaN() || new Double(this.positive).isNaN();
	}
	/**
	 * @return log the weights in place
	 */
	public WeightedBipolarSentiment logInplace() {
		if(this.negative > 0) this.negative = Math.log(negative);
		if(this.positive > 0) this.positive = Math.log(positive);
		if(this.neutral > 0 ) this.neutral = Math.log(neutral);
		return this;
	}
	/**
	 * @param neutral
	 */
	public void neutral(double neutral) {
		this.neutral = neutral;
	}
	/**
	 * @param negative
	 */
	public void negative(double negative) {
		this.negative = negative;
	}
	/**
	 * @param positive
	 */
	public void positive(double positive) {
		this.positive = positive;
	}
	public void correctNaN(double d) {
		if(new Double(this.negative).isNaN()) this.negative = d;
		if(new Double(this.positive).isNaN()) this.positive = d;
		if(new Double(this.neutral).isNaN()) this.neutral = d;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof WeightedBipolarSentiment))return false;
		WeightedBipolarSentiment that = (WeightedBipolarSentiment)obj;
		return this.negative == that.negative && this.positive == that.positive && this.neutral == that.neutral;
	}
	public WeightedBipolarSentiment expInplace() {
		this.negative = Math.exp(negative);
		this.positive = Math.exp(positive);
		this.neutral = Math.exp(neutral);
		return this;
	}
	public WeightedBipolarSentiment clipMaxInplace(double d) {
		if(this.negative > d) this.negative = d;
		if(this.positive > d) this.positive = d;
		if(this.neutral > d) this.neutral = d;
		return this;
	}
}
