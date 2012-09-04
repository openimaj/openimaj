package org.openimaj.text.nlp.textpipe.annotators;

import java.io.UnsupportedEncodingException;

import org.openimaj.text.nlp.EntityTweetTokeniser;
import org.openimaj.text.nlp.TweetTokeniserException;
import org.openimaj.text.nlp.textpipe.annotations.TokenAnnotation;
import org.openimaj.text.nlp.textpipe.annotations.TokenListAnnotation;

public class DefaultTokenAnnotator extends AbstractTokenAnnotator{

	@Override
	protected TokenListAnnotation tokenise(String text) {
		EntityTweetTokeniser t = null;
		try {
			t = new EntityTweetTokeniser(text);
		} catch (UnsupportedEncodingException e) {		
			e.printStackTrace();
		} catch (TweetTokeniserException e) {			
			e.printStackTrace();
		}
		TokenListAnnotation tla = new TokenListAnnotation();
		for(String token:t.getStringTokens()){
			tla.addTokenAnnotation(new TokenAnnotation(token, -1, -1));
		}
		return tla;
	}

}
