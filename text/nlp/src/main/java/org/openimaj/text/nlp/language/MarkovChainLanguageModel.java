package org.openimaj.text.nlp.language;

import gnu.trove.TObjectIntHashMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import Jama.Matrix;

/**
 * Code to train, classify and generate language specific text by building a first order markov chain.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class MarkovChainLanguageModel {
	
	private Map<Locale,Matrix> chains = new HashMap<Locale,Matrix>();
	private Map<Locale,long[]> chainCounts = new HashMap<Locale,long[]>();
	
	/**
	 * Generate a new empty markov chain language model
	 */
	public MarkovChainLanguageModel(){
		chains = new HashMap<Locale,Matrix>();
		chainCounts = new HashMap<Locale,long[]>();
	}
	
	/**
	 * 
	 * Add an example to a language's markov chain
	 * 
	 * @param language the language the example is being added to
	 * @param example the new example to learn from
	 * @param encoding the encoding of the example
	 * @throws UnsupportedEncodingException 
	 */
	public void train(Locale language, String example, String encoding) throws UnsupportedEncodingException{
		if(!chains.containsKey(language)){
			chains.put(language, new Matrix(256+1,256+1));
			chainCounts.put(language,new long[256+1]);
		}
		
		Matrix chain = chains.get(language);
		long[] chainCount = chainCounts.get(language);
		byte[] data = example.getBytes(encoding);
		
		int currentIndex = 0;
		double[][] chainData = chain.getArray();
		for (byte b : data) {
			int newIndex = (b & 0xff) + 1;
			chainData[currentIndex][newIndex] = chainData[currentIndex][newIndex] + 1;
			chainCount[currentIndex] += 1;
			currentIndex = newIndex;
		}
		
	}
	
	public void train(Locale language, InputStream stream) throws IOException {
		if(!chains.containsKey(language)){
			chains.put(language, new Matrix(256+1,256+1));
			chainCounts.put(language,new long[256+1]);
		}
		
		Matrix chain = chains.get(language);
		long[] chainCount = chainCounts.get(language);
		
		int currentIndex = 0;
		double[][] chainData = chain.getArray();
		int newIndex = -1;
		while ((newIndex = stream.read()) != -1) {
			newIndex += 1;
			chainData[currentIndex][newIndex] = chainData[currentIndex][newIndex] + 1;
			chainCount[currentIndex] += 1;
			currentIndex = newIndex;
		}
	}
	
	/**
	 * Generate a string using this model of the desired length
	 * @param language 
	 * 
	 * @param length
	 * @param encoding 
	 * @return the generated string
	 * @throws UnsupportedEncodingException 
	 */
	public String generate(Locale language, int length, String encoding) throws UnsupportedEncodingException{
		
		Matrix chain = this.chains.get(language);
		if(chain == null) return null;
		double[][] chainData = chain.getArray();
		long[] chainCount = this.chainCounts.get(language);
		
		int currentIndex = 0;
		byte[] newString = new byte[length];
		Random r = new Random();
		for (int i = 0; i < length; i++) {
			double prob = r.nextDouble();
			double[] currentLine = chainData[currentIndex];
			double probSum = 0.0;
			int newIndex = 0;
//			System.out.println("CURRENT STATE:" + (char)(currentIndex-1));
			while(probSum+(currentLine[newIndex]/ chainCount[currentIndex]) < prob){
				double probForIndex = (currentLine[newIndex++] / chainCount[currentIndex]);
//				System.out.println(probForIndex);
//				if(probForIndex > 0){
//					System.out.println("Prob to go to:" + (char)(newIndex-2) + " = " + probForIndex);
//				}
				probSum += probForIndex;
			}
//			System.out.println("NEW STATE:" + (char)(newIndex-1));
			newString[i] = (byte) (newIndex - 1);
			currentIndex = newIndex;
		}
		
		return new String(newString,encoding);
	}

	
}
