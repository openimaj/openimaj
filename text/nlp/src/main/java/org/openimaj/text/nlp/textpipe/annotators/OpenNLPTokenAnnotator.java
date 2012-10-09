package org.openimaj.text.nlp.textpipe.annotators;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class OpenNLPTokenAnnotator extends AbstractTokenAnnotator {

	/**
	 *
	 */
	public static final String TOKEN_MODEL_PROP = "org.openimaj.text.opennlp.models.token";
	TokenizerME tokenizer;

	/**
	 *
	 */
	public OpenNLPTokenAnnotator() {
		super();
		InputStream modelIn=null;
		modelIn = OpenNLPTokenAnnotator.class.getResourceAsStream(System.getProperty(TOKEN_MODEL_PROP));
		TokenizerModel model = null;
		try {
			model = new TokenizerModel(modelIn);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}
		tokenizer = new TokenizerME(model);
	}



	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {

	}


	@Override
	public List<TokenAnnotation> tokenise(String text) {
		List<TokenAnnotation> tla = new ArrayList<TokenAnnotation>();
		int currentOff =0;
		for(String token : tokenizer.tokenize(text)){
			int start = currentOff+(text.substring(currentOff).indexOf(token));
			int stop = start+token.length();
			tla.add(new TokenAnnotation(token,text.substring(currentOff,stop), start, stop));
			currentOff = stop;
		}
		return tla;
	}

}
