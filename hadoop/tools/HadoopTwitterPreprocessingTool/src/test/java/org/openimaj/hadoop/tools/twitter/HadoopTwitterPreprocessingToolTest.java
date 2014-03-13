/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
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
package org.openimaj.hadoop.tools.twitter;

import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.data.RandomData;
import org.openimaj.io.FileUtils;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.GeneralJSONTwitter;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.collection.FileTwitterStatusList;
import org.openimaj.twitter.collection.MemoryTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusList;

/**
 * Test some key functionality of the twitter preprocessing tool over hadoop
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class HadoopTwitterPreprocessingToolTest {
	/**
	 *
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private static final String JSON_TWITTER = "/org/openimaj/twitter/json_tweets.txt";
	private static final String JSON_GEO_TWITTER = "/org/openimaj/twitter/geo-sample.json";
	private static final String RAW_TWITTER = "/org/openimaj/twitter/tweets_fewer.txt";
	private static final String BROKEN_RAW_TWITTER = "/org/openimaj/twitter/broken_raw_tweets.txt";
	private static final String MONTH_LONG_TWITTER = "/org/openimaj/twitter/sample-2010-10.json";
	private File jsonTwitterInputFile;
	private File rawTwitterInputFile;
	private String commandFormat;
	private File brokenRawTwitterInputFile;
	private String modeFormat;

	private File jsonGeoTwitterInputFile;

	private File monthLongTwitterInputFile;

	/**
	 * Prepare all input files
	 * 
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		jsonTwitterInputFile = fileFromStream(HadoopTwitterPreprocessingToolTest.class.getResourceAsStream(JSON_TWITTER));
		jsonGeoTwitterInputFile = fileFromStream(HadoopTwitterPreprocessingToolTest.class
				.getResourceAsStream(JSON_GEO_TWITTER));
		rawTwitterInputFile = fileFromStream(HadoopTwitterPreprocessingToolTest.class.getResourceAsStream(RAW_TWITTER));
		brokenRawTwitterInputFile = fileFromStream(HadoopTwitterPreprocessingToolTest.class
				.getResourceAsStream(BROKEN_RAW_TWITTER));
		monthLongTwitterInputFile = fileFromStream(HadoopTwitterPreprocessingToolTest.class
				.getResourceAsStream(MONTH_LONG_TWITTER));

		commandFormat = "-i %s -o %s %s -om %s -rm -v";
		modeFormat = "-m %s";
	}

	private File fileFromStream(InputStream stream) throws IOException {
		final File f = folder.newFile("tweet" + stream.hashCode() + ".txt");
		final PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(f)));
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = null;
		while ((line = reader.readLine()) != null) {
			writer.println(line);
		}
		writer.flush();
		writer.close();
		return f;
	}

	/**
	 * Using hadoop to tokenise some json tweets
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJSONTokenise() throws Exception {
		final String mode = "TOKENISE";
		final File outJSON = folder.newFile("tokens-testJSONTokenise.json");
		performTest(outJSON, jsonTwitterInputFile, "", mode);
	}

	// /**
	// * Using hadoop to tokenise some json tweets
	// *
	// * @throws Exception
	// */
	// @Test
	// public void testJSONTokeniseTwitterOutput() throws Exception {
	// final String mode = "TOKENISE";
	// final File outJSON = folder.newFile("tokens-testJSONTokenise.json");
	// performTest(outJSON, jsonTwitterInputFile, GeneralJSONTwitter.class,
	// "-ot TWITTER", mode);
	// }

	/**
	 * Using hadoop to tokenise some json tweets
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJSONGEOTokenise() throws Exception {
		final String mode = "TOKENISE";
		final File outJSON = folder.newFile("tokens-testJSONTokenise.json");
		perform(outJSON, jsonGeoTwitterInputFile, "-prf GEO", mode);
	}

	/**
	 * Stem using some more difficult raw text
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTweetTokJSONDateRange() throws Exception {
		final String mode = "TOKENISE";
		final File outJSON = folder.newFile("tokens-testJSONTokenise.json");
		perform(outJSON, monthLongTwitterInputFile, "-prf DATE -drng 2010/09/01,2010/11/30", mode);
	}

	/**
	 * Using hadoop to tokenise some json tweets
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJSONRANDOMTokenise() throws Exception {
		final String mode = "TOKENISE";
		final File outJSON = folder.newFile("tokens-testJSONTokenise.json");
		perform(outJSON, jsonGeoTwitterInputFile, "-prf RANDOM", mode);
	}

	/**
	 * Using hadoop to tokenise some json tweets
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJSONRANDOMTokeniseLZOOutput() throws Exception {
		try {
			Class.forName("com.hadoop.compression.lzo.LzopCodec");
			final String mode = "TOKENISE";
			final File outJSON = folder.newFile("tokens-testJSONTokenise.json");
			perform(outJSON, jsonGeoTwitterInputFile, "-lzoc", mode);
		} catch (final ClassNotFoundException e) {
			System.err.println("LZO not found on classpath; skipping test");
		}
	}

	/**
	 * Using hadoop to tokenise some json tweets
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJSONStemmed() throws Exception {
		final String mode = "PORTER_STEM";
		final File outJSON = folder.newFile("tokens-testJSONStemmed.json");
		performTest(outJSON, jsonTwitterInputFile, "", mode);
	}

	/**
	 * Using hadoop to tokenise some raw tweets
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRAWTokenise() throws Exception {
		final String mode = "TOKENISE";
		final File outJSON = folder.newFile("tokens-testRAWTokenise.raw");
		performTest(outJSON, rawTwitterInputFile, "", mode);
	}

	/**
	 * Using hadoop to tokenise some raw tweets
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBrokenRAWTokenise() throws Exception {
		final String mode = "TOKENISE";
		final File outJSON = folder.newFile("tokens-broken.raw");
		performTest(outJSON, brokenRawTwitterInputFile, "", mode);
	}

	/**
	 * Using hadoop to tokenise some raw tweets
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJSONTokeniseLang() throws Exception {
		final File outJSON = folder.newFile("tokenslang-testJSONTokeniseLang.json");
		performTest(outJSON, jsonTwitterInputFile, "", "TOKENISE", "LANG_ID");
	}

	/**
	 * Using hadoop to tokenise some raw tweets
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJSONStem() throws Exception {
		final File outJSON = folder.newFile("tokenslang-testJSONStem.json");
		performTest(outJSON, rawTwitterInputFile, "", "PORTER_STEM");
	}

	private void performTest(File outputFile, File inputFile, String otherargs, String... mode) throws Exception {
		performTest(outputFile, inputFile, USMFStatus.class, otherargs, mode);
	}

	private void performTest(File outputFile, File inputFile, Class<? extends GeneralJSON> readtype, String otherargs,
			String... mode) throws Exception
	{
		String commandArgs = String.format(commandFormat, inputFile, outputFile, createModes(mode), "APPEND");
		commandArgs += " " + otherargs;
		final String[] commandArgsArr = commandArgs.split(" ");
		HadoopTwitterPreprocessingTool.main(commandArgsArr);
		final HadoopTwitterPreprocessingToolOptions opts = new HadoopTwitterPreprocessingToolOptions(createModes(mode)
				.split(" "), false);
		opts.prepare();
		assertTrue(checkSameAnalysis(
				inputFile,
				firstPart(outputFile),
				opts.preprocessingMode(),
				readtype));
		FileUtils.deleteRecursive(outputFile);

	}

	private void perform(File outputFile, File inputFile, String otherargs, String... mode) throws Exception {
		String commandArgs = String.format(commandFormat, inputFile, outputFile, createModes(mode), "APPEND");
		commandArgs += " " + otherargs;
		final String[] commandArgsArr = commandArgs.split(" ");
		HadoopTwitterPreprocessingTool.main(commandArgsArr);
	}

	private String createModes(String[] mode) {
		final String[] modeFormatted = new String[mode.length];
		for (int i = 0; i < modeFormatted.length; i++) {
			modeFormatted[i] = String.format(modeFormat, mode[i]);
		}
		return StringUtils.join(modeFormatted, " ");
	}

	private File firstPart(File tokenOutJSON) {
		final File[] parts = tokenOutJSON.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("part");
			}
		});
		return parts[0];
	}

	int[] range(int start, int stop)
	{
		final int[] result = new int[stop - start];

		for (int i = 0; i < stop - start; i++)
			result[i] = start + i;

		return result;
	}

	boolean checkSameAnalysis(File unanalysed, File analysed, List<TwitterPreprocessingMode<?>> modelist,
			Class<? extends GeneralJSON> readtype) throws IOException
	{
		final TwitterStatusList<USMFStatus> unanalysedTweetsF = FileTwitterStatusList.readUSMF(unanalysed, "UTF-8",
				GeneralJSONTwitter.class);
		final TwitterStatusList<USMFStatus> analysedTweetsF = FileTwitterStatusList.readUSMF(analysed, "UTF-8", readtype);

		final MemoryTwitterStatusList<USMFStatus> unanalysedTweets = new MemoryTwitterStatusList<USMFStatus>();
		for (final USMFStatus twitterStatus : unanalysedTweetsF) {
			if (twitterStatus.isInvalid())
				continue;
			unanalysedTweets.add(twitterStatus);
		}
		final MemoryTwitterStatusList<USMFStatus> analysedTweets = new MemoryTwitterStatusList<USMFStatus>();
		for (final USMFStatus twitterStatus : analysedTweetsF) {
			if (twitterStatus.isInvalid())
				continue;
			analysedTweets.add(twitterStatus);
		}

		final Map<String, USMFStatus> analysedMap = mapById(analysedTweets);

		final int N_TO_TEST = 10;
		int[] toTest = null;
		if (unanalysedTweets.size() < N_TO_TEST) {
			toTest = range(0, unanalysedTweets.size());
		}
		else {
			toTest = RandomData.getUniqueRandomInts(N_TO_TEST, 0, unanalysedTweets.size());
			Arrays.sort(toTest);
		}

		System.out.format("Checking equality of %d tweets\n", toTest.length);
		System.out.format("Checking tweets at index: %s\n", Arrays.toString(toTest));
		final int steps = toTest.length / 10 > 0 ? toTest.length / 10 : 1;

		for (int i = 0; i < toTest.length; i++) {
			// int i = 1958;

			if (i % (steps) == 0)
				System.out.format("...%d ", i);
			final int index = toTest[i];
			final USMFStatus nowAnalysed = unanalysedTweets.get(index);
			for (final TwitterPreprocessingMode<?> twitterPreprocessingMode : modelist) {
				twitterPreprocessingMode.process(nowAnalysed);
			}

			USMFStatus analysedTweet = null;
			if (nowAnalysed.id == 0)
				analysedTweet = analysedMap.get(nowAnalysed.text);
			else
				analysedTweet = analysedMap.get(nowAnalysed.id + "");
			if (!nowAnalysed.equals(analysedTweet))
				return false;
		}
		System.out.println();
		return true;
	}

	private Map<String, USMFStatus> mapById(TwitterStatusList<USMFStatus> analysedTweets) {
		final Map<String, USMFStatus> statusMap = new HashMap<String, USMFStatus>();
		for (final USMFStatus s : analysedTweets) {
			if (s.id != 0)
				statusMap.put(s.id + "", s);
			else
				statusMap.put(s.text, s);
		}
		return statusMap;
	}
}
