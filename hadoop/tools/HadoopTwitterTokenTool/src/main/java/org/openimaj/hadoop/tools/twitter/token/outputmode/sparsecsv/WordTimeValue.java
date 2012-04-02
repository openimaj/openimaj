package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.openimaj.hadoop.tools.twitter.utils.WordDFIDFTimeSeries;
import org.openimaj.util.pair.IndependentPair;

public class WordTimeValue {
	
	public LinkedHashMap<Long, IndependentPair<Long, Long>> timeIndex;
	public LinkedHashMap<String, IndependentPair<Long, Long>> wordIndex;
	public LinkedHashMap<String, WordDFIDFTimeSeries> values;

	/**
	 * @param path
	 * @throws IOException 
	 */
	public WordTimeValue(String path) throws IOException{
		this.timeIndex = TimeIndex.readTimeCountLines(path);
		this.wordIndex = WordIndex.readWordCountLines(path);
		this.values = Values.readWordDFIDF(path,timeIndex,wordIndex);
	}
}
