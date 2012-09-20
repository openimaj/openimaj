package org.openimaj.text.nlp.textpipe.annotations;

import java.util.ArrayList;
import java.util.Collection;

import org.openimaj.text.nlp.namedentity.NamedEntity;

/**
 * A Nameed Entity Annotation.
 * @author laurence
 *
 */
public class NamedEntityAnnotation extends TextPipeAnnotation {
	/**
	 * Tokens matched by this Named Entity
	 */
	public ArrayList<TokenAnnotation> tokensMatched;
	/**
	 * The Named Entity.
	 */
	public NamedEntity namedEntity;

	@SuppressWarnings("javadoc")
	public NamedEntityAnnotation() {
		super();
		tokensMatched = new ArrayList<TokenAnnotation>();
	}	
	
	@SuppressWarnings("javadoc")
	public NamedEntityAnnotation(ArrayList<TokenAnnotation> tokensMatched,
			NamedEntity namedEntity) {
		super();
		this.tokensMatched = tokensMatched;
		this.namedEntity = namedEntity;
	}

	/**
	 * Get the start char the matching substring for this Named Entity.
	 * @return integer of start char
	 */
	public int getStart(){
		if(tokensMatched.size()>0)return tokensMatched.get(0).start;
		else return -1;
	}
	
	/**
	 * Get the end char the matching substring for this Named Entity.
	 * @return integer of start char
	 */
	public int getEnd(){
		if(tokensMatched.size()>0)return tokensMatched.get(tokensMatched.size()-1).stop;
		else return -1;
	}
	
	/**
	 * Set the {@link TokenAnnotation}s matched by this {@link NamedEntityAnnotation}
	 * @param tokens
	 */
	public void addAllTokensMatched(Collection<TokenAnnotation> tokens){
		tokensMatched.addAll(tokens);
	}

}
