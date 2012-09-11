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

public class OpenNLPSentenceAnnotator extends AbstractSentenceAnnotator{
	
	SentenceDetectorME sentenceDetector;
	
	public OpenNLPSentenceAnnotator(){
		super();
		InputStream modelIn = null;
		modelIn = OpenNLPSentenceAnnotator.class.getClassLoader().getResourceAsStream(
				"org/openimaj/text/opennlp/models/en-sent.bin");
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
