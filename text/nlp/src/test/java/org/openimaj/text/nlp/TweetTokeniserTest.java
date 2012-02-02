package org.openimaj.text.nlp;

import gov.sandia.cognition.text.token.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

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
	}
	
	@Test
	public void testTweetTokeniser() throws UnsupportedEncodingException, TweetTokeniserException{
//		for (String text: allTweets) {
//			TweetTokeniser tokeniser = new TweetTokeniser(text);
//			String tokens = "[" + StringUtils.join(tokeniser.getTokens(), ",") + "]";
//			System.out.println("Tokens: " + tokens);
//		}
//		
	}
}
