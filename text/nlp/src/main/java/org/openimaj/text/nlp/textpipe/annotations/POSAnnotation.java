package org.openimaj.text.nlp.textpipe.annotations;

/**
 * Part of Speech Annotation based on the Penn treebank.
 * @author laurence
 *
 */
public class POSAnnotation extends TextPipeAnnotation{
	
	/**
	 * Penn Treebank part of speech types.
	 * @author laurence
	 *
	 */
	public enum PartOfSpeech{			
		@SuppressWarnings("javadoc")
		CC ("Coordinating conjunction"),
		@SuppressWarnings("javadoc")
		CD ("Cardinal number"),
		@SuppressWarnings("javadoc")
		DT ("Determiner"),
		@SuppressWarnings("javadoc")
		EX ("Existential there"),
		@SuppressWarnings("javadoc")
		FW ("Foreign word"),
		@SuppressWarnings("javadoc")
		IN ("Preposition or subordinating conjunction"),
		@SuppressWarnings("javadoc")
		JJ ("Adjective"),
		@SuppressWarnings("javadoc")
		JJR ("Adjective, comparative"),
		@SuppressWarnings("javadoc")
		JJS ("Adjective, superlative"),
		@SuppressWarnings("javadoc")
		LS ("List item marker"),
		@SuppressWarnings("javadoc")
		MD ("Modal"),
		@SuppressWarnings("javadoc")
		NN ("Noun, singular or mass"),
		@SuppressWarnings("javadoc")
		NNS ("Noun, plural"),
		@SuppressWarnings("javadoc")
		NNP ("Proper noun, singular"),
		@SuppressWarnings("javadoc")
		NNPS ("Proper noun, plural"),
		@SuppressWarnings("javadoc")
		PDT ("Predeterminer"),
		@SuppressWarnings("javadoc")
		POS ("Possessive ending"),
		@SuppressWarnings("javadoc")
		PRP ("Personal pronoun"),
		@SuppressWarnings("javadoc")
		PRP$ ("Possessive pronoun (prolog version PRP-S)"),
		@SuppressWarnings("javadoc")
		RB ("Adverb"),
		@SuppressWarnings("javadoc")
		RBR ("Adverb, comparative"),
		@SuppressWarnings("javadoc")
		RBS ("Adverb, superlative"),
		@SuppressWarnings("javadoc")
		RP ("Particle"),
		@SuppressWarnings("javadoc")
		SYM ("Symbol"),
		@SuppressWarnings("javadoc")
		TO ("to"),
		@SuppressWarnings("javadoc")
		UH ("Interjection"),
		@SuppressWarnings("javadoc")
		VB ("Verb, base form"),
		@SuppressWarnings("javadoc")
		VBD ("Verb, past tense"),
		@SuppressWarnings("javadoc")
		VBG ("Verb, gerund or present participle"),
		@SuppressWarnings("javadoc")
		VBN ("Verb, past participle"),
		@SuppressWarnings("javadoc")
		VBP ("Verb, non-3rd person singular present"),
		@SuppressWarnings("javadoc")
		VBZ ("Verb, 3rd person singular present"),
		@SuppressWarnings("javadoc")
		WDT ("Wh-determiner"),
		@SuppressWarnings("javadoc")
		WP ("Wh-pronoun"),
		@SuppressWarnings("javadoc")
		WP$ ("Possessive wh-pronoun (prolog version WP-S)"),
		@SuppressWarnings("javadoc")
		WRB ("Wh-adverb"),
		/**
		 * This is added for pos with no mapping to penn bank.
		 */
		UK("Unknown");
		
		
		/**
		 * Penn Tree Bank short description
		 */
		public final String DESCRIPTION;
		
		PartOfSpeech(String description){
			this.DESCRIPTION=description;
		}
		
		/**
		 * Gets a {@link PartOfSpeech} from a string.
		 * @param pennAbreviation
		 * @return PartOfSpeech
		 */
		public static PartOfSpeech getPOSfromString(String pennAbreviation){
			for(PartOfSpeech pos:PartOfSpeech.values()){
				if(pos.toString().equals(pennAbreviation))return pos;
			}
			return PartOfSpeech.UK;
		}
	};
	
	@SuppressWarnings("javadoc")
	public PartOfSpeech pos;
	
	
	@SuppressWarnings("javadoc")
	public POSAnnotation(PartOfSpeech pos){
		super();
		this.pos = pos;
	}

}
