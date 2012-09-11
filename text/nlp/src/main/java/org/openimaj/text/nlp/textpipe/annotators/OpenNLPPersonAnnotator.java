package org.openimaj.text.nlp.textpipe.annotators;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

import org.openimaj.text.nlp.namedentity.NamedEntity;
import org.openimaj.text.nlp.textpipe.annotations.AnnotationUtils;
import org.openimaj.text.nlp.textpipe.annotations.NamedEntityAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.SentenceAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

public class OpenNLPPersonAnnotator extends AbstractNEAnnotator {

	NameFinderME nameFinder;

	public OpenNLPPersonAnnotator() {
		super();
		TokenNameFinderModel model = null;
		InputStream modelIn = OpenNLPPersonAnnotator.class.getClassLoader()
				.getResourceAsStream(
						"org/openimaj/text/opennlp/models/en-ner-person.bin");

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
			    	NamedEntityAnnotation nea = new NamedEntityAnnotation();
			    	NamedEntity ne = new NamedEntity();
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
