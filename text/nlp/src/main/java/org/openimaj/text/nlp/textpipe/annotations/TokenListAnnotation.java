package org.openimaj.text.nlp.textpipe.annotations;

import java.util.ArrayList;
import java.util.List;

public class TokenListAnnotation extends TextPipeAnnotation{
	ArrayList<TokenAnnotation> tokens;
	
	public TokenListAnnotation(){
		super();
		tokens = new ArrayList<TokenAnnotation>();
	}
	
	public List<TokenAnnotation> getTokenAnnotations(){
		return tokens;
	}
	
	public List<String> getTokensAsStringList(){
		ArrayList<String> stringTokens = new ArrayList<String>();
		for (TokenAnnotation ta : tokens) {
			stringTokens.add(ta.getStringToken());
		}
		return stringTokens;
	}
	
	public void addTokenAnnotation(TokenAnnotation token){
		tokens.add(token);
	}

}
