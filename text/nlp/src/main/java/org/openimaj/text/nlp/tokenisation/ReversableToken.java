package org.openimaj.text.nlp.tokenisation;

import java.util.List;

public interface ReversableToken<TOKENISER extends ReversableTokeniser<TOKENISER,TOKEN>, TOKEN extends ReversableToken<TOKENISER,TOKEN>>{
	
	public Class<TOKENISER> getTokeniserClass();
	
	public String getRawString();
	
	public String reverse(List<ReversableToken> tokens);
	
}
