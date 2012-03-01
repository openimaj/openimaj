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
package org.openimaj.experiments;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.imageterrier.basictools.BasicTerrierConfig;
import org.imageterrier.locfile.QLFDocument;
import org.imageterrier.querying.parser.QLFDocumentQuery;
import org.imageterrier.toolopts.MatchingModelType;
import org.openimaj.experiments.stats.FeatureStatsPrinter;
import org.openimaj.experiments.stats.FeatureStatsPrinter.StatsOperation;
import org.openimaj.feature.local.list.FileLocalFeatureList;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.feature.local.keypoints.quantised.QuantisedKeypoint;
import org.terrier.matching.ResultSet;
import org.terrier.querying.Manager;
import org.terrier.querying.SearchRequest;
import org.terrier.structures.Index;
import org.terrier.utility.ApplicationSetup;

public class UKBenchExperiment {
	private static final int UKBENCH_LIMIT = 4;

	static{
		BasicTerrierConfig.configure();
	}
	private Index index;
	private Manager manager;
	private String quantisedExt;
	private String featureExt;
	private File quantisedBase;
	private List<String> ukBenchTest;
	private Map<String,List<String>> correct;
	private Map<String, Integer> scores;
	private List<FeatureStatsPrinter> statsList;
	private String imageExt;
	private PrintStream resultStream;
	private static final String UKBENCH_TEMPLATE = "ukbench%05d.%s";
	private static final MBFImage UKBENCH_IMAGE = new MBFImage(640,480,3);
	private static final String STATS_DELIM = " ";
	/**
	 * Load an index and prepare to query using the UKBench scheme finding quantised files in the location with the given extention
	 * @param indexFile
	 * @param quantisedBase
	 * @param quantisedExt
	 * @param featureExt 
	 * @param imageExt 
	 * @param resultStream 
	 */
	public UKBenchExperiment(File indexFile, File quantisedBase, String quantisedExt, String featureExt, String imageExt, PrintStream resultStream){
		index = Index.createIndex(indexFile.getAbsolutePath(), "index");
		manager = new Manager(index);
		this.quantisedBase = quantisedBase;
		this.quantisedExt = quantisedExt;
		this.featureExt = featureExt;
		this.imageExt = imageExt;
		this.correct = new HashMap<String,List<String>>();
		this.resultStream = resultStream;
		scores = new HashMap<String,Integer>();
		statsList = new ArrayList<FeatureStatsPrinter>();
		statsList.add(FeatureStatsPrinter.FEATURE_COUNT);
		statsList.add(FeatureStatsPrinter.FEATURE_COUNT_NORM);
		statsList.add(FeatureStatsPrinter.MATCHING_FEATURES);
		statsList.add(FeatureStatsPrinter.SELF_SIMILAR_FEATURES);
	}
	
	public void prepareExperiment(){
		this.ukBenchTest = new ArrayList<String>();
		for(int i = 20; i < 10200; i++){
			String query = String.format(UKBENCH_TEMPLATE,i,this.imageExt);
			ukBenchTest.add(query);
			int base = (i / 4) * 4;
			ArrayList<String> queryCorrect = new ArrayList<String>();
			queryCorrect.add(String.format(UKBENCH_TEMPLATE, base, this.imageExt));
			queryCorrect.add(String.format(UKBENCH_TEMPLATE, base+1, this.imageExt));
			queryCorrect.add(String.format(UKBENCH_TEMPLATE, base+2, this.imageExt));
			queryCorrect.add(String.format(UKBENCH_TEMPLATE, base+3, this.imageExt));
			correct.put(query,queryCorrect);
		}
	}
	
	public void doExperiment() throws IOException{
		for(String testString : ukBenchTest){
//			System.out.println("Querying: " + testString);
			File siftFile = new File(quantisedBase,testString + "." + this.featureExt);
			File quantisedFile = new File(quantisedBase,testString + "." + this.quantisedExt);
			
			FileLocalFeatureList<Keypoint> siftFeatures = FileLocalFeatureList.read(siftFile, Keypoint.class);
			List<QuantisedKeypoint> quantisedFeatures = FileLocalFeatureList.read(quantisedFile, QuantisedKeypoint.class);
			
			QLFDocument<QuantisedKeypoint> d = new QLFDocument<QuantisedKeypoint>(quantisedFeatures, "query", null);
			QLFDocumentQuery<QuantisedKeypoint> q = new QLFDocumentQuery<QuantisedKeypoint>(d);
			
			SearchRequest request = manager.newSearchRequest("foo");
			request.setQuery(q);
			MatchingModelType.L1IDF.configureRequest(request, q);
			ApplicationSetup.setProperty("ignore.low.idf.terms","false");
			ApplicationSetup.setProperty("matching.retrieved_set_size", ""+UKBENCH_LIMIT);
			
			manager.runPreProcessing(request);
			manager.runMatching(request);
			manager.runPostProcessing(request);
			manager.runPostFilters(request);
			
			ResultSet results = request.getResultSet();
			List<String> correctList = correct.get(testString);
			int score = 0;
			for (int i=0; i<UKBENCH_LIMIT; i++) {
				File file = getFile(results.getDocids()[i]);

				if (results.getScores()[i] <= 0) break; //filter 0 results 
				
//				System.out.format("Looking for %s in %s\n",file.getName(),correctList);
				if(correctList.contains(file.getName())) score ++;
			}
			scores.put(testString,score);
			// initialise statistics being gathered
			List<StatsOperation> operations = FeatureStatsPrinter.getOperations(this.statsList);
			FeatureStatsPrinter.initOperations(operations,UKBENCH_IMAGE,siftFeatures);
			// Iterate through keypoints, collect statistics 
			for (Keypoint keypoint : siftFeatures) {
				FeatureStatsPrinter.gatherOperations(operations,keypoint);
			}
			
			// Start printing score and stats
			this.resultStream.print(testString);
			this.resultStream.print(STATS_DELIM);
			this.resultStream.print(score);
			FeatureStatsPrinter.outputOperations(operations, this.resultStream, STATS_DELIM);
			this.resultStream.println();
		}
	}

	public File getFile(int docid) throws IOException {
		return new File(index.getIndexProperty("index.image.base.path", "/") + index.getMetaIndex().getItem("path", docid).replace(".fv.loc", ""));
	}
	
	public static void main(String[] args) throws IOException {
		UKBenchExperiment exp = new UKBenchExperiment(
			new File("/Users/ss/Development/data/ukbench/index"),	
			new File("/Users/ss/Development/data/ukbench/data/full"),
			"fv.loc",
			"fv",
			"jpg",
			new PrintStream(new FileOutputStream(new File("/Users/ss/Development/data/ukbench/experiments/score-featurecount-matching-multimatch.txt")))
		);
		exp.prepareExperiment();
		exp.doExperiment();
	}
}
