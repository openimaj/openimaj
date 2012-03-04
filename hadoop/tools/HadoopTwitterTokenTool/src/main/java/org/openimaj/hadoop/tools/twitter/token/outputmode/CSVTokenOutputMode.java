package org.openimaj.hadoop.tools.twitter.token.outputmode;

import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.hadoop.tools.twitter.token.outputmode.TwitterTokenOutputModeOption.DataMode;
import org.openimaj.util.pair.IndependentPair;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Output tokens in CSV mode
 * @author ss
 *
 */
public class CSVTokenOutputMode implements TwitterTokenOutputMode {
	Set<String> features = new HashSet<String>();
	Set<String> vectors = new HashSet<String>();
	
	Map<String,Map<String,Double>> lineColValues = new HashMap<String,Map<String,Double>>();
	private boolean vectorPerLine;
	private double defaultValue;
	
	
	/**
	 * @param dmode
	 * @param emptyValue 
	 */
	public CSVTokenOutputMode(DataMode dmode, double emptyValue) {
		switch (dmode) {
		case COLMAJOR:
			vectorPerLine = false;
			break;
		case ROWMAJOR:
			vectorPerLine = true;
			break;
		}
		this.defaultValue = emptyValue;
	}

	@Override
	public void acceptVect(String vectorName,List<IndependentPair<String, Double>> featureValues) {
		vectors.add(vectorName);
		for (IndependentPair<String, Double> featureValue: featureValues) {
			String featureName = featureValue.firstObject();
			features.add(featureValue.firstObject());
			recordValue(vectorName,featureName,featureValue.secondObject());
		}
	}

	@Override
	public void acceptFeat(String featureName,List<IndependentPair<String, Double>> vectorValues) {
		features.add(featureName);
		for (IndependentPair<String, Double> vectorValue: vectorValues) {
			String vectorName = vectorValue.firstObject();
			vectors.add(vectorValue.firstObject());
			recordValue(vectorName,featureName,vectorValue.secondObject());
		}
	}
	
	private void recordValue(String vectorName, String featureName,Double value) {
		String lineString;
		String colString;
		if(vectorPerLine){
			lineString = vectorName;
			colString = featureName;
		}
		else{
			colString = vectorName;
			lineString = featureName;
		}
		Map<String, Double> featureValues = lineColValues .get(lineString);
		if(featureValues == null){
			lineColValues.put(lineString, featureValues = new HashMap<String,Double>());
		}
		featureValues.put(colString, value);
	}
	
	@Override
	public void write(Writer output){
		Set<String> lineStrings = null;
		Set<String> colStrings = null;
		if(this.vectorPerLine){
			lineStrings = this.vectors;
			colStrings = this.features;
		}
		else{
			lineStrings = this.features;
			colStrings = this.vectors;
		}
		
		CSVWriter csvWriter = new CSVWriter(output);
		// Write the header for the columns
		String[] lineHolder = new String[colStrings.size() + 1];
		lineHolder[0] = "";
		int i = 1;
		for (String colString : colStrings) {
			lineHolder[i++] = colString;
		}
		csvWriter.writeNext(lineHolder);
		for (String line : lineStrings) {
			Map<String, Double> lineValues = this.lineColValues.get(line);
			i = 0;
			lineHolder[i++] = line;
			for (String col : colStrings) {
				Double val = lineValues.get(col);
				if(val == null){
					val = defaultValue;
				}
				lineHolder[i++] = "" + val;
			}
			csvWriter.writeNext(lineHolder);
		}
	}
}
