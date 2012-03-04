package org.openimaj.hadoop.tools.twitter;

import java.io.File;
import java.io.IOException;

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

	@Before
	public void setup() throws IOException{
		stemmedTweets = FileUtils.copyStreamToTemp(HadoopTwitterTokenToolTest.class.getResourceAsStream("/org/openimaj/twitter/json_tweets-stemmed.txt"), "stemmed", ".txt");
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

}
