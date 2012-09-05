package org.openimaj.text.nlp.textpipe.annotators;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import org.openimaj.text.nlp.textpipe.annotations.POSAnnotation.PartOfSpeech;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.SentenceAnnotation;

public class OpenNLPPOSAnnotator extends AbstractPOSAnnotator {

	POSTaggerME tagger;

	public OpenNLPPOSAnnotator() {
		super();
		InputStream modelIn = null;
		POSModel model = null;
		try {
			modelIn = OpenNLPPOSAnnotator.class
					.getClassLoader()
					.getResourceAsStream(
							"org/openimaj/text/opennlp/models/en-pos-maxent.bin");
			model = new POSModel(modelIn);
		} catch (IOException e) {
			// Model loading failed, handle the error
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}
		tagger = new POSTaggerME(model);
	}

	@Override
	public void annotate(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		if (!annotation.getAnnotationKeyList().contains(
				SentenceAnnotation.class))
			throw new MissingRequiredAnnotationException(
					"No SentenceAnnotations found : OpenNLPPOSAnnotator requires sentance splitting");
		super.annotate(annotation);
	}

	@Override
	protected List<PartOfSpeech> pos(List<String> tokenList) {
		List<PartOfSpeech> result = new ArrayList<PartOfSpeech>();
		String[] p = null;
		String[] sentence = new String[tokenList.size()];
		for (int i = 0; i < sentence.length; i++) {
			sentence[i]=tokenList.get(i);
		}
		p = tagger.tag(sentence);
		for(String pos:p){
			if(PartOfSpeech.getPOSfromString(pos)==null)System.out.println("no matching pos "+pos);
			result.add(PartOfSpeech.getPOSfromString(pos));
		}
		return result;
	}

}
