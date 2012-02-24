package org.openimaj.text.nlp;

import static org.junit.Assert.*;
import gov.sandia.cognition.text.token.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.text.nlp.patterns.EdgePunctuationPatternProvider;
import org.openimaj.text.nlp.patterns.EmailPatternProvider;
import org.openimaj.text.nlp.patterns.EmoticonPatternProvider;
import org.openimaj.text.nlp.patterns.ComplicatedNumberPatternProvider;
import org.openimaj.text.nlp.patterns.PatternProvider;
import org.openimaj.text.nlp.patterns.PunctuationPatternProvider;
import org.openimaj.text.nlp.patterns.TwitterStuffPatternProvider;
import org.openimaj.text.nlp.patterns.URLPatternProvider;
import org.openimaj.util.pair.IndependentPair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
@SuppressWarnings("unchecked")
public class TweetTokeniserTest {
	
	private static Gson gson;

	static{
		gson = new GsonBuilder().
			serializeNulls().
			create();
	}

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
				IndependentPair.pair("Intelligence is only one variable in the equation... (c) Susan DePhillips",1),
				IndependentPair.pair("@avlsuresh I didnt know about it :-)). I would be even more happy when you will give the old one to me.",1),
				IndependentPair.pair("RT @BThompsonWRITEZ: @libbyabrego honored?! Everybody knows the libster is nice with it...lol...(thankkkks a bunch;))",1),
				IndependentPair.pair("@glamthug well what the fuck man:(",1),
				 
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
				IndependentPair.pair("here we have, a url: www.woed.de asdasd",1),
				IndependentPair.pair("here we have, a url: // www.TrueCaller.com asdasd",1),
				IndependentPair.pair("http://assfsdhgftgfvkcsjtbvtbgmktyhklgbmkgskdmvdthydtyhgfyhdfht (@andreesrr live on http://twitcam.com/2bl4v", 1)
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
				IndependentPair.pair("@CarideeChris ....Awwwww...lets hope rest of the day you are more lucky",2),
				IndependentPair.pair("Maureen Green, the former TV anchor, blogs to keep up with social and workforce trends|Gloria Wright / The ..",5),
				IndependentPair.pair("people... so bored!!!!!!!!why u dont want 2 go 2 the campa?!!!why?!!",4)
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
	
	@Test
	public void twitterStuff(){
		TwitterStuffPatternProvider provider = new TwitterStuffPatternProvider ();
		IndependentPair<String, Integer>[] teststr = new IndependentPair[]{
				IndependentPair.pair("RT @erkthajerk: @Erkthajerk beat sale going on now til march 31st. Contact for details",2),
				IndependentPair.pair("you should all follow @sinjax #ff #awesomeGuy",3),
				IndependentPair.pair("@_CarolineF_ *Nods, smiling* Just alright? *touches your arm, seeing flashes of your recent past and drawing my hand away quickly in shock*",1),
				IndependentPair.pair("#some_dirty-hashtag right here",1),
				IndependentPair.pair("you should all follow @sinjax #ff #awesomeGuy",3),
				IndependentPair.pair("RT @GardenForkTV: The Labs in the park - http://bit.ly/doHueQ New on Gardenfork //they look adorable in the snow http://ff.im/-gHOF7", 1),
		};
		
		testProvider(provider,teststr);
	}
	
	@Test
	public void numbers(){
		ComplicatedNumberPatternProvider provider = new ComplicatedNumberPatternProvider();
		IndependentPair<String, Integer>[] teststr = new IndependentPair[]{
				IndependentPair.pair("Checking out \"HONUS WAGNER HARRISON STUDIOS 1919 $10,000\" on VINTAGE SPORTS CARDS",1),
				IndependentPair.pair("Dorin Dickerson was not on Kipers top 15 TE as Sr or Jr and made Millions today with 4.4 40 time and 43.d vert and now top 1st rd lock", 1),
				IndependentPair.pair("RT @Adam_Schefter: Florida QB Tim Tebow broke the combine record for QBs with a 38-inch vertical jump. He also ran an impressive 40 time ...",0),
		};
		
		testProvider(provider,teststr);
		

	}
	
	
	private void testProvider(PatternProvider provider,IndependentPair<String, Integer>[] pairs) {
		Pattern p = provider.pattern();
		for (IndependentPair<String, Integer> pair: pairs) {
			String string = pair.firstObject();
//			System.out.println("Original: " + string);
			string = EdgePunctuationPatternProvider.fixedges(string);
			Matcher matches = p.matcher(string);
			ArrayList<String> allemotes = new ArrayList<String>();
			while(matches.find()){
				allemotes .add(string.substring(matches.start(),matches.end()));
			}
			String found = StringUtils.join(allemotes ,"====");
			System.out.format("...%s\n...[%s]\n",string,found);
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
//			"aaaaaaah naaaaaao;;; hino do corinthiaans naaao DDDDD: IOEUIAOUEAIOUOAEIUEIO' (@stevens_adam live on http://twitcam.com/2bqv)",
//			"Maureen Green,the former TV anchor, blogs to keep up with social and workforce trends|Gloria Wright / The .. http://oohja.com/x7OhR",
//			"@avlsuresh I didnt know about it :-)). I would be even more happy when you will give the old one to me.",
//			"@_CarolineF_ *Nods, smiling* Just alright? *touches your arm, seeing flashes of your recent past and drawing my hand away quickly in shock*"
//			"RT @BThompsonWRITEZ: @libbyabrego honored?! Everybody knows the libster is nice with it...lol...(thankkkks a bunch;))",
//			"Big work event tonight means I've got to dress up, mix & mingle with the donors & bust out the non-granola hippy deodorant. Hurumph",
//			"here is a #hashTag",
			"\u30A2\u30DE\u30BE\u30F3\uFF0F\u6D0B\u66F8\u306E\u65B0\u7740\uFF08\uFF13\uFF09Alpine Glow \u3010\uFFE54,461\u3011 http://tinyurl.com/3yslnw5\u3000(http://tinyurl.com/24e8alm )",
			"http://assfsdhgftgfvkcsjtbvtbgmktyhklgbmkgskdmvdthydtyhgfyhdfht (@andreesrr live on http://twitcam.com/2bl4v"
		};
		for (String text: tweets) {
			TweetTokeniser tokeniser = new TweetTokeniser(text);
//			System.out.println("Tweet: " + text);
			String tokens = "[" + StringUtils.join(tokeniser.getTokens(), ",") + "]";
//			System.out.println("Tokens: " + tokens);
		}
	}
	
//	@Test
	public void testTweetTokeniser() throws UnsupportedEncodingException, TweetTokeniserException{
		for (String text: allTweets) {
			TweetTokeniser tokeniser = new TweetTokeniser(text);
//			System.out.println("Tweet: " + text);
			String tokens = "[" + StringUtils.join(tokeniser.getTokens(), ",") + "]";
//			System.out.println("Tokens: " + tokens);
		}
//		new TweetTokeniser("@geektome this is where I point out the 6-mile long #Blackhawks bandwagon and the #fire players on #USMNT #supportchicagoregardlessofsport");	
	}
	
	public void compareAgainstPython() throws TweetTokeniserException, IOException, InterruptedException{
		String pythonScriptLocation = "/usr/bin/env python /Users/ss/Development/python/trendminer-python/twokenize.py";
		for (String text: allTweets) {
			Map<String,String> tweetOut = new HashMap<String,String>();
			tweetOut.put("text", text);
			
			List<String> tokenisedPY = launchScript(pythonScriptLocation,gson.toJson(tweetOut));
			
			TweetTokeniser tokeniser = new TweetTokeniser(text);
			List<Token> tokenisedJ = tokeniser.getTokens();
			String tokens = "[" + StringUtils.join(tokenisedJ, ",") + "]";
			String pytokens = "[" + StringUtils.join(tokenisedPY,",") + "]";
			String diffString = "";
			
			for(int i = 0; i < Math.min(tokenisedJ.size(), tokenisedPY.size()); i++){
				String tpy = tokenisedPY.get(i);
				String tj = tokenisedJ.get(i).getText(); 
				if(!tpy.equals(tj)){
					diffString += String.format("%s != %s,",tpy,tj);
				}
			}
			if(tokenisedJ.size()!=tokenisedPY.size()){
				if(tokenisedPY.size() > tokenisedJ.size()){
					diffString += ",python was LONGER";
				}
				else{
					diffString += ",java was LONGER";
				}
			}
			if(!diffString.equals("")){
				
//				System.out.println();
//				System.out.println("======");
//				System.out.println("Tweet: " + text);
//				System.out.println("JaTokens: " + tokens);
//				System.out.println("PyTokens: " + pytokens);
//				System.out.println("DIFFERENCE: " + diffString);
			}
		}
//		new TweetTokeniser("@geektome this is where I point out the 6-mile long #Blackhawks bandwagon and the #fire players on #USMNT #supportchicagoregardlessofsport");	
	}
	
	@Test
	public void testGoodBadAll() throws UnsupportedEncodingException, TweetTokeniserException{
		IndependentPair<String, int[]>[] teststr = new IndependentPair[]{
				IndependentPair.pair("RT @erkthajerk: @Erkthajerk beat sale going on now til march 31st. Contact for details",new int[]{16,11,5}),
				IndependentPair.pair("you should all follow @sinjax #ff #awesomeGuy",new int[]{7,4,3}),
				IndependentPair.pair("@_CarolineF_ *Nods, smiling* Just alright? *touches your arm, seeing flashes of your recent past and drawing my hand away quickly in shock*",new int[]{29,21,8}),
				IndependentPair.pair("#some_dirty-hashtag right here",new int[]{3,2,1}),
				IndependentPair.pair("RT @GardenForkTV: The Labs in the park - http://bit.ly/doHueQ New on Gardenfork //they look adorable in the snow http://ff.im/-gHOF7",new int[]{21,15,6}),
				IndependentPair.pair("RT @Adam_Schefter: Florida QB Tim Tebow broke the combine record for QBs with a 38-inch vertical jump. He also ran an impressive 40 time",new int[]{26,22,4}),
		};
		
		for (IndependentPair<String, int[]> pair: teststr) {
			String string = pair.firstObject();
			int[] expectedCounts = pair.secondObject();
			TweetTokeniser tokeniser = new TweetTokeniser(string);
//			System.out.println(tokeniser.getStringTokens().size() + ": " + tokeniser.getStringTokens());
//			System.out.println(tokeniser.getProtectedStringTokens().size() + ": " + tokeniser.getProtectedStringTokens());
//			System.out.println(tokeniser.getUnprotectedStringTokens().size() + ": " + tokeniser.getUnprotectedStringTokens());
			assertTrue(expectedCounts[0] == tokeniser.getStringTokens().size());
			assertTrue(expectedCounts[1] == tokeniser.getUnprotectedStringTokens().size());
			assertTrue(expectedCounts[2] == tokeniser.getProtectedStringTokens().size());
		}
		
	}
	
	private List<String> launchScript(String pythonScriptLocation, String json) throws IOException, InterruptedException {
		Process p = Runtime.getRuntime().exec(pythonScriptLocation);
		PrintStream ps = new PrintStream(p.getOutputStream());
		ps.println(json);
		ps.close();
		p.waitFor();
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = br.readLine();
		Map<String,Object> output = gson.fromJson(line, Map.class);
		return (List<String>) ((Map<String,Object>)output.get("analysis")).get("tokens");
	}
	
	public static void main(String[] args) throws Exception{
		TweetTokeniserTest test = new TweetTokeniserTest();
		test.setup();
		test.compareAgainstPython();
	}
}
