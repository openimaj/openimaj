package org.openimaj.tools.twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TwitterPreprocessingToolRDFTests {
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
	public void setup() throws IOException {
		jsonTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(JSON_TWITTER));
		jsonGeoTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(GEO_JSON_TWITTER));
		jsonTwitterUTFInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(JSON_TWITTER_UTF));
		rawTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(RAW_TWITTER));
		rawFewerTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(RAW_FEWER_TWITTER));
		brokenRawTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(BROKEN_RAW_TWITTER));
		monthLongTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(MONTH_LONG_TWITTER));
		retweetedStatusFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(RETWEETED_STATUS));
		accidentlyBrokenFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(ACCIDENTLY_BROKEN));

		commandFormat = "-i %s -o %s -m %s -om %s -rm -q -ot RDF";
	}

	private File fileFromStream(InputStream stream) throws IOException {
		File f = folder.newFile("tweet" + stream.hashCode() + ".txt");
		PrintWriter writer = new PrintWriter(f, "UTF-8");
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		String line = null;
		while ((line = reader.readLine()) != null) {
			writer.println(line);
		}
		writer.flush();
		writer.close();
		return f;
	}

	/**
	 * detect language using json
	 * 
	 * @throws IOException
	 */
	@Test
	public void testTweetRetweetedStatus() throws IOException {
		String mode = "LANG_ID";
		File languageOutJSON = folder.newFile("retweetedStatus.json");
		String commandArgs = String.format(commandFormat, retweetedStatusFile, languageOutJSON, mode, "APPEND");
		String[] commandArgsArr = commandArgs.split(" ");
		TwitterPreprocessingTool.main(commandArgsArr);

		languageOutJSON.delete();
	}
}
