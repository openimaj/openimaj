package org.openimaj.text.nlp;

import gov.sandia.cognition.text.token.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert.*;
import org.openimaj.text.nlp.patterns.EmoticonPatternProvider;
import org.openimaj.text.nlp.patterns.PatternProvider;
import org.openimaj.text.nlp.patterns.PunctuationPatternProvider;
import org.openimaj.text.nlp.patterns.URLPatternProvider;
import org.openimaj.util.pair.IndependentPair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
@SuppressWarnings("unchecked")
public class TweetTokeniserTest {
	
//	private static Gson gson;
//
//	static{
//		gson = new GsonBuilder().
//			serializeNulls().
//			create();
//	}

	private ArrayList<String> allTweets;

	@Before
	public void setup() throws JsonSyntaxException, IOException{
		BufferedReader reader = new BufferedReader(new InputStreamReader(TweetTokeniserTest .class.getResourceAsStream("/org/openimaj/twitter/tweets.txt")));
		String line =null;
		allTweets = new ArrayList<String>();
		while((line = reader.readLine()) != null)
		{
			String json = line;
			allTweets.add(json);
		}
		reader.close();
	}
	
	@Test
	public void emoticons(){
		EmoticonPatternProvider provider = new EmoticonPatternProvider();
		IndependentPair<String, Integer>[] teststr = new IndependentPair[]{
				IndependentPair.pair("^_^ there is one :-) :) and now sad :(",4)
		};
		
		testProvider(provider,teststr);
	}
	
	@Test
	public void urls(){
		URLPatternProvider provider = new URLPatternProvider();
		IndependentPair<String, Integer>[] teststr = new IndependentPair[]{
				IndependentPair.pair("here we have, a url: http://woed.de some text",1),
				IndependentPair.pair("here we have, a url: http://woeD.de",1)
		};
		
		testProvider(provider,teststr);
		
	}
	
	@Test
	public void punctuation(){
		PunctuationPatternProvider provider = new PunctuationPatternProvider();
		IndependentPair<String, Integer>[] teststr = new IndependentPair[]{
				IndependentPair.pair("I was so IMPRESSED! ? !!",3)
		};
		
		testProvider(provider,teststr);
	}
	
	
	private void testProvider(PatternProvider provider,IndependentPair<String, Integer>[] pairs) {
		Pattern p = provider.pattern();
		for (IndependentPair<String, Integer> pair: pairs) {
			String string = pair.firstObject();
			Matcher matches = p.matcher(string);
			ArrayList<String> allemotes = new ArrayList<String>();
			while(matches.find()){
				allemotes .add(string.substring(matches.start(),matches.end()));
			}
			String found = StringUtils.join(allemotes ,", ");
			System.out.format("%s: [%s]\n",string,found);
			Assert.assertTrue(allemotes.size()==pair.secondObject());
		}
	}
	
	@Test
	public void testTweetTokeniser() throws UnsupportedEncodingException, TweetTokeniserException{
		for (String text: allTweets) {
			TweetTokeniser tokeniser = new TweetTokeniser(text);
//			String tokens = "[" + StringUtils.join(tokeniser.getTokens(), ",") + "]";
//			System.out.println("Tokens: " + tokens);
		}
//		new TweetTokeniser("@geektome this is where I point out the 6-mile long #Blackhawks bandwagon and the #fire players on #USMNT #supportchicagoregardlessofsport");
		
	}
}
