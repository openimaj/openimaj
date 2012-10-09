package org.openimaj.text.nlp.textpipe.annotators;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.SentenceAnnotation;

/**
 * {@link SentenceDetectorME} backed by a {@link SentenceModel} loaded from the resource located at: {@link OpenNLPSentenceAnnotator#SENTENCE_MODEL_PROP}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class OpenNLPSentenceAnnotator extends AbstractSentenceAnnotator{

	/**
	 * Property name pointing to the sentence model
	 */
	public static final String SENTENCE_MODEL_PROP = "org.openimaj.text.opennlp.models.sent";
	SentenceDetectorME sentenceDetector;

	/**
	 *
	 */
	public OpenNLPSentenceAnnotator(){
		super();
		InputStream modelIn = null;
		modelIn = OpenNLPSentenceAnnotator.class.getResourceAsStream(System.getProperty(SENTENCE_MODEL_PROP));
		SentenceModel model=null;
		try {
		  model = new SentenceModel(modelIn);
		}
		catch (IOException e) {
		  e.printStackTrace();
		}
		finally {
		  if (modelIn != null) {
		    try {
		      modelIn.close();
		    }
		    catch (IOException e) {
		    }
		  }
		}
		sentenceDetector = new SentenceDetectorME(model);
	}

	@Override
	protected List<SentenceAnnotation> getSentenceAnnotations(String text) {
		ArrayList<SentenceAnnotation> sents = new ArrayList<SentenceAnnotation>();
		List<String> sentences = Arrays.asList(sentenceDetector.sentDetect(text));
		int currentOff =0;
		for(int i =0; i<sentences.size();i++){
			String sentence = sentences.get(i);
			int start=currentOff+(text.substring(currentOff).indexOf(sentence));
			int stop=start+sentence.length();
			sents.add(new SentenceAnnotation(sentence,start,stop));
		}
		return sents;
	}

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		// TODO Auto-generated method stub

	}

}
