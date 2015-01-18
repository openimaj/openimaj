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
package org.openimaj.twitter.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.io.IOUtils;
import org.openimaj.twitter.GeneralJSONTwitter;
import org.openimaj.twitter.GeneralJSONTwitterRawText;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.collection.FileTwitterStatusList;
import org.openimaj.twitter.collection.MemoryTwitterStatusList;
import org.openimaj.twitter.collection.StreamTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusList;

/**
 * Test the twitter
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk),
 * 
 */
public class TwitterUtilsTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	/**
	 * see if reading the tweet dates works
	 * 
	 * @throws ParseException
	 * @throws IOException
	 */
	@Test
	public void testDates() throws ParseException, IOException {
		final File twitterfile = fileFromeStream(USMFStatus.class
				.getResourceAsStream("/org/openimaj/twitter/json_tweets.txt"));
		final FileTwitterStatusList<USMFStatus> status = FileTwitterStatusList.readUSMF(twitterfile, "UTF-8",
				GeneralJSONTwitter.class);
		for (final USMFStatus twitterStatus : status) {
			if (twitterStatus.isInvalid())
				continue;
			final DateTime d = twitterStatus.createdAt();
			assertEquals(d.getYear(), 2010);
			assertEquals(d.getMonthOfYear(), 10);
		}
	}

	/**
	 * See if we can read/write tweet analysis
	 * 
	 * @throws IOException
	 */
	@Test
	public void readJSONTweet() throws IOException {
		final InputStream stream = USMFStatus.class.getResourceAsStream("/org/openimaj/twitter/json_tweets.txt");
		final USMFStatus status = IOUtils.read(stream, new USMFStatus(GeneralJSONTwitter.class), "UTF-8");
		status.addAnalysis("someString", "with a value");
		status.addAnalysis("someInt", 1f);

		final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		IOUtils.writeASCII(outStream, status, "UTF-8");
		final byte[] arr = outStream.toByteArray();
		final USMFStatus readStatus = IOUtils.read(new ByteArrayInputStream(arr), USMFStatus.class, "UTF-8");

		assertTrue(status.equals(readStatus));
	}

	/**
	 * See if we can read text that isn't tweets into a {@link USMFStatus}
	 * 
	 * @throws IOException
	 */
	@Test
	public void readRawTweet() throws IOException {
		final InputStream stream = USMFStatus.class.getResourceAsStream("/org/openimaj/twitter/tweets.txt");
		final USMFStatus status = IOUtils.read(stream, new USMFStatus());
		status.addAnalysis("someString", "with a value");
		status.addAnalysis("someInt", 1f);

		final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		IOUtils.writeASCII(outStream, status);
		final byte[] arr = outStream.toByteArray();
		final USMFStatus readStatus = IOUtils.read(new ByteArrayInputStream(arr), USMFStatus.class);

		assertTrue(status.equals(readStatus));
	}

	/**
	 * See if we can take streams of {@link USMFStatus} instances into a memory
	 * list.
	 * 
	 * @throws IOException
	 */
	@Test
	public void readWriteStreamMemoryTweets() throws IOException {
		final InputStream stream = USMFStatus.class.getResourceAsStream("/org/openimaj/twitter/tweets.txt");
		final TwitterStatusList<USMFStatus> status = StreamTwitterStatusList.readUSMF(stream,
				GeneralJSONTwitterRawText.class);
		final TwitterStatusList<USMFStatus> memoryLoaded = new MemoryTwitterStatusList<USMFStatus>(status);

		final File ascii = folder.newFile("twitter" + stream.hashCode() + ".json");
		IOUtils.writeASCII(ascii, memoryLoaded);
		final TwitterStatusList<USMFStatus> readStatus = MemoryTwitterStatusList.read(ascii);

		assertTrue(memoryLoaded.equals(readStatus));
		ascii.delete();
	}

	/**
	 * Read tweets, Write {@link USMFStatus} to a file, read them again, check
	 * equality.
	 * 
	 * @throws IOException
	 */
	@Test
	public void readWriteFileTweets() throws IOException {
		final File twitterfile = fileFromeStream(USMFStatus.class.getResourceAsStream("/org/openimaj/twitter/tweets.txt"));
		final List<USMFStatus> status = FileTwitterStatusList.readUSMF(twitterfile, GeneralJSONTwitterRawText.class);
		MemoryTwitterStatusList<USMFStatus> memoryLoaded = new MemoryTwitterStatusList<USMFStatus>(status);
		memoryLoaded = new MemoryTwitterStatusList<USMFStatus>(memoryLoaded.subList(0, 10));
		final File ascii = folder.newFile("twitter" + twitterfile.hashCode() + ".json");
		IOUtils.writeASCII(ascii, memoryLoaded);
		final TwitterStatusList<USMFStatus> readStatus = FileTwitterStatusList.readUSMF(ascii);

		assertTrue(memoryLoaded.equals(readStatus));
	}

	@Test
	public void readRandomFileTweets() throws IOException {
		final File twitterfile = fileFromeStream(USMFStatus.class
				.getResourceAsStream("/org/openimaj/twitter/json_tweets.txt"));
		final FileTwitterStatusList<USMFStatus> status = FileTwitterStatusList.readUSMF(twitterfile, "UTF-8",
				GeneralJSONTwitter.class);
		MemoryTwitterStatusList<USMFStatus> memoryLoaded = new MemoryTwitterStatusList<USMFStatus>(status);
		for (int i = 0; i < status.size(); i++) {
			final boolean eq = status.get(i).equals(memoryLoaded.get(i));
			if (!eq)
				assertTrue(eq);
		}
		memoryLoaded = new MemoryTwitterStatusList<USMFStatus>(status.randomSubList(98));
		final File ascii = folder.newFile("twitter" + twitterfile.hashCode() + ".json");
		IOUtils.writeASCII(ascii, memoryLoaded, "UTF-8");
		final TwitterStatusList<USMFStatus> readStatus = FileTwitterStatusList.readUSMF(ascii, "UTF-8");
		for (int i = 0; i < readStatus.size(); i++) {
			final boolean eq = readStatus.get(i).equals(memoryLoaded.get(i));
			if (!eq)
				assertTrue(eq);
		}
	}

	// @Test
	// public void readTweetsToUSMFWriteToTweets() throws IOException{
	// File twitterfile =
	// fileFromeStream(USMFStatus.class.getResourceAsStream("/org/openimaj/twitter/json_tweets.txt"));
	// FileTwitterStatusList<USMFStatus> status =
	// FileTwitterStatusList.readUSMF(twitterfile,"UTF-8",GeneralJSONTwitter.class);
	// MemoryTwitterStatusList<USMFStatus> memoryLoaded = new
	// MemoryTwitterStatusList<USMFStatus>(status.randomSubList(10));
	// // add some cheeky analysis
	// for (USMFStatus usmfStatus : memoryLoaded) {
	// usmfStatus.addAnalysis("thing-int", 1);
	// usmfStatus.addAnalysis("thing-str", "str");
	// }
	// File ascii = folder.newFile("twitter" +twitterfile.hashCode()+ ".json");
	// IOUtils.writeASCII(ascii, new
	// ConvertUSMFList(memoryLoaded,GeneralJSONTwitter.class),"UTF-8");
	// FileTwitterStatusList<USMFStatus> loadedFromWritten =
	// FileTwitterStatusList.readUSMF(ascii,"UTF-8",GeneralJSONTwitter.class);
	// MemoryTwitterStatusList<USMFStatus> memoryLoadedFromWritten = new
	// MemoryTwitterStatusList<USMFStatus>(loadedFromWritten);
	// for (int i = 0; i < memoryLoaded.size(); i++) {
	// boolean eq = memoryLoaded.get(i).equals(memoryLoadedFromWritten.get(i));
	// if(!eq)
	// assertTrue(eq);
	// }
	// }

	@Test
	public void readBrokenUTFTweet() throws IOException {
		final File twitterfile = fileFromeStream(USMFStatus.class
				.getResourceAsStream("/org/openimaj/twitter/broken_json_tweets.txt"));
		final List<USMFStatus> status = FileTwitterStatusList.readUSMF(twitterfile, "UTF-8", GeneralJSONTwitter.class);
		final MemoryTwitterStatusList<USMFStatus> memoryLoaded = new MemoryTwitterStatusList<USMFStatus>(status);
		final USMFStatus ts = status.get(0);
		final USMFStatus tm = memoryLoaded.get(0);

		final File ascii = folder.newFile("twitter" + twitterfile.hashCode() + ".json");
		IOUtils.writeASCII(ascii, memoryLoaded, "UTF-8");
		assertTrue(ts.equals(tm));
	}

	@Test
	public void readBrokenUTFTweetRAW() throws IOException {
		// Because octopussy is silly
		final File twitterfile = fileFromeStream(USMFStatus.class
				.getResourceAsStream("/org/openimaj/twitter/broken_raw_tweets.txt"));

		final List<USMFStatus> status = FileTwitterStatusList.readUSMF(twitterfile, "UTF-8");
		final MemoryTwitterStatusList<USMFStatus> memoryLoaded = new MemoryTwitterStatusList<USMFStatus>(status);
		final USMFStatus ts = status.get(0);
		final USMFStatus tm = memoryLoaded.get(0);

		final File ascii = folder.newFile("twitter.json");
		IOUtils.writeASCII(ascii, memoryLoaded, "UTF-8");

		assertTrue(ts.equals(tm));
		assertTrue(ts.equals(FileTwitterStatusList.readUSMF(ascii, "UTF-8").get(0)));
		twitterfile.delete();
	}

	protected File fileFromeStream(InputStream stream) throws IOException {
		final File f = folder.newFile("broken_raw" + stream.hashCode() + ".txt");
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
}
