package org.openimaj.text.nlp.namedentity;

import java.io.File;
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

import org.openimaj.experiment.evaluation.classification.BasicClassificationResult;
import org.openimaj.experiment.evaluation.classification.ClassificationEvaluator;
import org.openimaj.experiment.evaluation.classification.ClassificationResult;
import org.openimaj.experiment.evaluation.classification.analysers.roc.ROCAnalyser;
import org.openimaj.experiment.evaluation.classification.analysers.roc.ROCResult;
import org.openimaj.ml.annotation.ScoredAnnotation;
import org.openimaj.text.nlp.TweetTokeniser;
import org.openimaj.text.nlp.TweetTokeniserException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.stanford.nlp.util.StringUtils;

/**
 * TODO: Laurence, fix javadocs here Experiement for examining the ability of a
 * Yago based organisation extractor
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class YagoCompanyAnnotatorEvaluator {
	private static final String CLASSIFICATION = "Organistaion";
	private DocumentBuilderFactory docBuilderFactory;
	private DocumentBuilder docBuilder;
	private Map<FileEntityLocation, Set<String>> actual;
	private final Map<FileEntityLocation, ClassificationResult<String>> results;
	private final EntityDisambiguatedAnnotator ycca;
	private TweetTokeniser tt;
	private final double threshold = 0.0;
	private ClassificationEvaluator<ROCResult<String>, String, FileEntityLocation> ce;
	private ROCAnalyser<FileEntityLocation, String> ra;

	/**
	 * @param args
	 *            the first argument must be the alias list/index directory
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("You have not given me a directory");
			System.exit(1);
		}
		final YagoCompanyAnnotatorEvaluator ya = new YagoCompanyAnnotatorEvaluator();
		ya.run(args[0]);
	}

	/**
	 * instnatiates the annotator
	 */
	public YagoCompanyAnnotatorEvaluator() {
		EntityAliasAnnotator ylca = null;
		try {
			ylca = new EntityAliasAnnotator(new YagoLookupMapFactory(true).createFromListFile(YagoLookupMapFileBuilder
					.getDefaultMapFilePath()));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		EntityContextAnnotator ywca = null;
		try {
			ywca = new EntityContextAnnotator(new YagoWikiIndexFactory(true).createFromIndexFile(YagoWikiIndexBuilder
					.getDefaultMapFilePath()));
		} catch (final IOException e) {
			e.printStackTrace();
		}
		ycca = new EntityDisambiguatedAnnotator(threshold, ylca, ywca);
		results = new HashMap<FileEntityLocation, ClassificationResult<String>>();
	}

	/**
	 * @param testDirectory
	 *            given a directory, run the evaluation
	 */
	public void run(String testDirectory) {
		buildTruthAndClassifications(testDirectory);
		ra = new ROCAnalyser<YagoCompanyAnnotatorEvaluator.FileEntityLocation, String>();
		ce = new ClassificationEvaluator<ROCResult<String>, String, FileEntityLocation>(results, actual, ra);
		final ROCResult<String> analysisResult = ce.analyse(ce.evaluate());
		System.out.println(analysisResult.getDetailReport());
	}

	/**
	 * @param testDirectory
	 */
	public void buildTruthAndClassifications(String testDirectory) {
		final File f = new File(testDirectory);
		actual = new HashMap<FileEntityLocation, Set<String>>();
		if (f.isDirectory()) {
			// Initialize XML parsing objects
			docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilder = null;
			try {
				docBuilder = docBuilderFactory.newDocumentBuilder();
			} catch (final ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (final File s : f.listFiles()) {
				final String name = s.getName();
				System.out.println("#################Processing " + name);
				if (name.substring(name.lastIndexOf(".") + 1).equals("xml")) {
					Document doc = null;
					try {
						doc = docBuilder.parse(s);
					} catch (final SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (final IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					doc.getDocumentElement().normalize();
					final HashMap<Integer, String> res = getResultsFrom(doc.getElementsByTagName("TextWithNodes").item(0)
							.getTextContent(), s.getAbsolutePath());
					final HashMap<Integer, String> act = getActualFrom(doc.getElementsByTagName("TextWithNodes").item(0)
							.getTextContent(), doc.getElementsByTagName("AnnotationSet"), s.getAbsolutePath());
					System.out.println("---------MY MISSES----------");
					for (final int key : act.keySet()) {
						if (!res.keySet().contains(key)) {
							System.out.println(act.get(key));
						}
					}
					System.out.println("---------THEIR MISSES----------");
					for (final int key : res.keySet()) {
						if (!act.keySet().contains(key)) {
							System.out.println(res.get(key));
						}
					}
				}
			}
		}
	}

	private HashMap<Integer, String> getResultsFrom(String textContent, String filePath) {
		System.out.println("---------RESULTS----------");
		try {
			tt = new TweetTokeniser(textContent);
		} catch (final UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final TweetTokeniserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final ArrayList<String> tokens = (ArrayList<String>) tt.getStringTokens();
		final List<ScoredAnnotation<HashMap<String, Object>>> annos = ycca.annotate(tokens);
		final HashMap<Integer, String> r = new HashMap<Integer, String>();
		for (final ScoredAnnotation<HashMap<String, Object>> anno : annos) {
			final FileEntityLocation fe = getFE(anno, textContent, tokens);
			final BasicClassificationResult<String> c = new BasicClassificationResult<String>();
			c.put(CLASSIFICATION, 1);
			fe.file = filePath;
			results.put(fe, c);
			if (fe.start >= 0 && fe.start < textContent.length() && fe.stop >= 0 && fe.stop < textContent.length()
					&& fe.stop > fe.start)
			{
				final String s = textContent.substring(fe.start, fe.stop) + " " + fe.start + ", " + fe.stop;
				r.put(fe.start + fe.stop, s);
				System.out.println(s);
			} else
				System.err.println("Substring out of range for :" + anno.annotation.get(EntityContextAnnotator.URI));
		}
		return r;
	}

	private FileEntityLocation getFE(ScoredAnnotation<HashMap<String, Object>> anno, String textContent,
			ArrayList<String> tokens)
	{
		// calculate the start char index
		final int sInd = (Integer) anno.annotation.get(EntityAliasAnnotator.START_TOKEN);
		final String sToken = tokens.get(sInd);
		// join all previous tokens with empty and get length
		int minStartChar = StringUtils.join(tokens.subList(0, sInd), "").length();
		// get the index of the first occurrence of the token after the minimum
		int startCharOff = textContent.substring(minStartChar).indexOf(sToken);
		final int startChar = minStartChar + startCharOff;
		// calculate the end char index
		final int eInd = (Integer) anno.annotation.get(EntityAliasAnnotator.END_TOKEN);
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
		System.out.println("---------Actual----------");
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
						System.out.println(s);
					}
				}
			}
		}
		return r;
	}

	/**
	 * @param path
	 * @return ground truths?
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			doc = docBuilder.parse(f);
		} catch (final SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
		return doc.getElementsByTagName("TextWithNodes").item(0).getTextContent();
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
