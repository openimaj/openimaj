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
		for(int i =0; i<sentences.size();i++){
			String sentence = sentences.get(i);
			int start=getOffset(sentences.subList(0, i));
			int stop=start+sentence.length();
			sents.add(new SentenceAnnotation(sentence,start,stop));
		}
		return sents;
	}
	
	

	private int getOffset(List<String> subList) {
		int result = 0;
		for(String sent : subList){
			result+=sent.length();
		}
		return result;
	}

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		// TODO Auto-generated method stub
		
	}

}
