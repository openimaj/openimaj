package org.openimaj.twitter.utils;

import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
		TwitterStatus status = IOUtils.read(stream, TwitterStatus.class, "UTF-8");
		status.addAnalysis("someString", "with a value");
		status.addAnalysis("someInt", 1f);
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		IOUtils.writeASCII(outStream , status, "UTF-8");
		byte[] arr = outStream.toByteArray();
		TwitterStatus readStatus = IOUtils.read(new ByteArrayInputStream(arr) , TwitterStatus.class, "UTF-8");
		
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
		File twitterfile = fileFromeStream(TwitterStatus.class.getResourceAsStream("/org/openimaj/twitter/tweets.txt"));
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
		File twitterfile = fileFromeStream(TwitterStatus.class.getResourceAsStream("/org/openimaj/twitter/json_tweets.txt"));
		FileTwitterStatusList status = FileTwitterStatusList.read(twitterfile,"UTF-8");
		MemoryTwitterStatusList memoryLoaded = new MemoryTwitterStatusList(status);
		for (int i = 0; i < status.size(); i++) {
			boolean eq = status.get(i).equals(memoryLoaded.get(i));
			if(!eq)
				assertTrue(eq);
		}
		memoryLoaded = new MemoryTwitterStatusList(status.randomSubList(98));
		File ascii = File.createTempFile("twitter", "json");
		IOUtils.writeASCII(ascii,memoryLoaded,"UTF-8");
		TwitterStatusList readStatus = FileTwitterStatusList.read(ascii, "UTF-8");
		for (int i = 0; i < readStatus.size(); i++) {
			boolean eq = readStatus.get(i).equals(memoryLoaded.get(i));
			if(!eq)
				assertTrue(eq);
		}
	}
	@Test
	public void readBrokenUTFTweet() throws IOException{
		File twitterfile = fileFromeStream(TwitterStatus.class.getResourceAsStream("/org/openimaj/twitter/broken_json_tweets.txt")	);
		List<TwitterStatus> status = FileTwitterStatusList.read(twitterfile,"UTF-8");
		MemoryTwitterStatusList memoryLoaded = new MemoryTwitterStatusList(status);
		TwitterStatus ts = status.get(0);
		TwitterStatus tm = memoryLoaded.get(0);
		
		File ascii = File.createTempFile("twitter", "json");
		IOUtils.writeASCII(ascii,memoryLoaded,"UTF-8");
		assertTrue(ts.equals(tm));
	}
	
	@Test
	public void readBrokenUTFTweetRAW() throws IOException{
		// Because octopussy is silly
		File twitterfile = fileFromeStream(TwitterStatus.class.getResourceAsStream("/org/openimaj/twitter/broken_raw_tweets.txt"));
		
		List<TwitterStatus> status = FileTwitterStatusList.read(twitterfile,"UTF-8");
		MemoryTwitterStatusList memoryLoaded = new MemoryTwitterStatusList(status);
		TwitterStatus ts = status.get(0);
		TwitterStatus tm = memoryLoaded.get(0);
		
		File ascii = File.createTempFile("twitter", "json");
		IOUtils.writeASCII(ascii,memoryLoaded,"UTF-8");
		
		assertTrue(ts.equals(tm));
		assertTrue(ts.equals(FileTwitterStatusList.read(ascii,"UTF-8").get(0)));
		twitterfile.delete();
	}

	private File fileFromeStream(InputStream stream) throws IOException {
		File f = File.createTempFile("broken_raw", ".txt");
		PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(f)));
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = null;
		while((line = reader.readLine()) != null){writer.println(line);}
		writer.flush(); writer.close();
		return f;
	}
}
