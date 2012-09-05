package org.openimaj.text.nlp.textpipe.annotators;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

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
		for(String sent : sentenceDetector.sentDetect(text)){
			sents.add(new SentenceAnnotation(sent));
		}
		return sents;
	}

}
