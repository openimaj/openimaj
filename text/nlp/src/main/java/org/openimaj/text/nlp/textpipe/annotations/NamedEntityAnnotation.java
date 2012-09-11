package org.openimaj.text.nlp.textpipe.annotations;

import java.util.ArrayList;
import java.util.Collection;

import org.openimaj.text.nlp.namedentity.NamedEntity;

public class NamedEntityAnnotation extends TextPipeAnnotation {
	public ArrayList<TokenAnnotation> tokensMatched;
	public NamedEntity namedEntity;

	public NamedEntityAnnotation() {
		super();
		tokensMatched = new ArrayList<TokenAnnotation>();
	}	
	
	public NamedEntityAnnotation(ArrayList<TokenAnnotation> tokensMatched,
			NamedEntity namedEntity) {
		super();
		this.tokensMatched = tokensMatched;
		this.namedEntity = namedEntity;
	}

	public int getStart(){
		if(tokensMatched.size()>0)return tokensMatched.get(0).start;
		else return -1;
	}
	
	public int getEnd(){
		if(tokensMatched.size()>0)return tokensMatched.get(tokensMatched.size()-1).stop;
		else return -1;
	}
	
	public void addAllTokensMatched(Collection<TokenAnnotation> tokens){
		tokensMatched.addAll(tokens);
	}

}
