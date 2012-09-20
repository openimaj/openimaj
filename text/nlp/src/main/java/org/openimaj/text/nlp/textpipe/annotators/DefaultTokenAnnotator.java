package org.openimaj.text.nlp.textpipe.annotators;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.text.nlp.EntityTweetTokeniser;
import org.openimaj.text.nlp.TweetTokeniserException;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

public class DefaultTokenAnnotator extends AbstractTokenAnnotator{

	@Override
	public List<TokenAnnotation> tokenise(String text) {
		EntityTweetTokeniser t = null;
		try {
			t = new EntityTweetTokeniser(text);
		} catch (UnsupportedEncodingException e) {		
			e.printStackTrace();
		} catch (TweetTokeniserException e) {			
			e.printStackTrace();
		}
		List<TokenAnnotation> tla = new ArrayList<TokenAnnotation>();
		for(String token:t.getStringTokens()){
			tla.add(new TokenAnnotation(token,null, -1, -1));
		}
		return tla;
	}

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		// TODO Auto-generated method stub
		
	}	

}
