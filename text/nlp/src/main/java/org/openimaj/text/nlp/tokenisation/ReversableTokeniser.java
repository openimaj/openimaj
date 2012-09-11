package org.openimaj.text.nlp.tokenisation;

import java.util.List;

public interface ReversableTokeniser<
	TOKENISER extends ReversableTokeniser<TOKENISER,TOKEN>, 
	TOKEN extends ReversableToken<TOKENISER,TOKEN>> {
	
	public List<TOKEN> tokenise(String text);
	
	public String deTokenise(List<TOKEN> tokens);
}
