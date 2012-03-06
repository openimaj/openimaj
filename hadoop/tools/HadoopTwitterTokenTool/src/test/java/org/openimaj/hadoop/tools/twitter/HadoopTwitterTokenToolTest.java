package org.openimaj.hadoop.tools.twitter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.TimeIndex;
import org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.WordIndex;
import org.openimaj.io.FileUtils;
import org.openimaj.util.pair.IndependentPair;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class HadoopTwitterTokenToolTest {
	
	private File stemmedTweets;
	private String hadoopCommand;
	private File outputLocation;
	private File resultsOutputLocation;
	private File jsonTweets;
	private static final String JSON_TWITTER = "/org/openimaj/twitter/json_tweets.txt";
	/**
	 * load files prepare outputs
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException{
		stemmedTweets = FileUtils.copyStreamToTemp(HadoopTwitterTokenToolTest.class.getResourceAsStream("/org/openimaj/twitter/json_tweets-stemmed.txt"), "stemmed", ".txt");
		jsonTweets = FileUtils.copyStreamToTemp(HadoopTwitterTokenToolTest.class.getResourceAsStream(JSON_TWITTER),"tweets",".json");
		outputLocation = File.createTempFile("out", "counted");
		outputLocation.delete();
		resultsOutputLocation = File.createTempFile("out", "result");
		resultsOutputLocation.delete();
		hadoopCommand = "-i %s -o %s -om CSV -ro %s -m %s -j %s -t 1";
	}
	
	/**
	 * test DFIDF mode on a file with stemmed tweets
	 * @throws Exception
	 */
	@Test
	public void testDFIDF() throws Exception{
		String command = String.format(
				hadoopCommand,
				stemmedTweets.getAbsolutePath(),
				outputLocation.getAbsolutePath(),
				resultsOutputLocation.getAbsolutePath(),
				"DFIDF",
				"analysis.stemmed"
		);
		HadoopTwitterTokenTool.main(command.split(" "));
		HashMap<String,IndependentPair<Long,Long>> wordLineCounts = WordIndex.readWordCountLines(resultsOutputLocation.getAbsolutePath());
		assertTrue(wordLineCounts.get(".").firstObject() == 12);
		HashMap<String,IndependentPair<Long,Long>> timeLineCounts = TimeIndex.readTimeCountLines(resultsOutputLocation.getAbsolutePath());
		long nenglish = 43;
		long sum = 0;
		for (IndependentPair<Long, Long> countLine: timeLineCounts.values()) {
			sum += countLine.firstObject(); 
		}
		assertTrue(sum == nenglish);
	}
	
	/**
	 * Test Stemming followed by DFIDF on a file which is plain json tweets
	 * @throws Exception
	 */
	@Test
	public void testStemmingDFIDF() throws Exception{
		String command = String.format(
				hadoopCommand,
				jsonTweets.getAbsolutePath(),
				outputLocation.getAbsolutePath(),
				resultsOutputLocation.getAbsolutePath(),
				"DFIDF",
				"analysis.stemmed"
		);
		String[] args = command.split(" ");
		args = (String[]) ArrayUtils.addAll(args, new String[]{"-pp","-m PORTER_STEM"});
		HadoopTwitterTokenTool.main(args);
		HashMap<String,IndependentPair<Long,Long>> wordLineCounts = WordIndex.readWordCountLines(resultsOutputLocation.getAbsolutePath());
		assertTrue(wordLineCounts.get(".").firstObject() == 12);
	}

}
