package org.openimaj.text.nlp.textpipe.annotators;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.text.nlp.EntityTweetTokeniser;
import org.openimaj.text.nlp.TweetTokeniserException;
import org.openimaj.text.nlp.textpipe.annotations.RawTextAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;

public class DefaultTokenAnnotator extends AbstractTokenAnnotator<DefaultTokenAnnotator>{

	@Override
	public List<TokenAnnotation<DefaultTokenAnnotator>> tokenise(String text) {
		EntityTweetTokeniser t = null;
		try {
			t = new EntityTweetTokeniser(text);
		} catch (UnsupportedEncodingException e) {		
			e.printStackTrace();
		} catch (TweetTokeniserException e) {			
			e.printStackTrace();
		}
		List<TokenAnnotation<DefaultTokenAnnotator>> tla = new ArrayList<TokenAnnotation<DefaultTokenAnnotator>>();
		for(String token:t.getStringTokens()){
			tla.add(new TokenAnnotation<DefaultTokenAnnotator>(token,null, -1, -1));
		}
		return tla;
	}

	@Override
	void checkForRequiredAnnotations(RawTextAnnotation annotation)
			throws MissingRequiredAnnotationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String deTokenise(List<TokenAnnotation<DefaultTokenAnnotator>> tokens) {
		// TODO Auto-generated method stub
		return null;
	}

}
