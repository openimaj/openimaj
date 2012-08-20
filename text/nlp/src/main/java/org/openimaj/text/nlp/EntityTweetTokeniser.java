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
package org.openimaj.text.nlp;

import gov.sandia.cognition.text.token.DefaultToken;
import gov.sandia.cognition.text.token.Token;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.openimaj.text.nlp.patterns.AbbreviationPatternProvider;
import org.openimaj.text.nlp.patterns.ComplicatedNumberPatternProvider;
import org.openimaj.text.nlp.patterns.EmailPatternProvider;
import org.openimaj.text.nlp.patterns.EmbeddedApostrophePatternProvider;
import org.openimaj.text.nlp.patterns.EmbeddedDashPatternProvider;
import org.openimaj.text.nlp.patterns.EmoticonPatternProvider;
import org.openimaj.text.nlp.patterns.EntityPatternProvider;
import org.openimaj.text.nlp.patterns.PunctuationPatternProvider;
import org.openimaj.text.nlp.patterns.TimePatternProvider;
import org.openimaj.text.nlp.patterns.TwitterStuffPatternProvider;
import org.openimaj.text.nlp.patterns.URLPatternProvider;
import org.openimaj.text.util.RegexUtil;



/**
 * A tokeniser built to work with short text, like that found in twitter.
 * Protects various elements of the text with an assumption that if the user made the mark, it was an important mark that carries meaning
 * because of the relatively high premium of each key stroke.
 * 
 * Based on the twokenise by Brendan O'Connor 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class EntityTweetTokeniser implements Iterable<Token>{
	
	
	private String text;
	private ArrayList<Token> tokenize;
	private ArrayList<Token> protectedTokens;
	private ArrayList<Token> unprotectedTokens;
	
	private final static Locale[] invalidLanguages = new Locale[]{
		new Locale("zh"),
		new Locale("ko"),
		new Locale("jp"),
	};
	
	
	/**
	 * Check whether this locale is supported by this tokeniser. The unsupported languages are those which don't need space
	 * characters to delimit words, namely the CJK languages.
	 * @param locale
	 * @return true if the local is supported
	 */
	public static boolean isValid(Locale locale){
		return isValid(locale.getLanguage());
	}
	/**
	 * Check whether this locale (specified by the two letter country code, {@link Locale}) is
	 * supported by this tokeniser. The unsupported languages are those which don't need space
	 * characters to delimit words, namely the CJK languages.
	 * @param locale
	 * @return true if the local is supported
	 */
	public static boolean isValid(String locale){
		for (Locale invalidLocal: invalidLanguages) {
			if(invalidLocal.getLanguage().equals(locale)) return false;
		}
		return true;
	}
	
	
//	public static String regex_or(String ... items )
//	{
//		String r = StringUtils.join(items, "|");
//		r = '(' + r + ')';
//		return r;
//	}
//	public String pos_lookahead(String r){
//		return "(?=" + r + ')';
//	}
//		
//	public static String neg_lookahead(String r) {
//		return "(?!" + r + ')';
//	}
//	public String optional(String r){
//		return String.format("(%s)?",r);
//	}
	
	static EmoticonPatternProvider emoticons = new EmoticonPatternProvider();
	static PunctuationPatternProvider punctuation = new PunctuationPatternProvider();
	static EntityPatternProvider entity = new EntityPatternProvider();
	static URLPatternProvider url = new URLPatternProvider();
	static TimePatternProvider time = new TimePatternProvider();
	static ComplicatedNumberPatternProvider number = new ComplicatedNumberPatternProvider();
	static TwitterStuffPatternProvider twitterPart = new TwitterStuffPatternProvider();
	static EmailPatternProvider email = new EmailPatternProvider();
	static AbbreviationPatternProvider abbrev = new AbbreviationPatternProvider(entity);
	private static final String spaceRegex = "\\s+";
	static String Separators = RegexUtil.regex_or_match("--+", "\u2015");
	static String Decorations = new String(" [\u266b]+ ").replace(" ","");
	static EmbeddedApostrophePatternProvider embedded = new EmbeddedApostrophePatternProvider(punctuation);
	static EmbeddedDashPatternProvider embeddedDash = new EmbeddedDashPatternProvider(punctuation);
	
	
	static String [] ProtectThese = new String[]{
			twitterPart.patternString(),
			emoticons.patternString(),
			url.patternString(),
			email.patternString(),
			entity.patternString(),
			time.patternString(),
			number.patternString(),
//			embeddedDash.patternString(),
//			embedded.patternString(),
			punctuation.patternString(),
			abbrev.patternString(),
			Separators,
			Decorations,
	};
	static String oredProtect = RegexUtil.regex_or_match(ProtectThese);
	static Pattern Protect_RE = Pattern.compile(oredProtect,Pattern.UNICODE_CASE|Pattern.CASE_INSENSITIVE);
//	static Pattern Protect_RE = twitterPart.pattern();
	
	
	/**
	 * @param s Tokenise this string
	 * @throws UnsupportedEncodingException
	 * @throws TweetTokeniserException
	 */
	public EntityTweetTokeniser(String s) throws UnsupportedEncodingException, TweetTokeniserException{
//		System.out.println(EdgePunct);
//		System.out.println(new String(""));
		this.text = new String(s);
//		System.out.println("TWEET:" + text);
		fixEncoding();
		squeeze_whitespace();
		simple_tokenize();
	}
	
	private void simple_tokenize() throws TweetTokeniserException {
		this.tokenize = new ArrayList<Token>();
		edge_punct_munge();
		
		ArrayList<String> goods = new ArrayList<String>();
		ArrayList<String> bads = new ArrayList<String>();
		ArrayList<Token> res = new ArrayList<Token>();
		ArrayList<Token> goodt = new ArrayList<Token>();
		ArrayList<Token> badt = new ArrayList<Token>();
		int i = 0;
		Matcher matches = Protect_RE.matcher(this.text);
		if(matches!=null)
		{
			while(matches.find()) {
				String goodString = this.text.substring(i,matches.start());
				goods.add(goodString);
				List<Token> goodStrings = unprotected_tokenize(goodString);
				res.addAll(goodStrings);
				goodt.addAll(goodStrings);
				String badString = this.text.substring(matches.start(),matches.end());
				bads.add(badString);
				DefaultToken badTok = new DefaultToken(badString,0);
				res.add(badTok);
				badt.add(badTok);
				i = matches.end();
			}
			String finalGood =  this.text.substring(i, this.text.length());
			List<Token> goodStrings = unprotected_tokenize(finalGood);
			res.addAll(goodStrings);
			goodt.addAll(goodStrings);
		}
		else
		{
			String goodString = this.text.substring(0, this.text.length());
			List<Token> goodStrings = unprotected_tokenize(goodString);
			res.addAll(goodStrings);
			goodt.addAll(goodStrings);
		}	
		
		
		this.tokenize = post_process(res);
		this.protectedTokens = post_process(badt);
		this.unprotectedTokens = post_process(goodt);
	}

	private ArrayList<Token> post_process(ArrayList<Token> res) {
		return res;
	}
	private List<Token> unprotected_tokenize(String goodString) {
		String[] strings = goodString.split("\\s+");
		List<Token> t = new ArrayList<Token>();
		for (String s : strings) {
			if(s.isEmpty()) continue;
			t.add(new DefaultToken(s, 0));
		}
		return t;
	}
	private void edge_punct_munge() {
//		this.text = EdgePunctuationPatternProvider.fixedges(this.text);
	}

	private void squeeze_whitespace() {
		this.text = this.text.replaceAll(spaceRegex, " ");
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
	
	public List<String> getStringTokens(){
		List<String> stringTokens = new ArrayList<String>();
		for (Token token : this.tokenize) {
			stringTokens.add(token.getText());
		}
		return stringTokens;
	}

	public List<String> getProtectedStringTokens() {
		List<String> stringTokens = new ArrayList<String>();
		for (Token token : this.protectedTokens) {
			stringTokens.add(token.getText());
		}
		return stringTokens;
	}
	
	public List<String> getUnprotectedStringTokens() {
		List<String> stringTokens = new ArrayList<String>();
		for (Token token : this.unprotectedTokens) {
			stringTokens.add(token.getText());
		}
		return stringTokens;
	}

}
