package org.openimaj.text.nlp.patterns;

import java.util.Arrays;
import java.util.Comparator;

import org.openimaj.text.util.RegexUtil;


public class ColloquialismPatternProvider extends PatternProvider {
	String[] EmoticonsDNArr = new String[] {
			"2day","2morrow","2nite","2night"
		};
	
	String EmoticonsDN = RegexUtil.regex_or(longestfirst(EmoticonsDNArr));
//	String EmoticonsDN = regex_or(EmoticonsDNArr);
	
	@Override
	public String patternString(){
		return EmoticonsDN;
	}

	private String[] longestfirst(String[] emoticons) {
		Arrays.sort(emoticons,new Comparator<String>(){

			@Override
			public int compare(String s1, String s2) {
				int s1Longer = s1.length() - s2.length();
				if(s1Longer > 0) return -1;
				else if(s1Longer < 0) return 1;
				else{
					return s1.compareTo(s2);
				}
			}
			
		});
		return emoticons;
	}

	@Override
	public PatternProvider combine(PatternProvider other) {
		return new CombinedPatternProvider(this,other);
	}
}
