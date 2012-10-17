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
package org.openimaj.text.nlp.namedentity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.openimaj.experiment.evaluation.classification.BasicClassificationResult;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.roc.ROCAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.roc.ROCResult;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.EntityTweetTokeniser;
import org.openimaj.text.nlp.TweetTokeniserException;
import org.openimaj.text.nlp.namedentity.YagoEntityCandidateFinderFactory.YagoEntityCandidateFinder;
import org.openimaj.text.nlp.namedentity.YagoEntityContextScorerFactory.YagoEntityContextScorer;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Experiment for examining the ability of a
 * Yago based organisation extractor.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class YagoCompanyAnnotatorEvaluator {
	private static final String CLASSIFICATION = "Organistaion";
	private static BufferedWriter logOut;
	private static boolean logging;
	private DocumentBuilderFactory docBuilderFactory;
	private DocumentBuilder docBuilder;
	private Map<FileEntityLocation, Set<String>> actual;
	private Map<FileEntityLocation, ClassificationResult<String>> results;
	private final YagoEntityCompleteAnnotator ycca;
	private EntityTweetTokeniser tt; 
	private ClassificationEvaluator<ROCResult<String>, String, FileEntityLocation> ce;
	private ROCAnalyser<FileEntityLocation, String> ra;
	private boolean verbose=false;

	/**
	 * @param args
	 *            the first argument must be the alias list/index directory
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("You have not given me a directory for the Test data.");
			System.exit(1);
		}
		if(args.length==2){
			createLogging(args[1]);
			logging = true;
		}
		else{
			System.out.println("No logging file specified.");
			logging=false;
		}
		final YagoCompanyAnnotatorEvaluator ya = new YagoCompanyAnnotatorEvaluator();
		ya.run(args[0]);
	}

	private static void createLogging(String logFilePath) {
		File f = new File(logFilePath);
		if(!f.isFile()){
			try {
				f.createNewFile();				
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
		else{
		}
		FileWriter fstream = null; 
		try {
			fstream = new FileWriter(logFilePath);
			logOut = new BufferedWriter(fstream);
			logOut.write("");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * instantiates the annotator
	 */
	public YagoCompanyAnnotatorEvaluator() {
		YagoEntityCandidateFinder ycf = null;
		ycf = YagoEntityCandidateFinderFactory.createFromAliasFile(EntityExtractionResourceBuilder.getDefaultAliasFilePath());
		YagoEntityContextScorer ycs = null;
		ycs = YagoEntityContextScorerFactory.createFromIndexFile(EntityExtractionResourceBuilder.getDefaultIndexDirectoryPath());
		ycca = new YagoEntityCompleteAnnotator(ycs,ycf);
	}

	/**
	 * @param testDirectory
	 *            given a directory, run the evaluation
	 */
	public void run(String testDirectory) {
		System.out.println("Started....");
		buildTruthAndClassifications(testDirectory);
		ra = new ROCAnalyser<YagoCompanyAnnotatorEvaluator.FileEntityLocation, String>();
		ce = new ClassificationEvaluator<ROCResult<String>, String, FileEntityLocation>(results, actual, ra);
		final ROCResult<String> analysisResult = ce.analyse(ce.evaluate());
		System.out.println(analysisResult.getDetailReport());
		doMyCalcs();
		if(logging)
			try {
				logOut.flush();
				logOut.close();
			} catch (IOException e) {				
				e.printStackTrace();
			}
	}

	private void doMyCalcs() {
		double fp=0;
		double tp=0;
		double fn=0;
		for(FileEntityLocation fe:results.keySet()){
			if(actual.keySet().contains(fe))tp++;
			else fp++;
		}
		for(FileEntityLocation fe:actual.keySet()){
			if(!results.keySet().contains(fe))fn++;
		}
		System.out.println("Precision : "+(tp/(tp+fp)));
		System.out.println("Recall : "+(tp/(tp+fn)));
	}

	/**
	 * @param testDirectory
	 */
	private void buildTruthAndClassifications(String testDirectory) {
		final File f = new File(testDirectory);
		actual = new HashMap<FileEntityLocation, Set<String>>();
		results = new HashMap<FileEntityLocation, ClassificationResult<String>>();
		if (f.isDirectory()) {
			// Initialize XML parsing objects
			docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilder = null;
			try {
				docBuilder = docBuilderFactory.newDocumentBuilder();
			} catch (final ParserConfigurationException e) {				
				e.printStackTrace();
			}

			for (final File s : f.listFiles()) {
				final String name = s.getName();
				print("#################Processing " + name);
				if (name.substring(name.lastIndexOf(".") + 1).equals("xml")) {
					Document doc = null;
					try {
						doc = docBuilder.parse(s);
					} catch (final SAXException e) {
						
						e.printStackTrace();
					} catch (final IOException e) {						
						e.printStackTrace();
					}
					doc.getDocumentElement().normalize();
					final HashMap<Integer, String> res = getResultsFrom(doc.getElementsByTagName("TextWithNodes").item(0)
							.getTextContent(), s.getAbsolutePath());
					final HashMap<Integer, String> act = getActualFrom(doc.getElementsByTagName("TextWithNodes").item(0)
							.getTextContent(), doc.getElementsByTagName("AnnotationSet"), s.getAbsolutePath());
					print("---------MY MISSES----------");
					for (final int key : act.keySet()) {
						if (!res.keySet().contains(key)) {
							print(act.get(key));
						}
					}
					print("---------THEIR MISSES----------");
					for (final int key : res.keySet()) {
						if (!act.keySet().contains(key)) {
							print(res.get(key));
						}
					}
				}
			}
		}
	}

	private HashMap<Integer, String> getResultsFrom(String textContent, String filePath) {
		print("---------RESULTS----------");
		try {
			tt = new EntityTweetTokeniser(textContent);
		} catch (final UnsupportedEncodingException e) {		
			e.printStackTrace();
		} catch (final TweetTokeniserException e) {			
			e.printStackTrace();
		}
		final ArrayList<String> tokens = (ArrayList<String>) tt.getStringTokens();
		final List<ScoredAnnotation<HashMap<String, Object>>> annos = ycca.annotate(tokens);
		final HashMap<Integer, String> r = new HashMap<Integer, String>();
		for (final ScoredAnnotation<HashMap<String, Object>> anno : annos) {
			if (anno.annotation.get(EntityAnnotator.TYPE)==NamedEntity.Type.Organisation.toString()){
				final FileEntityLocation fe = getFE(anno, textContent, tokens);
				final BasicClassificationResult<String> c = new BasicClassificationResult<String>();
				c.put(CLASSIFICATION, 1);
				fe.file = filePath;
				results.put(fe, c);
				if (fe.start >= 0 && fe.start < textContent.length()
						&& fe.stop >= 0 && fe.stop < textContent.length()
						&& fe.stop > fe.start) {
					final String s = textContent.substring(fe.start, fe.stop)
							+ " " + fe.start + ", " + fe.stop;
					r.put(fe.start + fe.stop, s);
					print(s);
				} else
					System.err.println("Substring out of range for :"
							+ anno.annotation.get(EntityAnnotator.URI));
			}
			//else System.out.println("Skipped person : "+anno.annotation.get(EntityAnnotator.URI));
		}
		return r;
	}

	private FileEntityLocation getFE(ScoredAnnotation<HashMap<String, Object>> anno, String textContent,
			ArrayList<String> tokens)
	{
		// calculate the start char index
		final int sInd = (Integer) anno.annotation.get(EntityAnnotator.START_TOKEN);
		final String sToken = tokens.get(sInd);
		// join all previous tokens with empty and get length
		int minStartChar = StringUtils.join(tokens.subList(0, sInd), "").length();
		// get the index of the first occurrence of the token after the minimum
		int startCharOff = textContent.substring(minStartChar).indexOf(sToken);
		final int startChar = minStartChar + startCharOff;
		// calculate the end char index
		final int eInd = (Integer) anno.annotation.get(EntityAnnotator.END_TOKEN);
		final String eToken = tokens.get(eInd);
		minStartChar = StringUtils.join(tokens.subList(0, eInd), "").length();
		startCharOff = textContent.substring(minStartChar).indexOf(eToken);
		final int endChar = minStartChar + startCharOff + eToken.length();
		final FileEntityLocation fe = new FileEntityLocation();
		fe.start = startChar;
		fe.stop = endChar;
		return fe;
	}

	private HashMap<Integer, String> getActualFrom(String textContent, NodeList anoSets, String filePath) {
		print("---------Actual----------");
		final HashSet<String> c = new HashSet<String>();
		c.add(CLASSIFICATION);
		final HashMap<Integer, String> r = new HashMap<Integer, String>();
		for (int i = 0; i < anoSets.getLength(); i++) {
			final Node n = anoSets.item(i);
			final NamedNodeMap m = n.getAttributes();
			if (m.getNamedItem("Name") != null && m.getNamedItem("Name").getNodeValue().equals("Key")) {
				final NodeList anoChildren = n.getChildNodes();
				for (int j = 0; j < anoChildren.getLength(); j++) {
					final Node child = anoChildren.item(j);
					if (child.hasAttributes() && child.getAttributes().getNamedItem("Type") != null
							&& child.getAttributes().getNamedItem("Type").getNodeValue().equals("Organization"))
					{
						final int startchar = Integer.parseInt(child.getAttributes().getNamedItem("StartNode")
								.getNodeValue());
						final int endchar = Integer
								.parseInt(child.getAttributes().getNamedItem("EndNode").getNodeValue());
						final FileEntityLocation fe = new FileEntityLocation();
						fe.file = filePath;
						fe.start = startchar;
						fe.stop = endchar;
						actual.put(fe, c);
						final String s = textContent.substring(fe.start, fe.stop) + " " + fe.start + ", " + fe.stop;
						r.put(fe.start + fe.stop, s);
						print(s);
					}
				}
			}
		}
		return r;
	}

	/**
	 * @param path to the Gate document
	 * @return plain text of document.
	 */
	public static String getRawStringFromTest(String path) {
		final File f = new File(path);
		Document doc = null;
		DocumentBuilderFactory factory = null;
		DocumentBuilder docBuilder = null;
		// Initialize XML parsing objects
		factory = DocumentBuilderFactory.newInstance();
		docBuilder = null;
		try {
			docBuilder = factory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {			
			e.printStackTrace();
		}
		try {
			doc = docBuilder.parse(f);
		} catch (final SAXException e) {			
			e.printStackTrace();
		} catch (final IOException e) {			
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
		return doc.getElementsByTagName("TextWithNodes").item(0).getTextContent();
	}
	
	private void print(String message){
		if(verbose)System.out.println(message);
		if(logging)
			try {
				logOut.append(message+"\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	/**
	 * An object which uniquely identifies and equates a start/stop in a
	 * specific file.
	 * 
	 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class FileEntityLocation {
		String file;
		int start;
		int stop;

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((file == null) ? 0 : file.hashCode());
			result = prime * result + start;
			result = prime * result + stop;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof FileEntityLocation))
				return false;
			final FileEntityLocation comp = (FileEntityLocation) obj;
			if (!comp.file.equals(this.file))
				return false;
			if (comp.start != this.start)
				return false;
			if (comp.stop != this.stop)
				return false;
			return true;
		}

	}

}
