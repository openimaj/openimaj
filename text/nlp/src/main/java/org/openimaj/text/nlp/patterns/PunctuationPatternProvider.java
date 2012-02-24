package org.openimaj.text.nlp.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openimaj.text.util.RegexUtil;

public class PunctuationPatternProvider extends PatternProvider{
	
	String[] PunctCharsList = new String[]{
		"'","\\|","\\/",
		"\u2026", // Ellipses
		"\u201c", // open quote
		"\u201d", // close quote
		"\"",".","?","!",",",":",";","&","*"
	};
	private String Punct;
	private String charPuncs;
	
	public PunctuationPatternProvider() {
		String [] allpuncs = new String[PunctCharsList.length];
		this.charPuncs = "[";
		int i = 0;
		for (String punc : PunctCharsList) {
			allpuncs[i++] = String.format("[%s]+",punc);
			charPuncs += punc;
		}
		charPuncs+="]";
		this.Punct = String.format("%s", RegexUtil.regex_or(allpuncs));
	}
	
	@Override
	public String patternString() {
		return charPuncs + "+";
	}
	
	public String charPattern(){
		return this.charPuncs;
	}
	
	public List<String> notMinus(String toIgnore){
		List<String> allnotpuncs = new ArrayList<String>();
		for (String punc : PunctCharsList) {
			if(toIgnore.equals(punc)) continue;
			allnotpuncs.add(String.format("^%s",punc));
		}
		return allnotpuncs;
	}

	
}
