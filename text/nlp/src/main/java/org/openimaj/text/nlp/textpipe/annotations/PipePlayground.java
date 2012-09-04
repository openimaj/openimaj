package org.openimaj.text.nlp.textpipe.annotations;

import org.openimaj.text.nlp.textpipe.annotators.DefaultTokenAnnotator;

public class PipePlayground {
	
	public static void main(String[] args) {
		
		RawTextAnnotation rta = new RawTextAnnotation("Hello there, this is the first sentence.");
		DefaultTokenAnnotator ta = new DefaultTokenAnnotator();
		ta.annotate(rta);
		for(String token:rta.getAnnotation(TokenListAnnotation.class).getTokensAsStringList()){
			System.out.println(token);
		}
	}

}
