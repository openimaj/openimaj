/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.text.nlp.textpipe.annotations;


/**
 * An annotation representing a phrase as per the Penn Treebank.
 * @author laurence
 *
 */
public class PhraseAnnotation extends TextPipeAnnotation{
	
	/**
	 * Penn Treebank phrase abbreviations.
	 * @author laurence
	 *
	 */
	public enum Phrase{			
		@SuppressWarnings("javadoc")
		ADJP("Adjective Phrase."),
		@SuppressWarnings("javadoc")
		ADVP ("Adverb Phrase."),
		@SuppressWarnings("javadoc")
		CONJP ("Conjunction Phrase."),
		@SuppressWarnings("javadoc")
		FRAG ("Fragment."),
		@SuppressWarnings("javadoc")
		INTJ ("Interjection. Corresponds approximately to the part-of-speech tag UH."),
		@SuppressWarnings("javadoc")
		LST ("List marker. Includes surrounding punctuation."),
		@SuppressWarnings("javadoc")
		NAC ("Not a Constituent; used to show the scope of certain prenominal modifiers within an NP."),
		@SuppressWarnings("javadoc")
		NP ("Noun Phrase. "),
		@SuppressWarnings("javadoc")
		NX ("Used within certain complex NPs to mark the head of the NP. Corresponds very roughly to N-bar level but used quite differently."),
		@SuppressWarnings("javadoc")
		PP ("Prepositional Phrase."),
		@SuppressWarnings("javadoc")
		PRN ("Parenthetical. "),
		@SuppressWarnings("javadoc")
		PRT ("Particle. Category for words that should be tagged RP. "),
		@SuppressWarnings("javadoc")
		QP ("Quantifier Phrase (i.e. complex measure/amount phrase); used within NP."),
		@SuppressWarnings("javadoc")
		RRC ("Reduced Relative Clause. "),
		@SuppressWarnings("javadoc")
		UCP ("Unlike Coordinated Phrase. "),
		@SuppressWarnings("javadoc")
		VP ("Vereb Phrase. "),
		@SuppressWarnings("javadoc")
		WHADJP ("Wh-adjective Phrase. Adjectival phrase containing a wh-adverb, as in how hot."),
		@SuppressWarnings("javadoc")
		WHAVP ("Wh-adverb Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical, containing a wh-adverb such as how or why."),
		@SuppressWarnings("javadoc")
		WHNP ("Wh-noun Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical, containing some wh-word, e.g. who, which book, whose daughter, none of which, or how many leopards."),
		@SuppressWarnings("javadoc")
		WHPP ("Wh-prepositional Phrase. Prepositional phrase containing a wh-noun phrase (such as of which or by whose authority) that either introduces a PP gap or is contained by a WHNP."),
		@SuppressWarnings("javadoc")
		X ("Unknown, uncertain, or unbracketable. X is often used for bracketing typos and in bracketing the...the-constructions."),
		/**
		 * This is added for phrases with no mapping to penn bank.
		 */
		UK("Unknown");
		
		
		/**
		 * Penn Tree Bank short description
		 */
		public final String DESCRIPTION;
		
		
		Phrase(String description){
			this.DESCRIPTION=description;
		}
		
		
		/**
		 * Returns a {@link Phrase} based on the string.
		 * @param pennAbreviation
		 * @return {@link Phrase}
		 */
		public static Phrase getPhrasefromString(String pennAbreviation){
			for(Phrase pos:Phrase.values()){
				if(pos.toString().equals(pennAbreviation))return pos;
			}
			return Phrase.UK;
		}
	};
	
	/**
	 * The {@link Phrase} label.
	 */
	public Phrase phrase;
	/**
	 * true if this is the start token of a phrase segment. false if it is a continuation.
	 */
	public boolean start;
	
	@SuppressWarnings("javadoc")
	public PhraseAnnotation(Phrase phrase, boolean start){
		super();
		this.phrase=phrase;
		this.start=start;
	}
	
	/**
	 * Returns a string representation of the Phrase order of this phrase.
	 * @return String order.
	 */
	public String getOrder(){
		if(start)return "start";
		else return "continue";
	}
	
	

}
