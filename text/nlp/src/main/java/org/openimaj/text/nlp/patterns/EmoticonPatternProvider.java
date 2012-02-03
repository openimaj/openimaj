package org.openimaj.text.nlp.patterns;


public class EmoticonPatternProvider extends PatternProvider {
	String[] EmoticonsDNArr = new String[] {
			":\\)",":\\(",":-\\)",">:\\]",":o\\)",":3",":c\\)",":>","=\\]","8\\)","=\\)",
			":\\}",":^\\)",">:D\\)",":-D",":D","8-D","8D","x-D","xD","X-D","XD","=-D","=D",
			"=-3","=3\\)","8-\\)",":-\\)\\)",":\\)\\)",">-\\[",":-\\(",":\\(",":-c",":c",":-<",":<",
			":-\\[",":\\[",":\\{",">.>","<.<",">.<","\\^.\\^",":-\\|\\|","D:<","D:","D8","D;","D=","DX",
			"v.v","D-\\':",">;\\]",";-\\)",";\\)","\\*-\\)","\\*\\)",";-\\]",";\\]",";D",";^\\)",">:P",
			":-P",":P","X-P","x-p","xp","XP",":-p",":p","=p",":-b",":b",">:o",">:O",":-O",
			":O",":0","o_O","o_0","o.O","8-0",">:\\",">:/",":-/",":-\\.",":/",":\\\\",
			"=\\/","=\\",":S",":\\|",":-\\|",">:X",":-X",":X",":-#",":#",":$","O:-\\)","0:-3",
			"0:3","O:-\\)","O:\\)","0;^\\)",">:\\)",">;\\)",">:-\\)",":\\'-\\(",":\\'\\(",":\\'-\\)",":\\'\\)",
			";\\)\\)",";;\\)","<3","8-\\}",">:D<","=\\)\\)","=\\(\\(","x\\(","X\\(",":-\\*",":\\*",":\\\">","~X\\(",":-?;"
		};
	String EmoticonsDN = regex_or(EmoticonsDNArr);
	
	public String patternString(){
		return EmoticonsDN;
	}

	@Override
	public PatternProvider combine(PatternProvider other) {
		return new CombinedPatternProvider(this,other);
	}
}
