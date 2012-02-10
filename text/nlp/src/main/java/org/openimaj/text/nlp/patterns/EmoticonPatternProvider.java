package org.openimaj.text.nlp.patterns;

import java.util.Arrays;
import java.util.Comparator;

import org.openimaj.text.util.RegexUtil;


public class EmoticonPatternProvider extends PatternProvider {
	String[] EmoticonsDNArr = new String[] {
			":\\)",":\\(",":-\\)",">:\\]",":o\\)",":3",":c\\)",":>","=\\]","8\\)","=\\)",
			":\\}",":\\^\\)",">:D\\)",":-D",":D","8-D","8D","x-D","xD","X-D","XD","=-D","=D",
			"=-3","=3\\)","8-\\)",":-\\)\\)",":\\)\\)",">-\\[",":-\\(",":\\(",":-c",":c",":-<",":<",
			":-\\[",":\\[","\\[:",":\\{",">[._]>","<[._]<",">v<","\\^[._]\\^",":-\\|\\|","D:<","D+:","D8","D;","D=","DX",
			"v[.]v","D-\\':",">;\\]",";-\\)",";\\)","\\*-\\)","\\*\\)",";-\\]",";\\]",";D",";^\\)",">:[pP]",
			":-[pP]",":[pP]","X-[pP]","x-[pP]","x[pP]","X[pP]",":-[pP]",":[pP]",";[pP]","=[pP]",":-b",":b",">:o",">:O",":-O",
			":O",":0","o_O","o_0","o[.]O","8-0",">:\\",">:/",":-/",":-[.]",":/",":\\\\",
			"=\\/","=\\",":S",":\\|",":-\\|",">:X",":-X",":X",":-#",":#",":$","O:-\\)","0:-3",
			"0:3","O:-\\)","O:\\)","0;^\\)",">:\\)",">;\\)",">:-\\)",":\\'-\\(",":\\'\\(",":\\'-\\)",":\\'\\)",
			";\\)\\)",";;\\)","<3","8-\\}",">:D<","=\\)\\)","=\\(\\(","x\\(","X\\(",":-\\*",":\\*",":\\\">","~X\\(",":-?;",
			"-_-","\\*-\\*","[.]_[.]","[*]--[*]",
			 "\\([ ]*c[ ]*\\)","\\([ ]*tm[ ]*\\)", //THIS IS ABSOLUTELY DISGUSTING, IT SHOULD NOT BE HERE
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
