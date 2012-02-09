package org.openimaj.tools.twitter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.tools.twitter.modes.TokeniseMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.TwitterStatus;
import org.openimaj.twitter.collection.FileTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusList;

public class TwitterPreprocessingToolTests {
	
	private static final String JSON_TWITTER = "/org/openimaj/twitter/json_tweets.txt";
	private static final String RAW_TWITTER = "/org/openimaj/twitter/tweets.txt";
	private File jsonTwitterInputFile;
	private File rawTwitterInputFile;
	private String commandFormat;
	@Before
	public void setup(){
		jsonTwitterInputFile = new File(
			TwitterPreprocessingToolTests.class.getResource(JSON_TWITTER).getFile()
		);
		
		rawTwitterInputFile = new File(
			TwitterPreprocessingToolTests.class.getResource(RAW_TWITTER).getFile()
		);
		
		commandFormat = "-i %s -o %s -m %s -om %s -rm";
	}
	
	@Test
	public void testTweetTokenise() throws IOException{
		String tokMode = "TOKENISE";
		File tokenOutJSON = File.createTempFile("tokens", ".json");
		String commandArgs = String.format(commandFormat,jsonTwitterInputFile,tokenOutJSON,tokMode,"APPEND");
		String[] commandArgsArr = commandArgs.split(" ");
		TwitterPreprocessingTool.main(commandArgsArr);
		TokeniseMode m = new TokeniseMode();
		assertTrue(checkSameAnalysis(jsonTwitterInputFile,tokenOutJSON,m));
	}

	private boolean checkSameAnalysis(File unanalysed,File analysed, TwitterPreprocessingMode m) throws IOException {
		TwitterStatusList unanalysedTweets = FileTwitterStatusList.read(unanalysed);
		TwitterStatusList analysedTweets = FileTwitterStatusList.read(analysed);
		
		for (int i = 0; i < unanalysedTweets.size(); i++) {
			TwitterStatus nowAnalysed = unanalysedTweets.get(i);
			m.process(nowAnalysed);
			if(!nowAnalysed.equals(analysedTweets.get(i))) 
				return false;
		}
		return true;
	}
}
