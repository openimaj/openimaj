package org.openimaj.text.nlp.patterns;

import java.util.List;

import org.openimaj.text.util.RegexUtil;

public class EmbeddedApostrophePatternProvider extends PatternProvider{
	public String EmbeddedApostrophe;
	public EmbeddedApostrophePatternProvider(PunctuationPatternProvider punctuation) {
		List<String> puncs = punctuation.notMinus("'");
		puncs.add("^ ");
		String notpuncs = RegexUtil.regex_char_neg(puncs);
		this.EmbeddedApostrophe = String.format(notpuncs+"+'"+notpuncs+"+");
	}

	@Override
	public String patternString() {
		return EmbeddedApostrophe;
	}

}
