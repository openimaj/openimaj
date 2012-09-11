package org.openimaj.text.nlp.textpipe.annotations;

import org.openimaj.text.nlp.textpipe.annotations.POSAnnotation.PartOfSpeech;

public class PhraseAnnotation extends TextPipeAnnotation{
	
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
		
		
		public static Phrase getPhrasefromString(String pennAbreviation){
			for(Phrase pos:Phrase.values()){
				if(pos.toString().equals(pennAbreviation))return pos;
			}
			return Phrase.UK;
		}
	};
	
	public Phrase phrase;
	public boolean start;
	public PhraseAnnotation continuation;
	
	public PhraseAnnotation(Phrase phrase, boolean start){
		super();
		this.phrase=phrase;
		this.start=start;
		continuation=null;
	}
	
	public String getOrder(){
		if(start)return "start";
		else return "continue";
	}
	
	

}
