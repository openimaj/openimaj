package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.feature.FeatureExtractor;

public class TfIdfFeatureExtractor implements FeatureExtractor<ArrayList<Double>, List<String>>{
	
	private HashMap<String,ArrayList<Double>> docTfIdfVectors;
	private HashMap<String,Double> termIDF;
	HashMap<String, TermCounter> counters;
	TermCounter global;
	
	public TfIdfFeatureExtractor (Map< String,List<String>> tokenisedCorpus){
		this.docTfIdfVectors = new HashMap<String, ArrayList<Double>>();
		this.termIDF = new HashMap<String, Double>();	
		counters = new HashMap<String, TfIdfFeatureExtractor.TermCounter>();
		global = new TermCounter();
		
		//Count all the terms in each document and in the corpus.
		for (String me : tokenisedCorpus.keySet()) {
			counters.put(me, new TermCounter());
			for (String term : tokenisedCorpus.get(me)) {
				addNullToAllExceptMe(term, me);
			}
		}		
		
		//Calculate the IDF for each term.
		for(String term : global.termCount.keySet()){			
			termIDF.put(term, calculateIdfOf(term));
		}	
		
		//Build the TF/IDF vectors for each document
		for (String me : tokenisedCorpus.keySet()) {
			ArrayList<Double> vector = new ArrayList<Double>();
			for (String term : global.termCount.keySet()) {
				vector.add(calculateTfOf(term,me)*termIDF.get(term));
			}
			docTfIdfVectors.put(me,vector);
		}			
	}
	
	private double calculateTfOf(String term, String docKey){
		if(counters.get(docKey).termCount.get(term).count>0){
			double count = counters.get(docKey).termCount.get(term).count;
			double total = counters.get(docKey).getTotal();
			return count/total;
		}
		return 0;
	}
	
	private double calculateIdfOf(String term) {
		int docCount=0;
		for(String docKey:counters.keySet()){
			if(counters.get(docKey).termCount.get(term).count>0)docCount++;
		}
		return Math.log(counters.size()/(1+docCount));
	}

	@Override
	public ArrayList<Double> extractFeature(List<String> object) {
		RestrictedTermCounter tc = new RestrictedTermCounter(global.termCount.keySet());
		for (String term : object) {
			tc.incrementTerm(term);
		}
		ArrayList<Double> vector = new ArrayList<Double>();
		for(String term:global.termCount.keySet()){
			if(tc.termCount.get(term).count>0){
				double count = tc.termCount.get(term).count;
				double total = tc.getTotal();
				double tf = count/total;				
				vector.add(tf*termIDF.get(term));
			}
			else vector.add(0.0);
		}
		return null;
	}
	
	private void addNullToAllExceptMe(String term, String me){
		for(String key:counters.keySet()){
			if(key.equals(me))counters.get(key).addIncrementTerm(term);
			else(counters.get(key)).addNullTerm(term);
		}
		global.addIncrementTerm(term);
	}

	public static class TermCounter{
		public LinkedHashMap<String,Counter> termCount;
		private double total = 0;
		
		public TermCounter(){
			termCount=new LinkedHashMap<String, Counter>();
		}
		
		public double getTotal() {
			return total;
		}

		public void addIncrementTerm(String term){
			if(termCount.containsKey(term)){				
				termCount.get(term).increment();
			}
			else{
				Counter c = new Counter();
				c.increment();
				termCount.put(term,c);
			}
			total++;
		}
		
		public void addNullTerm(String term){
			if(!termCount.containsKey(term))termCount.put(term, new Counter());
		}		
	}
	
	public static class RestrictedTermCounter{
		public LinkedHashMap<String,Counter> termCount;
		private double total = 0;
		
		public RestrictedTermCounter(Set<String> vocabulary){
			termCount=new LinkedHashMap<String, Counter>();
			for(String term:vocabulary){
				termCount.put(term, new Counter());
			}
		}
		
		public double getTotal() {
			return total;
		}

		public void incrementTerm(String term){
			if(termCount.containsKey(term))termCount.get(term).increment();
			total++;
		}			
	}
	
	public static class Counter{
		private int count;
		public Counter(){
			this.count=0;
		}
		public void increment(){
			count++;
		}
		public int count(){
			return count;
		}
	}
	
	public static void main(String[] args){
		TermCounter t = new TermCounter();
		for(String term : "what what do not get confused what".split(" ")){
			t.addIncrementTerm(term);
		}
		for (String term : t.termCount.keySet()) {
			System.out.println(t.termCount.get(term).count());
		}
	}

}
