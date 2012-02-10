package org.openimaj.text.nlp.patterns;

import org.openimaj.text.util.RegexUtil;

public class AbbreviationPatternProvider extends PatternProvider {
	
	private String ArbitraryAbbrev;

	public AbbreviationPatternProvider(EntityPatternProvider entity) {
//		String[] Abbrevs1 = new String[]{"am","pm","us","usa","ie","eg"};
//		
//		this.Abbrevs = regexify_abbrev(Abbrevs1);
	//
		String BoundaryNotDot = RegexUtil.regex_or("\\s", "[\\u201c\\u201d\"?!,:;]", entity.patternString());
		String aa1 = "([A-Za-z]\\.){2,}" + RegexUtil.pos_lookahead(BoundaryNotDot);
		String aa2 = "([A-Za-z]\\.){1,}[A-Za-z]" + RegexUtil.pos_lookahead(BoundaryNotDot);
		this.ArbitraryAbbrev = RegexUtil.regex_or(aa1,aa2);
	}
	
//	private String[] regexify_abbrev(String[] a){
//		String[] out = new String[a.length];
//		for (int i = 0 ; i < a.length; i++) {
//			String s = a[i];
//			String dotted = "";
//			for (int j = 0; j < s.length(); j++) {
//				dotted += s.substring(j,j+1).toUpperCase() + "\\.";
//			}
//			out[i] = dotted;
//		}
//		return out;
//	}

	@Override
	public String patternString() {
		return this.ArbitraryAbbrev;
	}
	
}
