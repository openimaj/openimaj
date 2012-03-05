package org.openimaj.hadoop.tools.twitter;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.io.FileUtils;

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
	@Before
	public void setup() throws IOException{
		stemmedTweets = FileUtils.copyStreamToTemp(HadoopTwitterTokenToolTest.class.getResourceAsStream("/org/openimaj/twitter/json_tweets-stemmed.txt"), "stemmed", ".txt");
		jsonTweets = FileUtils.copyStreamToTemp(HadoopTwitterTokenToolTest.class.getResourceAsStream(JSON_TWITTER),"tweets",".json");
		outputLocation = File.createTempFile("out", "counted");
		outputLocation.delete();
		resultsOutputLocation = File.createTempFile("out", "result");
		hadoopCommand = "-i %s -o %s -om CSV -ro %s -m %s -j %s -t 1";
	}
	
	@Test
	public void testDFIDF() throws Exception{
		String command = String.format(
				hadoopCommand,
				stemmedTweets.getAbsolutePath(),
				outputLocation.getAbsolutePath(),
//				resultsOutputLocation.getAbsolutePath(),
				"-",
				"DFIDF",
				"analysis.stemmed"
		);
		HadoopTwitterTokenTool.main(command.split(" "));
	}
	
	@Test
	public void testStemmingDFIDF() throws Exception{
		String command = String.format(
				hadoopCommand,
				jsonTweets.getAbsolutePath(),
				outputLocation.getAbsolutePath(),
//				resultsOutputLocation.getAbsolutePath(),
				"-",
				"DFIDF",
				"analysis.stemmed"
		);
		String[] args = command.split(" ");
		args = (String[]) ArrayUtils.addAll(args, new String[]{"-pp","-m PORTER_STEM"});
		HadoopTwitterTokenTool.main(args);
	}

}
