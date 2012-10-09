package org.openimaj.text.nlp.textpipe.annotators;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

import org.openimaj.text.nlp.textpipe.annotations.AnnotationUtils;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.SentenceAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

/**
 * Uses a {@link TokenNameFinderModel} instnace to instanciate a {@link NameFinderME}
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class OpenNLPPersonAnnotator extends AbstractNEAnnotator {

	/**
	 * Property name containing the person model
	 */
	public static final String PERSON_MODEL_PROP = "org.openimaj.text.opennlp.models.person";
	NameFinderME nameFinder;

	/**
	 * loads a model from {@link OpenNLPPersonAnnotator#PERSON_MODEL_PROP}
	 */
	public OpenNLPPersonAnnotator() {
		super();
		TokenNameFinderModel model = null;
		String resourceLocation = System.getProperty(PERSON_MODEL_PROP);
		InputStream modelIn = OpenNLPPersonAnnotator.class.getResourceAsStream(resourceLocation);

		try {
			model = new TokenNameFinderModel(modelIn);
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
		nameFinder = new NameFinderME(model);
	}

	@Override
	void performAnnotation(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {


			  for (SentenceAnnotation sentence : annotation.getAnnotationsFor(SentenceAnnotation.class)) {
				  List<TokenAnnotation> atoks = sentence.getAnnotationsFor(TokenAnnotation.class);
				  List<String> toks = AnnotationUtils.getStringTokensFromTokenAnnotationList(atoks);
			    Span nameSpans[] = nameFinder.find(AnnotationUtils.ListToArray(toks));
			    for(Span s :nameSpans){
//			    	NamedEntityAnnotation nea = new NamedEntityAnnotation();
//			    	NamedEntity ne = new NamedEntity();
			    	for(int i = s.getStart();i<s.getEnd();i++){
			    		atoks.get(i).addAnnotation(annotation);
			    	}
			    }
			  }
			  nameFinder.clearAdaptiveData();

	}

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		// TODO Auto-generated method stub

	}

}
