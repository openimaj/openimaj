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

import static org.junit.Assert.*;
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
import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.GeneralJSONTwitter;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.collection.FileTwitterStatusList;
import org.openimaj.twitter.collection.MemoryTwitterStatusList;
import org.openimaj.twitter.collection.StreamTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusList;

public class TwitterUtilsTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	/**
	 * see if reading the tweet dates works
	 * @throws ParseException 
	 * @throws IOException 
	 */
	@Test
	public void testDates() throws ParseException, IOException{
		File twitterfile = fileFromeStream(USMFStatus.class.getResourceAsStream("/org/openimaj/twitter/json_tweets.txt"));
		FileTwitterStatusList<USMFStatus> status = FileTwitterStatusList.read(twitterfile,"UTF-8",GeneralJSONTwitter.class);
		for (USMFStatus twitterStatus : status) {
			if(twitterStatus.isInvalid()) continue;
			DateTime d = twitterStatus.createdAt();
			assertEquals(d.getYear(),2010);
			assertEquals(d.getMonthOfYear(),10);
		}
	}
	
	@Test
	public void readJSONTweet() throws IOException{
		InputStream stream = USMFStatus.class.getResourceAsStream("/org/openimaj/twitter/json_tweets.txt");
		USMFStatus status = IOUtils.read(stream, new USMFStatus(GeneralJSONTwitter.class), "UTF-8");
		status.addAnalysis("someString", "with a value");
		status.addAnalysis("someInt", 1f);
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		IOUtils.writeASCII(outStream , status, "UTF-8");
		byte[] arr = outStream.toByteArray();
		USMFStatus readStatus = IOUtils.read(new ByteArrayInputStream(arr) , USMFStatus.class, "UTF-8");
		
		assertTrue(status.equals(readStatus));
	}
	
	@Test
	public void readRawTweet() throws IOException{
		InputStream stream = USMFStatus.class.getResourceAsStream("/org/openimaj/twitter/tweets.txt");
		USMFStatus status = IOUtils.read(stream, new USMFStatus(GeneralJSONTwitter.class));
		status.addAnalysis("someString", "with a value");
		status.addAnalysis("someInt", 1f);
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		IOUtils.writeASCII(outStream , status);
		byte[] arr = outStream.toByteArray();
		USMFStatus readStatus = IOUtils.read(new ByteArrayInputStream(arr) , USMFStatus.class);
		
		assertTrue(status.equals(readStatus));
	}
	
	@Test
	public void readWriteStreamMemoryTweets() throws IOException{
		InputStream stream = USMFStatus.class.getResourceAsStream("/org/openimaj/twitter/tweets.txt");
		TwitterStatusList<USMFStatus> status = StreamTwitterStatusList.read(stream, GeneralJSONTwitter.class);
		TwitterStatusList<USMFStatus> memoryLoaded = new MemoryTwitterStatusList<USMFStatus>(status);
		
		File ascii = folder.newFile("twitter"+ stream.hashCode() +".json");
		IOUtils.writeASCII(ascii,memoryLoaded);
		TwitterStatusList<USMFStatus> readStatus = MemoryTwitterStatusList.read(ascii);
		
		assertTrue(memoryLoaded.equals(readStatus));
		ascii.delete();
	}
	
	@Test
	public void readWriteFileTweets() throws IOException{
		File twitterfile = fileFromeStream(USMFStatus.class.getResourceAsStream("/org/openimaj/twitter/tweets.txt"));
		List<USMFStatus> status = FileTwitterStatusList.read(twitterfile);
		MemoryTwitterStatusList<USMFStatus> memoryLoaded = new MemoryTwitterStatusList<USMFStatus>(status);
		memoryLoaded = new MemoryTwitterStatusList<USMFStatus>(memoryLoaded.subList(0, 10));
		File ascii = folder.newFile("twitter" +twitterfile.hashCode()+ ".json");
		IOUtils.writeASCII(ascii,memoryLoaded );
		TwitterStatusList<USMFStatus> readStatus = FileTwitterStatusList.read(ascii);
		
		assertTrue(memoryLoaded.equals(readStatus));
	}
	
	@Test
	public void readRandomFileTweets() throws IOException{
		File twitterfile = fileFromeStream(USMFStatus.class.getResourceAsStream("/org/openimaj/twitter/json_tweets.txt"));
		FileTwitterStatusList<USMFStatus> status = FileTwitterStatusList.read(twitterfile,"UTF-8");
		MemoryTwitterStatusList<USMFStatus> memoryLoaded = new MemoryTwitterStatusList<USMFStatus>(status);
		for (int i = 0; i < status.size(); i++) {
			boolean eq = status.get(i).equals(memoryLoaded.get(i));
			if(!eq)
				assertTrue(eq);
		}
		memoryLoaded = new MemoryTwitterStatusList<USMFStatus>(status.randomSubList(98));
		File ascii = folder.newFile("twitter" +twitterfile.hashCode()+ ".json");
		IOUtils.writeASCII(ascii,memoryLoaded,"UTF-8");
		TwitterStatusList<USMFStatus> readStatus = FileTwitterStatusList.read(ascii, "UTF-8");
		for (int i = 0; i < readStatus.size(); i++) {
			boolean eq = readStatus.get(i).equals(memoryLoaded.get(i));
			if(!eq)
				assertTrue(eq);
		}
	}
	@Test
	public void readBrokenUTFTweet() throws IOException{
		File twitterfile = fileFromeStream(USMFStatus.class.getResourceAsStream("/org/openimaj/twitter/broken_json_tweets.txt")	);
		List<USMFStatus> status = FileTwitterStatusList.read(twitterfile,"UTF-8");
		MemoryTwitterStatusList<USMFStatus> memoryLoaded = new MemoryTwitterStatusList<USMFStatus>(status);
		USMFStatus ts = status.get(0);
		USMFStatus tm = memoryLoaded.get(0);
		
		File ascii = folder.newFile("twitter" + twitterfile.hashCode() + ".json");
		IOUtils.writeASCII(ascii,memoryLoaded,"UTF-8");
		assertTrue(ts.equals(tm));
	}
	
	@Test
	public void readBrokenUTFTweetRAW() throws IOException{
		// Because octopussy is silly
		File twitterfile = fileFromeStream(USMFStatus.class.getResourceAsStream("/org/openimaj/twitter/broken_raw_tweets.txt"));
		
		List<USMFStatus> status = FileTwitterStatusList.read(twitterfile,"UTF-8");
		MemoryTwitterStatusList<USMFStatus> memoryLoaded = new MemoryTwitterStatusList<USMFStatus>(status);
		USMFStatus ts = status.get(0);
		USMFStatus tm = memoryLoaded.get(0);
		
		File ascii = folder.newFile("twitter.json");
		IOUtils.writeASCII(ascii,memoryLoaded,"UTF-8");
		
		assertTrue(ts.equals(tm));
		assertTrue(ts.equals(FileTwitterStatusList.read(ascii,"UTF-8").get(0)));
		twitterfile.delete();
	}

	private File fileFromeStream(InputStream stream) throws IOException {
		File f = folder.newFile("broken_raw"+stream.hashCode()+".txt");
		PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(f)));
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = null;
		while((line = reader.readLine()) != null){writer.println(line);}
		writer.flush(); writer.close();
		return f;
	}
}
