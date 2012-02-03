package org.openimaj.text.nlp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.text.nlp.patterns.EmailPatternProvider;
import org.openimaj.text.nlp.patterns.EmoticonPatternProvider;
import org.openimaj.text.nlp.patterns.PatternProvider;
import org.openimaj.text.nlp.patterns.PunctuationPatternProvider;
import org.openimaj.text.nlp.patterns.URLPatternProvider;
import org.openimaj.util.pair.IndependentPair;

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
				IndependentPair.pair("^_^ there is one :-) :) and now sad :(",4),
				IndependentPair.pair("Intelligence is only one variable in the equation... (c) Susan DePhillips",1)
		};
		
		testProvider(provider,teststr);
	}
	
	@Test
	public void urls(){
		URLPatternProvider provider = new URLPatternProvider();
		IndependentPair<String, Integer>[] teststr = new IndependentPair[]{
				IndependentPair.pair("here we have, a url: http://woed.de some text",1),
				IndependentPair.pair("here we have, a url: http://woeD.de",1),
				IndependentPair.pair("http://foo.com/more_(than)_one_(parens)",1),
				IndependentPair.pair("http://foo.com/blah_(wikipedia)#cite-1",1),
				IndependentPair.pair("http://foo.com/blah_(wikipedia)_blah#cite-1",1),
				IndependentPair.pair("http://foo.com/unicode_(\u272A)_in_parens",1),
				IndependentPair.pair("http://foo.com/(something)?after=parens",1),
		};
		
		testProvider(provider,teststr);
		
	}
	
	@Test
	public void dfurls(){
		URLPatternProvider.DFURLPatternProvider provider = new URLPatternProvider.DFURLPatternProvider();
		IndependentPair<String, Integer>[] teststr = new IndependentPair[]{
				IndependentPair.pair("here we have, a url: http://woed.de some text",1),
				IndependentPair.pair("here we have, a url: http://woeD.de",1),
				IndependentPair.pair("here we have, a url: www.woed.de asdasd",1),
				IndependentPair.pair("here we have, a url: // www.TrueCaller.com asdasd",1),
				IndependentPair.pair("http://foo.com/more_(than)_one_(parens)",1),
				IndependentPair.pair("http://foo.com/blah_(wikipedia)#cite-1",1),
				IndependentPair.pair("http://foo.com/blah_(wikipedia)_blah#cite-1",1),
				IndependentPair.pair("http://foo.com/unicode_(\u272A)_in_parens",1),
				IndependentPair.pair("http://foo.com/(something)?after=parens",1),
				IndependentPair.pair("@CarideeChris ....Awwwww...lets hope rest of the day you are more lucky ;-)",0),
				IndependentPair.pair("12123321 The Everglades comehttp://short.ie/m0h9q4", 1)
		};
		
		testProvider(provider,teststr);
		
	}
	
	
	@Test
	public void punctuation(){
		PunctuationPatternProvider provider = new PunctuationPatternProvider();
		IndependentPair<String, Integer>[] teststr = new IndependentPair[]{
				IndependentPair.pair("I was so IMPRESSED! ? !!",3),
				IndependentPair.pair("@CarideeChris ....Awwwww...lets hope rest of the day you are more lucky ;-)",3)
		};
		
		testProvider(provider,teststr);
	}
	
	@Test
	public void email(){
		EmailPatternProvider provider = new EmailPatternProvider();
		IndependentPair<String, Integer>[] teststr = new IndependentPair[]{
				IndependentPair.pair("RT @erkthajerk: @Erkthajerk beat sale going on now til march 31st. Contact redplanetmusicgroup@gmail.com for details",1),
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
//			System.out.format("%s: [%s]\n",string,found);
			Assert.assertTrue(allemotes.size()==pair.secondObject());
		}
	}
	
	@Test
	public void testSingleTweets() throws UnsupportedEncodingException, TweetTokeniserException{
		String[] tweets = new String[]{
//			"Listening to \"Rockin u Radio\" http://www.live365.com/stations/djmarkstevens on Live365.",
//			"Maigc.'everything' was'nt magic",
//			"Maigc.'everything' was'nt magic",
//			"Maigc.everything' 'a's magic",
//			"@CarideeChris ....Awwwww...lets hope rest of the day you are more lucky ;-)",
//			"Intelligence is only one variable in the equation... (c) Susan DePhillips",
//			"@Snuva You might be the Queen of Grump but the Wicked Witch of the South(tm) insists the weekend doesn't start til 2:00 pm tomorrow :-("
//			"RT @iAmTheGreek: everybody in Lehigh Valley (& beyond) should support @homebase610 with voting during March for the Pepsi Refresh Project"
			"aaaaaaah naaaaaao;;; hino do corinthiaans naaao DDDDD: IOEUIAOUEAIOUOAEIUEIO' (@stevens_adam live on http://twitcam.com/2bqv)"
		};
		for (String text: tweets) {
			TweetTokeniser tokeniser = new TweetTokeniser(text);
//			System.out.println("Tweet: " + text);
//			String tokens = "[" + StringUtils.join(tokeniser.getTokens(), ",") + "]";
//			System.out.println("Tokens: " + tokens);
		}
	}
	
	@Test
	public void testTweetTokeniser() throws UnsupportedEncodingException, TweetTokeniserException{
		for (String text: allTweets) {
			TweetTokeniser tokeniser = new TweetTokeniser(text);
//			System.out.println("Tweet: " + text);
//			String tokens = "[" + StringUtils.join(tokeniser.getTokens(), ",") + "]";
//			System.out.println("Tokens: " + tokens);
		}
//		new TweetTokeniser("@geektome this is where I point out the 6-mile long #Blackhawks bandwagon and the #fire players on #USMNT #supportchicagoregardlessofsport");
		
	}
}
