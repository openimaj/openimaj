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
package org.openimaj.text.nlp.language;

import gnu.trove.TIntDoubleHashMap;
import gnu.trove.TIntFloatHashMap;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntProcedure;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.openimaj.io.IOUtils;
import org.openimaj.io.WriteableBinary;
import org.openimaj.io.wrappers.ReadWriteableByte;
import org.openimaj.util.pair.IndependentPair;

import Jama.Matrix;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Short text language detection ported from langid: https://github.com/saffsd/langid.py
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
@SuppressWarnings("unused")
public class LanguageDetector {
	
	private static Gson gson;

	static{
		gson = new GsonBuilder().
			serializeNulls().
			create();
	}
	
	
	/**
	 * default location of the compressed json version language model
	 */
	public static final String LANGUAGE_MODEL_JSON = "/org/openimaj/text/language/language.model.json.gz";
	/**
	 * default location of the compressed binary version of the language model 
	 */
	public static final String LANGUAGE_MODEL_BINARY = "/org/openimaj/text/language/language.model.binary.gz";
	
	private LanguageModel languageModel;

	/**
	 * Load a language model from {@value #LANGUAGE_MODEL_BINARY}
	 * @throws IOException 
	 */
	
	public LanguageDetector() throws IOException {
		this(false);
	}
	
	@SuppressWarnings("unchecked")
	private void loadFromJSON() throws IOException {
		Map<String,Object> languageModelRaw;
		InputStream is = new GZIPInputStream(LanguageDetector.class.getResourceAsStream(LANGUAGE_MODEL_JSON));
		languageModelRaw = gson.fromJson(new InputStreamReader(is), Map.class);
		languageModel = new LanguageModel(languageModelRaw);
	}
	
	private void loadFromBinary() throws IOException {
		this.languageModel = IOUtils.read(
				new GZIPInputStream(LanguageDetector.class.getResourceAsStream(LANGUAGE_MODEL_BINARY)), 
				LanguageModel.class
		);
	}

	/**
	 * Create a language detector with a provided language model
	 * 
	 * @param model
	 */
	public LanguageDetector(LanguageModel model) {
		this.languageModel = model;
	}

	LanguageDetector(boolean fromJSON) throws IOException {
		if(fromJSON){
			loadFromJSON();
		}
		else{
			loadFromBinary();
		}
	}

	/**
	 * 
	 * A langauge with an associated confidence 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
	 *
	 */
	public static class WeightedLocale{
		/**
		 * Default constructor
		 * @param language
		 * @param best
		 */
		public WeightedLocale(String language, double best) {
			this.language = language;
			this.confidence = best;
		}
		
		@Override
		public String toString(){
			return String.format("%s: %f",this.language.toString(), this.confidence);
		}
		
		/**
		 * @return the locale based on the language
		 */
		public Locale getLocale(){
			return new Locale(language);
		}
		
		/**
		 * @return this weighted locale as a map
		 */
		public Map<String, Object> asMap(){
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("language", language);
			map.put("confidence", confidence);
			return map;
		}
		
		/**
		 * @param map
		 * @return Construct a weighted locale from a map
		 */
		public static WeightedLocale fromMap(Map<String,Object> map){
			return new WeightedLocale((String)map.get("language"),(Double)map.get("confidence"));
		}
		
		/**
		 * Estimated language
		 */
		public String language;
		
		/**
		 * Naive bayesian probability
		 */
		public double confidence;
	}
	
	/**
	 * Classify the language using a naive-bayes model
	 * 
	 * @param text
	 * @return the detected language
	 */
	public WeightedLocale classify(String text){
		Matrix fv = tokenize(text);
		WeightedLocale locale = naiveBayesClassify(fv);
		return locale;
	}

	private WeightedLocale naiveBayesClassify(Matrix fv) {
		
		double logFVSum = sumLogFactorial(fv);
		Matrix pdc = fv.times(this.languageModel.naiveBayesPTC);
//		multiplied.print(5, 5);
//		this.languageModel.naiveBayesPTC.print(5, 5);
		Matrix pd = pdc.plus(this.languageModel.naiveBayesPC);
		double[][] pdData = pd.getArray();
		int bestIndex = -1;
		double best = 0;
		double sum = 0;
		for (int i = 0; i < pd.getColumnDimension(); i++) {
			double correctedScore = pdData[0][i] - logFVSum ;
//			System.out.format("%s scores %f \n",this.languageModel.naiveBayesClasses[i],correctedScore);
			sum +=correctedScore;
			if(bestIndex == -1 || correctedScore > best)
			{
				bestIndex = i;
				best = correctedScore;
			}
		}
		
		return new WeightedLocale(this.languageModel.naiveBayesClasses[bestIndex],best/sum);
	}
	
	// an element wise log-factorial
	TIntDoubleHashMap logFacCache = new TIntDoubleHashMap();
	private double sumLogFactorial(Matrix fv) {
		double sum = 0;
		double[][] data = fv.getArray();
		for (int i = 0; i < fv.getColumnDimension(); i ++) {
			int fvi = (int) data[0][i];
			if(logFacCache.contains(fvi))
			{
				sum += logFacCache.get(fvi);
			}
			else{
				for(int j = 1; j < fvi+1; j++){
					sum += Math.log(j);
				}
			}
		}
		return sum;
	}

	private Matrix tokenize(String text) {
		byte[] ords = null;
		try {
			ords = text.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
		}
		int state = 0;
		TIntIntHashMap statecount = new TIntIntHashMap();
		for (byte letter : ords) {
			state = this.languageModel.tk_nextmove[(state << 8) + (letter & 0xff)];
			statecount.adjustOrPutValue(state, 1, 1);
		}
		final double[][] fv = new double[1][this.languageModel.naiveBayesNFeats];
		statecount.forEachEntry(new TIntIntProcedure() {
			@Override
			public boolean execute(int state, final int statecount) {
				int[] indexes = LanguageDetector.this.languageModel.tk_output.get(state);
				if(indexes == null) return true;
				for (int i : indexes) {
					
					fv[0][i] += statecount;
				}
				return true;
			}
		});
		return Matrix.constructWithCopy(fv);
	}

	public LanguageModel getLanguageModel() {
		return this.languageModel;
		
	}
}
