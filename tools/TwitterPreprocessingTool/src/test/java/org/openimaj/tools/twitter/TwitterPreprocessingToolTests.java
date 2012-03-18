/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.tools.twitter;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.data.RandomData;
import org.openimaj.io.FileUtils;
import org.openimaj.tools.twitter.modes.preprocessing.LanguageDetectionMode;
import org.openimaj.tools.twitter.modes.preprocessing.StemmingMode;
import org.openimaj.tools.twitter.modes.preprocessing.TokeniseMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.TwitterStatus;
import org.openimaj.twitter.collection.FileTwitterStatusList;
import org.openimaj.twitter.collection.MemoryTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusList;

/**
 * Test the command line twitter preprocessing tool
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TwitterPreprocessingToolTests {
	
	private static final String JSON_TWITTER = "/org/openimaj/twitter/json_tweets.txt";
	private static final String JSON_TWITTER_UTF = "/org/openimaj/twitter/json_tweets_utf.txt";
	private static final String RAW_TWITTER = "/org/openimaj/twitter/tweets.txt";
	private static final String RAW_FEWER_TWITTER = "/org/openimaj/twitter/tweets_fewer.txt";
	private static final String BROKEN_RAW_TWITTER = "/org/openimaj/twitter/broken_raw_tweets.txt";
	private File jsonTwitterInputFile;
	private File jsonTwitterUTFInputFile;
	private File rawTwitterInputFile;
	private String commandFormat;
	private File brokenRawTwitterInputFile;
	private File rawFewerTwitterInputFile;
	@Before
	public void setup() throws IOException{
		jsonTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(JSON_TWITTER));
		jsonTwitterUTFInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(JSON_TWITTER_UTF));
		rawTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(RAW_TWITTER));
		rawFewerTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(RAW_FEWER_TWITTER));
		brokenRawTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(BROKEN_RAW_TWITTER));
		
		commandFormat = "-i %s -o %s -m %s -om %s -rm -q";
	}
	
	private File fileFromStream(InputStream stream) throws IOException {
		File f = File.createTempFile("tweet", ".txt");
		PrintWriter writer = new PrintWriter(f,"UTF-8");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		String line = null;
		while((line = reader.readLine()) != null){
			writer.println(line);
		}
		writer.flush(); writer.close();
		return f;
	}
	
	/**
	 * Tokenise using json input
	 * 
	 * @throws IOException
	 */
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
	
	/**
	 * detect language using json
	 * 
	 * @throws IOException
	 */
	@Test
	public void testTweetLanguageDetectJSON() throws IOException{
		String mode = "LANG_ID";
		File languageOutJSON = File.createTempFile("language", ".json");
		String commandArgs = String.format(commandFormat,jsonTwitterInputFile,languageOutJSON,mode,"APPEND");
		String[] commandArgsArr = commandArgs.split(" ");
		TwitterPreprocessingTool.main(commandArgsArr);
		LanguageDetectionMode m = new LanguageDetectionMode();
		assertTrue(checkSameAnalysis(jsonTwitterInputFile,languageOutJSON,m));
		languageOutJSON.delete();
	}
	
	/**
	 * Tokenise using raw tweet
	 * 
	 * @throws IOException
	 */
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
	
	/**
	 * Tokenise using some more difficult raw text
	 * 
	 * @throws IOException
	 */
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
	
	/**
	 * Stem using some more difficult raw text
	 * 
	 * @throws IOException
	 */
	@Test
	public void testTweetStemJSON() throws IOException{
		String stemMode = "PORTER_STEM";
		File stemOutRAW = File.createTempFile("stem", ".json");
		String commandArgs = String.format(commandFormat,rawFewerTwitterInputFile,stemOutRAW,stemMode,"APPEND");
		String[] commandArgsArr = commandArgs.split(" ");
		System.out.println("Stemming");
		TwitterPreprocessingTool.main(commandArgsArr);
		System.out.println("Done tokenising, checking equality...");
		StemmingMode m = new StemmingMode();
		assertTrue(checkSameAnalysis(rawFewerTwitterInputFile,stemOutRAW,m));
		stemOutRAW.delete();
	}
	
	@Test
	public void testShortOutput() throws IOException{
		String stemMode = "TOKENISE";
		File stemOutJSON = File.createTempFile("stem", ".json");
		String commandArgs = String.format(commandFormat,jsonTwitterInputFile,stemOutJSON,stemMode,"CONDENSED");
		commandArgs += " -te text -te created_at -m LANG_ID";
		String[] commandArgsArr = commandArgs.split(" ");
		TwitterPreprocessingTool.main(commandArgsArr);
		String out = FileUtils.readall(stemOutJSON);
		System.out.println(out);
		// Make sure the smaller output is ok somehow?
		stemOutJSON.delete();
	}
	
	@Test
	public void testUTFInput() throws IOException {
		String stemMode = "LANG_ID";
		File stemOutJSON = File.createTempFile("stem", ".json");
		String commandArgs = String.format(commandFormat,jsonTwitterUTFInputFile,stemOutJSON,stemMode,"APPEND");
		String[] commandArgsArr = commandArgs.split(" ");
		System.out.println("Tokenising");
		TwitterPreprocessingTool.main(commandArgsArr);
		String inp = FileUtils.readall(jsonTwitterUTFInputFile);
		String out = FileUtils.readall(stemOutJSON);
		System.out.println(inp);
		System.out.println(out);
		stemOutJSON.delete();
	}
	
	@Test
	public void testMultipleInput() throws IOException {
		String stemMode = "TOKENISE";
		File stemOutJSON = File.createTempFile("tokenise", ".json");
		File inputList  = File.createTempFile("inputs", ".txt");
		PrintWriter listWriter = new PrintWriter(new FileWriter(inputList));
		listWriter.println(jsonTwitterInputFile.getAbsolutePath());
		listWriter.println(rawFewerTwitterInputFile.getAbsolutePath());
		listWriter.flush();
		listWriter.close();
		commandFormat = "-if %s -o %s -m %s -om %s -rm -q";
		String commandArgs = String.format(commandFormat,inputList.getAbsolutePath(),stemOutJSON,stemMode,"APPEND");
		String[] commandArgsArr = commandArgs.split(" ");
		System.out.println("Tokenising");
		TwitterPreprocessingTool.main(commandArgsArr);
		int lines = FileUtils.countLines(stemOutJSON);
		System.out.println("We end up with: " + lines);
		stemOutJSON.delete();
	}
	
	int[] range(int start, int stop)
	{
	   int[] result = new int[stop-start];

	   for(int i=0;i<stop-start;i++)
	      result[i] = start+i;

	   return result;
	}
	
	boolean checkSameAnalysis(File unanalysed,File analysed, TwitterPreprocessingMode<?> m) throws IOException {
		TwitterStatusList<TwitterStatus>  unanalysedTweetsF = FileTwitterStatusList.read(unanalysed,"UTF-8");
		TwitterStatusList<TwitterStatus>  analysedTweetsF = FileTwitterStatusList.read(analysed,"UTF-8");
		
		MemoryTwitterStatusList<TwitterStatus> unanalysedTweets = new MemoryTwitterStatusList<TwitterStatus>();
		for (TwitterStatus twitterStatus : unanalysedTweetsF) {
			if(twitterStatus.isInvalid()) continue;
			unanalysedTweets.add(twitterStatus);
		}
		MemoryTwitterStatusList<TwitterStatus> analysedTweets = new MemoryTwitterStatusList<TwitterStatus>();
		for (TwitterStatus twitterStatus : analysedTweetsF) {
			if(twitterStatus.isInvalid()) continue;
			analysedTweets.add(twitterStatus);
		}
		
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
