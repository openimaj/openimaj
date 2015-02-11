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
		jsonGeoTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class
				.getResourceAsStream(GEO_JSON_TWITTER));
		jsonTwitterUTFInputFile = fileFromStream(TwitterPreprocessingToolTests.class
				.getResourceAsStream(JSON_TWITTER_UTF));
		rawTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(RAW_TWITTER));
		rawFewerTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class
				.getResourceAsStream(RAW_FEWER_TWITTER));
		brokenRawTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class
				.getResourceAsStream(BROKEN_RAW_TWITTER));
		monthLongTwitterInputFile = fileFromStream(TwitterPreprocessingToolTests.class
				.getResourceAsStream(MONTH_LONG_TWITTER));
		retweetedStatusFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(RETWEETED_STATUS));
		accidentlyBrokenFile = fileFromStream(TwitterPreprocessingToolTests.class.getResourceAsStream(ACCIDENTLY_BROKEN));

		commandFormat = "-i %s -o %s -m %s -om %s -rm -q -ot RDF";
	}

	private File fileFromStream(InputStream stream) throws IOException {
		final File f = folder.newFile("tweet" + stream.hashCode() + ".txt");
		final PrintWriter writer = new PrintWriter(f, "UTF-8");
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
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
		final String mode = "LANG_ID";
		final File languageOutJSON = folder.newFile("retweetedStatus.json");
		final String commandArgs = String.format(commandFormat, retweetedStatusFile, languageOutJSON, mode, "APPEND");
		final String[] commandArgsArr = commandArgs.split(" ");
		TwitterPreprocessingTool.main(commandArgsArr);

		languageOutJSON.delete();
	}
}
