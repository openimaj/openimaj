package org.openimaj.text.nlp.patterns;

import java.util.List;

public class EmbeddedApostrophePatternProvider extends PatternProvider{
	public String EmbeddedApostrophe;
	public EmbeddedApostrophePatternProvider(PunctuationPatternProvider punctuation) {
		List<String> puncs = punctuation.notMinus("'");
		puncs.add("^ ");
		String notpuncs = regex_char_neg(puncs);
		this.EmbeddedApostrophe = String.format(notpuncs+"+'"+notpuncs+"+");
	}

	@Override
	public String patternString() {
		return EmbeddedApostrophe;
	}

}
