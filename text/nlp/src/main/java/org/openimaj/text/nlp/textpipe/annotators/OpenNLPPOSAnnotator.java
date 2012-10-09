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

/**
 * Uses a {@link POSTaggerME} backed by a {@link POSModel}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class OpenNLPPOSAnnotator extends AbstractPOSAnnotator {

	/**
	 * Name of system property pointing to the POS model
	 */
	public static final String POS_MODEL_PROP = "org.openimaj.text.opennlp.models.pos";
	POSTaggerME tagger;

	/**
	 *
	 */
	public OpenNLPPOSAnnotator() {
		super();
		InputStream modelIn = null;
		POSModel model = null;
		try {
			modelIn = OpenNLPPOSAnnotator.class.getResourceAsStream(System.getProperty(POS_MODEL_PROP));
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

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		// TODO Auto-generated method stub

	}

}
