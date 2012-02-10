package org.openimaj.tools.twitter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.data.RandomData;
import org.openimaj.tools.twitter.modes.TokeniseMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.TwitterStatus;
import org.openimaj.twitter.collection.FileTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusList;

public class TwitterPreprocessingToolTests {
	
	private static final String JSON_TWITTER = "/org/openimaj/twitter/json_tweets.txt";
	private static final String RAW_TWITTER = "/org/openimaj/twitter/tweets.txt";
	private static final String BROKEN_RAW_TWITTER = "/org/openimaj/twitter/broken_raw_tweets.txt";
	private File jsonTwitterInputFile;
	private File rawTwitterInputFile;
	private String commandFormat;
	private File brokenRawTwitterInputFile;
	@Before
	public void setup(){
		jsonTwitterInputFile = new File(
			TwitterPreprocessingToolTests.class.getResource(JSON_TWITTER).getFile()
		);
		
		rawTwitterInputFile = new File(
			TwitterPreprocessingToolTests.class.getResource(RAW_TWITTER).getFile()
		);
		
		brokenRawTwitterInputFile = new File(
			TwitterPreprocessingToolTests.class.getResource(BROKEN_RAW_TWITTER).getFile()
		);
		
		commandFormat = "-i %s -o %s -m %s -om %s -rm";
	}
	
	@Test
	public void testTweetTokeniseJSON() throws IOException{
		String tokMode = "TOKENISE";
		File tokenOutJSON = File.createTempFile("tokens", ".json");
		String commandArgs = String.format(commandFormat,jsonTwitterInputFile,tokenOutJSON,tokMode,"APPEND");
		String[] commandArgsArr = commandArgs.split(" ");
		TwitterPreprocessingTool.main(commandArgsArr);
		TokeniseMode m = new TokeniseMode();
		assertTrue(checkSameAnalysis(jsonTwitterInputFile,tokenOutJSON,m));
		tokenOutJSON.delete();
	}
	
	@Test
	public void testTweetTokeniseRAW() throws IOException{
		String tokMode = "TOKENISE";
		File tokenOutRAW = File.createTempFile("tokens", ".json");
		String commandArgs = String.format(commandFormat,rawTwitterInputFile,tokenOutRAW,tokMode,"APPEND");
		String[] commandArgsArr = commandArgs.split(" ");
		System.out.println("Tokenising");
		TwitterPreprocessingTool.main(commandArgsArr);
		System.out.println("Done tokenising, checking equality...");
		TokeniseMode m = new TokeniseMode();
		assertTrue(checkSameAnalysis(rawTwitterInputFile,tokenOutRAW,m));
		tokenOutRAW.delete();
	}
	
	@Test
	public void testTweetTokeniseBrokenRAW() throws IOException{
		String tokMode = "TOKENISE";
		File tokenOutRAW = File.createTempFile("tokens", ".json");
		String commandArgs = String.format(commandFormat,brokenRawTwitterInputFile,tokenOutRAW,tokMode,"APPEND");
		String[] commandArgsArr = commandArgs.split(" ");
		System.out.println("Tokenising");
		TwitterPreprocessingTool.main(commandArgsArr);
		System.out.println("Done tokenising, checking equality...");
		TokeniseMode m = new TokeniseMode();
		assertTrue(checkSameAnalysis(brokenRawTwitterInputFile,tokenOutRAW,m));
		tokenOutRAW.delete();
	}
	
	public int[] range(int start, int stop)
	{
	   int[] result = new int[stop-start];

	   for(int i=0;i<stop-start;i++)
	      result[i] = start+i;

	   return result;
	}
	
	private boolean checkSameAnalysis(File unanalysed,File analysed, TwitterPreprocessingMode m) throws IOException {
		TwitterStatusList unanalysedTweets = FileTwitterStatusList.read(unanalysed,"UTF-8");
		TwitterStatusList analysedTweets = FileTwitterStatusList.read(analysed,"UTF-8");
		int N_TO_TEST = 10;
		int[] toTest = null;
		if(unanalysedTweets.size() < N_TO_TEST){
			toTest = range(0,unanalysedTweets.size());
		}
		else{
			toTest = RandomData.getUniqueRandomInts(N_TO_TEST, 0, unanalysedTweets.size());
			Arrays.sort(toTest);
		}
		
		System.out.format("Checking equality of %d tweets\n",toTest.length);
		System.out.format("Checking tweets at index: %s\n",Arrays.toString(toTest));
		int steps = toTest.length/10 > 0 ? toTest.length/10 : 1;
		
		for (int i = 0; i < toTest.length; i++) {
//		int i = 1958;
			
			if(i % (steps) == 0) System.out.format("...%d ",i);
			int index = toTest[i];
			TwitterStatus nowAnalysed = unanalysedTweets.get(index);
			m.process(nowAnalysed);
			TwitterStatus analysedTweet = analysedTweets.get(index);
			if(!nowAnalysed.equals(analysedTweet)) 
				return false;
		}
		System.out.println();
		return true;
	}
}
