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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.data.RandomData;
import org.openimaj.io.FileUtils;
import org.openimaj.text.nlp.TweetTokeniser;
import org.openimaj.tools.twitter.modes.preprocessing.LanguageDetectionMode;
import org.openimaj.tools.twitter.modes.preprocessing.SentimentExtractionMode;
import org.openimaj.tools.twitter.modes.preprocessing.StemmingMode;
import org.openimaj.tools.twitter.modes.preprocessing.TokeniseMode;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.tools.twitter.options.TwitterPreprocessingToolOptions;
import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.GeneralJSONTwitter;
import org.openimaj.twitter.GeneralJSONTwitterRawText;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.collection.FileTwitterStatusList;
import org.openimaj.twitter.collection.MemoryTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusList;

/**
 * Test the command line twitter preprocessing tool
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TwitterPreprocessingToolTests {
	/**
	 * the output folder
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private static final String JSON_TWITTER = "/org/openimaj/twitter/json_tweets.txt";
	private static final String GEO_JSON_TWITTER = "/org/openimaj/twitter/geo-sample.json";
	private static final String JSON_TWITTER_UTF = "/org/openimaj/twitter/json_tweets_utf.txt";
	private static final String RAW_TWITTER = "/org/openimaj/twitter/tweets.txt";
	private static final String RAW_FEWER_TWITTER = "/org/openimaj/twitter/tweets_fewer.txt";
	private static final String BROKEN_RAW_TWITTER = "/org/openimaj/twitter/broken_raw_tweets.txt";
	private static final String MONTH_LONG_TWITTER = "/org/openimaj/twitter/sample-2010-10.json";
	private static final String RETWEETED_STATUS = "/org/openimaj/twitter/deletedRetweetedTweets.json";
	private static final String ACCIDENTLY_BROKEN = "/org/openimaj/twitter/accidentlyBrokenTweet.json";

	private File jsonTwitterInputFile;
	private File jsonTwitterUTFInputFile;
	private File rawTwitterInputFile;
	private String commandFormat;
	private File brokenRawTwitterInputFile;
	private File rawFewerTwitterInputFile;

	private File jsonGeoTwitterInputFile;

	private File monthLongTwitterInputFile;
	private File retweetedStatusFile;

	private File accidentlyBrokenFile;

	/**
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException{
		jsonTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(JSON_TWITTER));
		jsonGeoTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(GEO_JSON_TWITTER));
		jsonTwitterUTFInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(JSON_TWITTER_UTF));
		rawTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(RAW_TWITTER));
		rawFewerTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(RAW_FEWER_TWITTER));
		brokenRawTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(BROKEN_RAW_TWITTER));
		monthLongTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(MONTH_LONG_TWITTER));
		retweetedStatusFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(RETWEETED_STATUS));
		accidentlyBrokenFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(ACCIDENTLY_BROKEN));

		commandFormat = "-i %s -o %s -m %s -om %s -rm -q";
	}

	private File fileFromStream(InputStream stream) throws IOException {
		File f = folder.newFile("tweet" + stream.hashCode() + ".txt");
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
		File tokenOutJSON = folder.newFile("tokens-testTweetTokeniseJSON.json");
		String commandArgs = String.format(commandFormat,jsonTwitterInputFile,tokenOutJSON,tokMode,"APPEND");
		String[] commandArgsArr = commandArgs.split(" ");
		TwitterPreprocessingTool.main(commandArgsArr);
		TokeniseMode m = new TokeniseMode();
		assertTrue(checkSameAnalysis(jsonTwitterInputFile,tokenOutJSON,m));
		tokenOutJSON.delete();
	}

	/**
	 * Tokenise using json input
	 *
	 * @throws IOException
	 */
	@Test
	public void testTweetTokeniseTwitterOUTJSON() throws IOException{
		String tokMode = "TOKENISE";
		File tokenOutJSON = folder.newFile("tokens-testTweetTokeniseJSON.json");
		String commandArgs = String.format(commandFormat,jsonTwitterInputFile,tokenOutJSON,tokMode,"APPEND");
		commandArgs += " -ot TWITTER";
		String[] commandArgsArr = commandArgs.split(" ");
		TwitterPreprocessingTool.main(commandArgsArr);
		TokeniseMode m = new TokeniseMode();
		assertTrue(checkSameAnalysis(jsonTwitterInputFile,tokenOutJSON,m,GeneralJSONTwitter.class));
		tokenOutJSON.delete();
	}

	/**
	 * Tokenise using json input
	 * @throws Exception
	 */
	@Test
	public void testInvalidLanguageTokeniseJSON() throws Exception{
		String tokMode = "TOKENISE";
		File tokenOutJSON = folder.newFile("tokens-testTweetTokeniseJSON.json");
		String commandArgs = String.format(commandFormat,jsonTwitterInputFile,tokenOutJSON,tokMode,"APPEND");
		commandArgs += " -m LANG_ID";
		String[] commandArgsArr = commandArgs.split(" ");
		TwitterPreprocessingTool.main(commandArgsArr);
		TwitterStatusList<USMFStatus>  toktweets = FileTwitterStatusList.readUSMF(tokenOutJSON,"UTF-8");
		LanguageDetectionMode langDet = new LanguageDetectionMode();
		TokeniseMode tokModeInst = new TokeniseMode();
		for (USMFStatus twitterStatus : toktweets) {
			Map<String, Object> a = LanguageDetectionMode.results(twitterStatus, langDet);
			boolean validLanguage = TweetTokeniser.isValid((String) a.get("language"));
			Map<String, List<String>> tokens = TokeniseMode.results(twitterStatus, tokModeInst);
			boolean containsTokens = tokens.size() != 0;
			boolean valid = (containsTokens && validLanguage) || (!containsTokens && !validLanguage) ;
			if(!valid ){
				System.out.println("Language was: " + a.get("language"));
				System.out.println("Tokens were: " + tokens);
			}
			assertTrue(valid );

		}
		tokenOutJSON.delete();
	}

	/**
	 * tokenise a json tweet stream
	 * @throws IOException
	 */
	@Test
	public void testTweetTokeniseJSONStream() throws IOException{
		String tokMode = "TOKENISE";
		File tokenOutJSON = folder.newFile("tokens-testTweetTokeniseJSON.json");
		String commandArgs = String.format(commandFormat,"-",tokenOutJSON,tokMode,"APPEND");
		TwitterPreprocessingToolOptions.sysin = new FileInputStream(jsonTwitterInputFile);
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
		File languageOutJSON = folder.newFile("language-testTweetLanguageDetectJSON.json");
		String commandArgs = String.format(commandFormat,jsonTwitterInputFile,languageOutJSON,mode,"APPEND");
		String[] commandArgsArr = commandArgs.split(" ");
		TwitterPreprocessingTool.main(commandArgsArr);
		LanguageDetectionMode m = new LanguageDetectionMode();
		assertTrue(checkSameAnalysis(jsonTwitterInputFile,languageOutJSON,m));
		languageOutJSON.delete();
	}

	/**
	 * detect language using json
	 *
	 * @throws IOException
	 */
	@Test
	public void testTweetRetweetedStatus() throws IOException{
		String mode = "LANG_ID";
		File languageOutJSON = folder.newFile("retweetedStatus.json");
		String commandArgs = String.format(commandFormat,retweetedStatusFile,languageOutJSON,mode,"APPEND");
		String[] commandArgsArr = commandArgs.split(" ");
		TwitterPreprocessingTool.main(commandArgsArr);
		LanguageDetectionMode m = new LanguageDetectionMode();
		assertTrue(checkSameAnalysis(retweetedStatusFile,languageOutJSON,m));
		languageOutJSON.delete();
	}

	/**
	 * detect language using json
	 *
	 * @throws IOException
	 */
	@Test
	public void testTweetAccidentlyBrokenStatus() throws IOException{
		String mode = "LANG_ID";
		File languageOutJSON = folder.newFile("accidentlyBroken.json");
		String commandArgs = String.format(commandFormat,accidentlyBrokenFile,languageOutJSON,mode,"APPEND");
		String[] commandArgsArr = commandArgs.split(" ");
		TwitterPreprocessingTool.main(commandArgsArr);
		LanguageDetectionMode m = new LanguageDetectionMode();
		assertTrue(checkSameAnalysis(accidentlyBrokenFile,languageOutJSON,m));
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
		File tokenOutRAW = folder.newFile("tokens-testTweetTokeniseRAW.json");
		String commandArgs = String.format(commandFormat,rawTwitterInputFile,tokenOutRAW,tokMode,"APPEND") + " -it RAW";
		String[] commandArgsArr = commandArgs.split(" ");
		System.out.println("Tokenising");
		TwitterPreprocessingTool.main(commandArgsArr);
		System.out.println("Done tokenising, checking equality...");
		TokeniseMode m = new TokeniseMode();
		assertTrue(checkSameAnalysis(rawTwitterInputFile,tokenOutRAW,m,USMFStatus.class,GeneralJSONTwitterRawText.class));
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
		File tokenOutRAW = folder.newFile("tokens-testTweetTokeniseBrokenRAW.json");
		String commandArgs = String.format(commandFormat,brokenRawTwitterInputFile,tokenOutRAW,tokMode,"APPEND") + " -it RAW";
		String[] commandArgsArr = commandArgs.split(" ");
		System.out.println("Tokenising");
		TwitterPreprocessingTool.main(commandArgsArr);
		System.out.println("Done tokenising, checking equality...");
		TokeniseMode m = new TokeniseMode();
		assertTrue(checkSameAnalysis(brokenRawTwitterInputFile,tokenOutRAW,m,USMFStatus.class,GeneralJSONTwitterRawText.class));
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
		File stemOutRAW = folder.newFile("stem-testTweetStemJSON.json");
		String commandArgs = String.format(commandFormat,rawFewerTwitterInputFile,stemOutRAW,stemMode,"APPEND") + " -it RAW";
		String[] commandArgsArr = commandArgs.split(" ");
		System.out.println("Stemming");
		TwitterPreprocessingTool.main(commandArgsArr);
		System.out.println("Done tokenising, checking equality...");
		StemmingMode m = new StemmingMode();
		assertTrue(checkSameAnalysis(rawFewerTwitterInputFile,stemOutRAW,m,USMFStatus.class,GeneralJSONTwitterRawText.class));
		stemOutRAW.delete();
	}
	/**
	 * Stem using some more difficult raw text
	 *
	 * @throws IOException
	 */
	@Test
	public void testTweetTokJSONGEO() throws IOException{
		String stemMode = "TOKENISE";
		File stemOutRAW = folder.newFile("stem-testTweetStemJSON.json");
		String commandArgs = String.format(commandFormat,jsonGeoTwitterInputFile,stemOutRAW,stemMode,"APPEND");
		commandArgs += " -prf GEO";
		String[] commandArgsArr = commandArgs.split(" ");
		System.out.println("Stemming");
		TwitterPreprocessingTool.main(commandArgsArr);
		FileTwitterStatusList<USMFStatus> fl = FileTwitterStatusList.readUSMF(stemOutRAW,"UTF-8");
		for (USMFStatus twitterStatus : fl) {
			System.out.println(twitterStatus.text);
		}
		System.out.println(fl.size());
	}

	/**
	 * Stem using some more difficult raw text
	 *
	 * @throws IOException
	 */
	@Test
	public void testTweetTokJSONRANDOM() throws IOException{
		String stemMode = "TOKENISE";
		File stemOutRAW = folder.newFile("stem-testTweetStemJSON.json");
		String commandArgs = String.format(commandFormat,jsonGeoTwitterInputFile,stemOutRAW,stemMode,"APPEND");
		commandArgs += " -prf RANDOM -rfc 0.1";
		String[] commandArgsArr = commandArgs.split(" ");
		System.out.println("Stemming");
		TwitterPreprocessingTool.main(commandArgsArr);

		FileTwitterStatusList<USMFStatus> fl = FileTwitterStatusList.readUSMF(jsonGeoTwitterInputFile,"UTF-8");
		System.out.println(fl.size());
		FileTwitterStatusList<USMFStatus> flrnd = FileTwitterStatusList.readUSMF(stemOutRAW,"UTF-8");
		System.out.println(flrnd.size());
	}

	/**
	 * Stem using some more difficult raw text
	 *
	 * @throws IOException
	 */
	@Test
	public void testTweetTokJSONDate() throws IOException{
		String stemMode = "TOKENISE";
		File stemOutRAW = folder.newFile("stem-testTweetStemJSON.json");
		String commandArgs = String.format(commandFormat,monthLongTwitterInputFile,stemOutRAW,stemMode,"APPEND");
		commandArgs += " -prf DATE -from 2010/09/01 -to 2010/11/30";
		String[] commandArgsArr = commandArgs.split(" ");
		System.out.println("Date Filtering");
		TwitterPreprocessingTool.main(commandArgsArr);

		FileTwitterStatusList<USMFStatus> fl = FileTwitterStatusList.readUSMF(monthLongTwitterInputFile,"UTF-8");
		System.out.println(fl.size());
		FileTwitterStatusList<USMFStatus> fldate = FileTwitterStatusList.readUSMF(stemOutRAW,"UTF-8");
		System.out.println(fldate.size());
	}

	/**
	 * Stem using some more difficult raw text
	 *
	 * @throws IOException
	 */
	@Test
	public void testTweetTokInReplyTo() throws IOException{
		String stemMode = "TOKENISE";
		File stemOutRAW = folder.newFile("stem-testTweetStemJSON.json");
		String commandArgs = String.format(commandFormat,monthLongTwitterInputFile,stemOutRAW,stemMode,"APPEND");
		commandArgs += " -prf IN_REPLY_TO";
		String[] commandArgsArr = commandArgs.split(" ");
		System.out.println("Date Filtering");
		TwitterPreprocessingTool.main(commandArgsArr);

		FileTwitterStatusList<USMFStatus> fl = FileTwitterStatusList.readUSMF(monthLongTwitterInputFile,"UTF-8");
		System.out.println(fl.size());
		FileTwitterStatusList<USMFStatus> fldate = FileTwitterStatusList.readUSMF(stemOutRAW,"UTF-8");
		System.out.println(fldate.size());
	}

	/**
	 * Stem using some more difficult raw text
	 *
	 * @throws IOException
	 */
	@Test
	public void testTweetTokJSONDateRange() throws IOException{
		String stemMode = "TOKENISE";
		File stemOutRAW = folder.newFile("stem-testTweetStemJSON.json");
		String commandArgs = String.format(commandFormat,monthLongTwitterInputFile,stemOutRAW,stemMode,"APPEND");
		commandArgs += " -prf DATE -drng 2010/09/01,2010/11/30";
		String[] commandArgsArr = commandArgs.split(" ");
		System.out.println("Date Filtering");
		TwitterPreprocessingTool.main(commandArgsArr);

		FileTwitterStatusList<USMFStatus> fl = FileTwitterStatusList.readUSMF(monthLongTwitterInputFile,"UTF-8");
		System.out.println(fl.size());
		FileTwitterStatusList<USMFStatus> fldate = FileTwitterStatusList.readUSMF(stemOutRAW,"UTF-8");
		System.out.println(fldate.size());
	}

	/**
	 * Stem using some more difficult raw text
	 *
	 * @throws IOException
	 */
	@Test
	public void testTweetTokGrep() throws IOException{
		String stemMode = "TOKENISE";
		File stemOutRAW = folder.newFile("stem-testTweetStemJSON.json");
		String commandArgs = String.format(commandFormat,monthLongTwitterInputFile,stemOutRAW,stemMode,"APPEND");
		commandArgs += " -prf GREP -r .*a.*";
		String[] commandArgsArr = commandArgs.split(" ");
		System.out.println("Date Filtering");
		TwitterPreprocessingTool.main(commandArgsArr);

		FileTwitterStatusList<USMFStatus> fl = FileTwitterStatusList.readUSMF(monthLongTwitterInputFile,"UTF-8");
		System.out.println(fl.size());
		FileTwitterStatusList<USMFStatus> fldate = FileTwitterStatusList.readUSMF(stemOutRAW,"UTF-8");
		System.out.println(fldate.size());
	}


	/**
	 * see if the output can be shurnk down
	 * @throws IOException
	 */
	@Test
	public void testShortOutput() throws IOException{
		String stemMode = "TOKENISE";
		File stemOutJSON = folder.newFile("stem-testShortOutput.json");
		String commandArgs = String.format(commandFormat,jsonTwitterInputFile,stemOutJSON,stemMode,"CONDENSED");
		commandArgs += " -te text -te date -m LANG_ID -m PORTER_STEM";
		String[] commandArgsArr = commandArgs.split(" ");
		TwitterPreprocessingTool.main(commandArgsArr);
//		String out = FileUtils.readall(stemOutJSON);
//		System.out.println(out);
		FileTwitterStatusList<USMFStatus> statuses = FileTwitterStatusList.readUSMF(stemOutJSON, "UTF-8");
		for (USMFStatus usmfStatus : statuses) {
			assertTrue(usmfStatus.date!=null);
			assertTrue(usmfStatus.text!=null);
		}
		// Make sure the smaller output is ok somehow?
		stemOutJSON.delete();
	}

	/**
	 * make sure we can read UTF stuff
	 * @throws IOException
	 */
	@Test
	public void testUTFInput() throws IOException {
		String stemMode = "LANG_ID";
		File stemOutJSON = folder.newFile("stem-testUTFInput.json");
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

	/**
	 * see if we can deal with multiple files input
	 * @throws IOException
	 */
	@Test
	public void testMultipleInput() throws IOException {
		String stemMode = "TOKENISE";
		File stemOutJSON = folder.newFile("tokenise-testMultipleInput.json");
		File inputList  = folder.newFile("inputs-testMultipleInput.txt");
		PrintWriter listWriter = new PrintWriter(new FileWriter(inputList));
		listWriter.println(jsonTwitterInputFile.getAbsolutePath());
		listWriter.println(rawFewerTwitterInputFile.getAbsolutePath());
		listWriter.flush();
		listWriter.close();
		commandFormat = "-if %s -o %s -m %s -om %s -rm -q";
		String commandArgs = String.format(commandFormat,inputList.getAbsolutePath(),stemOutJSON,stemMode,"APPEND") + " -it RAW";
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
		return checkSameAnalysis(unanalysed,analysed, m,USMFStatus.class);
	}
	boolean checkSameAnalysis(File unanalysed,File analysed, TwitterPreprocessingMode<?> m, Class<? extends GeneralJSON> readclass) throws IOException {
		return checkSameAnalysis(unanalysed,analysed,m,readclass,GeneralJSONTwitter.class);
	}
	boolean checkSameAnalysis(File unanalysed,File analysed, TwitterPreprocessingMode<?> m, Class<? extends GeneralJSON> readclass,Class<? extends GeneralJSON> originalClass) throws IOException {
		TwitterStatusList<USMFStatus>  unanalysedTweetsF = FileTwitterStatusList.readUSMF(unanalysed,"UTF-8",originalClass);
		TwitterStatusList<USMFStatus>  analysedTweetsF = FileTwitterStatusList.readUSMF(analysed,"UTF-8",readclass);

		MemoryTwitterStatusList<USMFStatus> unanalysedTweets = new MemoryTwitterStatusList<USMFStatus>();
		for (USMFStatus twitterStatus : unanalysedTweetsF) {
			if(twitterStatus.isInvalid()) continue;
			unanalysedTweets.add(twitterStatus);
		}
		MemoryTwitterStatusList<USMFStatus> analysedTweets = new MemoryTwitterStatusList<USMFStatus>();
		for (USMFStatus twitterStatus : analysedTweetsF) {
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
			USMFStatus nowAnalysed = unanalysedTweets.get(index);
			m.process(nowAnalysed);
			USMFStatus analysedTweet = analysedTweets.get(index);
			if(!nowAnalysed.equals(analysedTweet))
				return false;
		}
		System.out.println();
		return true;
	}
	@Test
    public void testSentimentExtraction() throws IOException, Exception{
        File unanalysed = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream("/org/openimaj/twitter/json_tweets.txt"));
        TwitterStatusList<USMFStatus> tweets = FileTwitterStatusList.readUSMF(unanalysed,"UTF-8",GeneralJSONTwitter.class);
        USMFStatus tweet = tweets.get(0);
        TwitterPreprocessingMode.results(tweet, new SentimentExtractionMode());
    }
}
