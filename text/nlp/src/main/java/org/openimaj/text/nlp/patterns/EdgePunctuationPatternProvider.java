package org.openimaj.text.nlp.patterns;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;

import edu.stanford.nlp.util.StringUtils;

public abstract class EdgePunctuationPatternProvider extends PatternProvider{
	
	protected String EdgePunct;
	protected String NotEdgePunct;
	protected String StartPunc;
	protected String EndPunc;
	
	
	public EdgePunctuationPatternProvider(PunctuationPatternProvider punctuation) {
		String[] EdgePunctArr = new String[]{
				"'", 
				"\"", 
				"\\*", 
				"\u201c", 
				"\u201d", 
				"\u2018", 
				"\u2019", 
				"\\<", 
				"\\>", 
				"\u00AB", 
				"\u00BB", 
				"{", 
				"}", 
				"\\(", 
				"\\)", 
				"\\[", 
				"\\\\", "\\|"
		};
		HashSet<String> edgeSet = new HashSet<String>();
		for (String string : EdgePunctArr) {
			edgeSet.add(string);
		}
		EdgePunct = "[" + StringUtils.join(EdgePunctArr,"") + "]";
		List<String> puncArr = new ArrayList<String>();
		for(String punc : punctuation.PunctCharsList){
			if(edgeSet.contains(punc)) continue;
			puncArr.add(punc);
		}
		
		
//		NotEdgePunct = "(?:[a-zA-Z0-9]|"+"[" +StringUtils.join(puncArr,",")+"]" +")";
		NotEdgePunct = "(?:[a-zA-Z0-9])";
		StartPunc = "\\s|^|[.,]";
		EndPunc = "\\s|$|[.,]";
	}
	
	public abstract String correctEdges(String s);
	
	
	public static class Left extends EdgePunctuationPatternProvider{
		protected String EdgePunctLeft;
		public Left(PunctuationPatternProvider punctuation) {
			super(punctuation);
			EdgePunctLeft	= String.format("(%s)(%s+)(%s)",StartPunc,EdgePunct, NotEdgePunct);
		}

		@Override
		public String patternString() {
			return EdgePunctLeft;
		}
		
		public String correctEdges(String s) {
//			Matcher matcher = pattern().matcher(s);
//			while(matcher.find()){
//				System.out.println("Found RIGHT match: '" + s.substring(matcher.start(),matcher.end()) + "'");
//				System.out.println("... ngroups: " + matcher.groupCount());
//				for(int i = 0; i < matcher.groupCount(); i++){
//					System.out.println("... ... '" + matcher.group(i) + "'");
//				}
//			}
			return pattern().matcher(s).replaceAll("$1$2 $3");
		}
		
	}
	
	public static class Right extends EdgePunctuationPatternProvider{
		protected String EdgePunctRight;
		public Right(PunctuationPatternProvider punctuation) {
			super(punctuation);
			EdgePunctRight = String.format("(%s)(%s+)(%s)",NotEdgePunct, EdgePunct,EndPunc);
//			System.out.println("Right match pattern: " + EdgePunctRight);
		}

		@Override
		public String patternString() {
			return EdgePunctRight;
		}
		public String correctEdges(String s) {
//			
//			Matcher matcher = pattern().matcher(s);
//			while(matcher.find()){
//				System.out.println("Found RIGHT match: '" + s.substring(matcher.start(),matcher.end()) + "'");
//				System.out.println("... ngroups: " + matcher.groupCount());
//				for(int i = 0; i < matcher.groupCount(); i++){
//					System.out.println("... ... '" + matcher.group(i) + "'");
//				}
//			}
			String ret = pattern().matcher(s).replaceAll("$1 $2$3");
			return ret;
		}
		
	}
	
	static PunctuationPatternProvider punctuation = new PunctuationPatternProvider();
	static EdgePunctuationPatternProvider edgeleft = new EdgePunctuationPatternProvider.Left(punctuation);
	static EdgePunctuationPatternProvider edgeright = new EdgePunctuationPatternProvider.Right(punctuation);
	
	public static String fixedges(String text) {
		String s = text;
		s = edgeleft.correctEdges(s);
		s = edgeright.correctEdges(s);;
		return s;
	}

}
