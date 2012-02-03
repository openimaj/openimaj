package org.openimaj.text.nlp;

import gov.sandia.cognition.text.token.DefaultToken;
import gov.sandia.cognition.text.token.Token;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.openimaj.text.nlp.patterns.AbbreviationPatternProvider;
import org.openimaj.text.nlp.patterns.EmoticonPatternProvider;
import org.openimaj.text.nlp.patterns.EntityPatternProvider;
import org.openimaj.text.nlp.patterns.NumberPatternProvider;
import org.openimaj.text.nlp.patterns.PunctuationPatternProvider;
import org.openimaj.text.nlp.patterns.TimePatternProvider;
import org.openimaj.text.nlp.patterns.URLPatternProvider;



public class TweetTokeniser implements Iterable<Token>{
	
	private static final String spaceRegex = "\\s+";
	private static final String NotEdgePunct = "[a-zA-Z0-9]";
	private static final String EdgePunct = new String("[' \" Ò Ó Ô Õ < > Ç È { } ( ) \\[ \\]	]").replace(" ","");
	private static final String  EdgePunctLeft	= String.format("(\\s|^)(%s+)(%s)",EdgePunct, NotEdgePunct);
	private static final String  EdgePunctRight = String.format("(%s)(%s+)(\\s|$)",NotEdgePunct, EdgePunct);
	private static final Pattern  EdgePunctLeft_RE = Pattern.compile(EdgePunctLeft);
	private static final Pattern EdgePunctRight_RE= Pattern.compile(EdgePunctRight);
	private String text;
	private ArrayList<Token> tokenize;
	
	
	public static String regex_or(String ... items )
	{
		String r = StringUtils.join(items, "|");
		r = '(' + r + ')';
		return r;
	}
	public String pos_lookahead(String r){
		return "(?=" + r + ')';
	}
		
	public String neg_lookahead(String r) {
		return "(?!" + r + ')';
	}
	public String optional(String r){
		return String.format("(%s)?",r);
	}
	
	static EmoticonPatternProvider emoticons = new EmoticonPatternProvider();
	static PunctuationPatternProvider punctuation = new PunctuationPatternProvider();
	static EntityPatternProvider entity = new EntityPatternProvider();
	static URLPatternProvider url = new URLPatternProvider(punctuation,entity);
	static TimePatternProvider time = new TimePatternProvider();
	static NumberPatternProvider number = new NumberPatternProvider();
	static AbbreviationPatternProvider abbrev = new AbbreviationPatternProvider(entity);
	static String Separators = regex_or("--+", "\u2015");
	static String Decorations = new String(" [\u266b]+ ").replace(" ","");
	static String EmbeddedApostrophe = "\\S+'\\S+";
	
	
	static String [] ProtectThese = new String[]{
//			emoticons.patternString(),
			url.patternString(),
//			entity.patternString(),
//			time.patternString(),
//			number.patternString(),
//			punctuation.patternString(),
//			abbrev.patternString(),
//			Separators,
//			Decorations,
//			EmbeddedApostrophe,
	};
	static Pattern Protect_RE = Pattern.compile(regex_or(ProtectThese));
	
	public TweetTokeniser(String s) throws UnsupportedEncodingException, TweetTokeniserException{
		this.text = new String(s);
//		System.out.println("TWEET:" + text);
//		fixEncoding();
		squeeze_whitespace();
		simple_tokenize();
		align();
	}
	
	private void align() {
	}

	private void simple_tokenize() throws TweetTokeniserException {
		this.tokenize = new ArrayList<Token>();
		edge_punct_munge();
//		System.out.println("Expunged: " + this.text);
		
		ArrayList<String> goods = new ArrayList<String>();
		ArrayList<String> bads = new ArrayList<String>();
		ArrayList<Token> res = new ArrayList<Token>();
		int i = 0;
		Matcher matches = Protect_RE.matcher(this.text);
		if(matches!=null)
		{
			while(matches.find()) {
				String goodString = this.text.substring(i,matches.start());
				
				for (String token : unprotected_tokenize(goodString)) {
					if(token.length() == 0) continue;
					goods.add(token);
					res.add(new DefaultToken(token,0));
				}
				String badString = this.text.substring(matches.start(),matches.end());
				bads.add(badString);
				res.add(new DefaultToken(badString,0));
				i = matches.end();
			}
			String finalGood =  this.text.substring(i, this.text.length());
			for (String token : unprotected_tokenize(finalGood)) {
				goods.add(token);
				res.add(new DefaultToken(token,0));
			}
		}
		else
		{
			String goodString = this.text.substring(0, this.text.length());
			for (String token : unprotected_tokenize(goodString)) {
				res.add(new DefaultToken(token,0));
				goods.add(token);
			}
		}
//		
//		for (int j = 0; j < bads.size(); j++) {
//			res.add(new DefaultToken(goods.get(i),0));
//			res.add(new DefaultToken(bads.get(i),0));
//		}
//		res.add(new DefaultToken(goods.get(goods.size()-1),0));
			
		
		this.tokenize = post_process(res);
	}

	private ArrayList<Token> post_process(ArrayList<Token> res) {
		return res;
	}
	private String[] unprotected_tokenize(String goodString) {
		return goodString.split("\\s+");
	}
	private void edge_punct_munge() {
		String s = this.text;
		s = EdgePunctLeft_RE.matcher(s).replaceAll("\\1\\2 \\3");
		s = EdgePunctRight_RE.matcher(s).replaceAll("\\1 \\2\\3");
		this.text = s;
	}

	private void squeeze_whitespace() {
		this.text.replaceAll(spaceRegex, " ");
	}

	private void fixEncoding() throws UnsupportedEncodingException {
		this.text = new String(text.getBytes("UTF-8"),"UTF-8");
		this.text = StringEscapeUtils.unescapeHtml(this.text);
//		System.out.println("UTF-8:" + text);
	}
	@Override
	public Iterator<Token> iterator() {
		return this.tokenize.iterator();
	}
	
	public List<Token> getTokens(){
		return this.tokenize;
	}

}
