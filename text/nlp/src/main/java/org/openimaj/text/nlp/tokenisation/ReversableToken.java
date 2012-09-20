package org.openimaj.text.nlp.tokenisation;

import java.util.List;

public interface ReversableToken{
	
	public String getRawString();
	
	public String reverse(List<? extends ReversableToken> tokens);
	
}
