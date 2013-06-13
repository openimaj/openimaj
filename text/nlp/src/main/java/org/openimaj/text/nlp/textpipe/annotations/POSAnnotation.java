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
 * Part of Speech Annotation based on the Penn treebank.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * 
 */
public class POSAnnotation extends TextPipeAnnotation {

	/**
	 * Penn Treebank part of speech types.
	 * 
	 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
	 * 
	 */
	public enum PartOfSpeech {
		CC("Coordinating conjunction"),
		CD("Cardinal number"),
		DT("Determiner"),
		EX("Existential there"),
		FW("Foreign word"),
		IN("Preposition or subordinating conjunction"),
		JJ("Adjective"),
		JJR("Adjective, comparative"),
		JJS("Adjective, superlative"),
		LS("List item marker"),
		MD("Modal"),
		NN("Noun, singular or mass"),
		NNS("Noun, plural"),
		NNP("Proper noun, singular"),
		NNPS("Proper noun, plural"),
		PDT("Predeterminer"),
		POS("Possessive ending"),
		PRP("Personal pronoun"),
		PRP$("Possessive pronoun (prolog version PRP-S)"),
		RB("Adverb"),
		RBR("Adverb, comparative"),
		RBS("Adverb, superlative"),
		RP("Particle"),
		SYM("Symbol"),
		TO("to"),
		UH("Interjection"),
		VB("Verb, base form"),
		VBD("Verb, past tense"),
		VBG("Verb, gerund or present participle"),
		VBN("Verb, past participle"),
		VBP("Verb, non-3rd person singular present"),
		VBZ("Verb, 3rd person singular present"),
		WDT("Wh-determiner"),
		WP("Wh-pronoun"),
		WP$("Possessive wh-pronoun (prolog version WP-S)"),
		WRB("Wh-adverb"),
		/**
		 * This is added for pos with no mapping to penn bank.
		 */
		UK("Unknown");

		/**
		 * Penn Tree Bank short description
		 */
		public final String DESCRIPTION;

		PartOfSpeech(String description) {
			this.DESCRIPTION = description;
		}

		/**
		 * Gets a {@link PartOfSpeech} from a string.
		 * 
		 * @param pennAbreviation
		 * @return PartOfSpeech
		 */
		public static PartOfSpeech getPOSfromString(String pennAbreviation) {
			for (final PartOfSpeech pos : PartOfSpeech.values()) {
				if (pos.toString().equals(pennAbreviation))
					return pos;
			}
			return PartOfSpeech.UK;
		}
	};

	public PartOfSpeech pos;

	public POSAnnotation(PartOfSpeech pos) {
		super();
		this.pos = pos;
	}

}
