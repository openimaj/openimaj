package org.openimaj.twitter.utils;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import org.junit.Test;
import org.openimaj.io.IOUtils;
import org.openimaj.twitter.TwitterStatus;
import org.openimaj.twitter.collection.FileTwitterStatusList;
import org.openimaj.twitter.collection.MemoryTwitterStatusList;
import org.openimaj.twitter.collection.StreamTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusList;

public class TwitterUtilsTest {
	
	@Test
	public void readJSONTweet() throws IOException{
		InputStream stream = TwitterStatus.class.getResourceAsStream("/org/openimaj/twitter/json_tweets.txt");
		TwitterStatus status = IOUtils.read(stream, TwitterStatus.class);
		status.addAnalysis("someString", "with a value");
		status.addAnalysis("someInt", 1f);
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		IOUtils.writeASCII(outStream , status);
		byte[] arr = outStream.toByteArray();
		TwitterStatus readStatus = IOUtils.read(new ByteArrayInputStream(arr) , TwitterStatus.class);
		
		assertTrue(status.equals(readStatus));
	}
	
	@Test
	public void readRawTweet() throws IOException{
		InputStream stream = TwitterStatus.class.getResourceAsStream("/org/openimaj/twitter/tweets.txt");
		TwitterStatus status = IOUtils.read(stream, TwitterStatus.class);
		status.addAnalysis("someString", "with a value");
		status.addAnalysis("someInt", 1f);
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		IOUtils.writeASCII(outStream , status);
		byte[] arr = outStream.toByteArray();
		TwitterStatus readStatus = IOUtils.read(new ByteArrayInputStream(arr) , TwitterStatus.class);
		
		assertTrue(status.equals(readStatus));
	}
	
	@Test
	public void readWriteStreamMemoryTweets() throws IOException{
		InputStream stream = TwitterStatus.class.getResourceAsStream("/org/openimaj/twitter/tweets.txt");
		TwitterStatusList status = StreamTwitterStatusList.read(stream, 5);
		TwitterStatusList memoryLoaded = new MemoryTwitterStatusList(status);
		
		File ascii = File.createTempFile("twitter", "json");
		IOUtils.writeASCII(ascii,memoryLoaded );
		TwitterStatusList readStatus = MemoryTwitterStatusList.read(ascii);
		
		assertTrue(memoryLoaded.equals(readStatus));
		ascii.delete();
	}
	
	@Test
	public void readWriteFileTweets() throws IOException{
		File twitterfile = new File(TwitterStatus.class.getResource("/org/openimaj/twitter/tweets.txt").getFile());
		List<TwitterStatus> status = FileTwitterStatusList.read(twitterfile);
		MemoryTwitterStatusList memoryLoaded = new MemoryTwitterStatusList(status);
		memoryLoaded = new MemoryTwitterStatusList(memoryLoaded.subList(0, 10));
		File ascii = File.createTempFile("twitter", "json");
		IOUtils.writeASCII(ascii,memoryLoaded );
		TwitterStatusList readStatus = FileTwitterStatusList.read(ascii);
		
		assertTrue(memoryLoaded.equals(readStatus));
	}
	
	@Test
	public void readRandomFileTweets() throws IOException{
		File twitterfile = new File(TwitterStatus.class.getResource("/org/openimaj/twitter/json_tweets.txt").getFile());
		FileTwitterStatusList status = FileTwitterStatusList.read(twitterfile);
		MemoryTwitterStatusList memoryLoaded = new MemoryTwitterStatusList(status);
		for (int i = 0; i < status.size(); i++) {
			boolean eq = status.get(i).equals(memoryLoaded.get(i));
			if(!eq)
				assertTrue(eq);
		}
		memoryLoaded = new MemoryTwitterStatusList(status.randomSubList(5));
		File ascii = File.createTempFile("twitter", "json");
//		IOUtils.writeASCII(ascii,memoryLoaded );
		PrintWriter writer = new PrintWriter(ascii, "UTF-8");
		for (TwitterStatus twitterStatus : memoryLoaded) {
			writer.println(TwitterStatus.gson.toJson(twitterStatus));
		}
		writer.flush();
		TwitterStatusList readStatus = FileTwitterStatusList.read(ascii);
		for (int i = 0; i < readStatus.size(); i++) {
			boolean eq = readStatus.get(i).equals(memoryLoaded.get(i));
			if(!eq)
				assertTrue(eq);
		}
		
//		assertTrue(memoryLoaded.equals(readStatus));
	}
	@Test
	public void readBrokenUTFTweet() throws IOException{
		File twitterfile = new File(TwitterStatus.class.getResource("/org/openimaj/twitter/broken_json_tweets.txt").getFile());
		List<TwitterStatus> status = FileTwitterStatusList.read(twitterfile);
		MemoryTwitterStatusList memoryLoaded = new MemoryTwitterStatusList(status);
		TwitterStatus ts = status.get(0);
		TwitterStatus tm = memoryLoaded.get(0);
		assertTrue(ts.equals(tm));
	}
}
