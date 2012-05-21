package org.openimaj.hadoop.tools.twitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.PairwiseMutualInformationMode;
import org.openimaj.io.FileUtils;

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class PairwiseMutualInformationTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	private String hadoopCommand;

	private File jsonTweets;
	private File outputLocation;
	private static final String JSON_TWITTER = "/org/openimaj/twitter/json_tweets.txt";
	/**
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		hadoopCommand = "-i %s -o %s -m %s -j %s";
		jsonTweets = FileUtils.copyStreamToFile(HadoopTwitterTokenToolTest.class.getResourceAsStream(JSON_TWITTER),folder.newFile("tweets.json"));
		outputLocation = folder.newFile("out.counted");
		outputLocation.delete();
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testPairwiseMutualInformation() throws Exception {
		String cmd = String.format(hadoopCommand,jsonTweets.getAbsolutePath(),outputLocation.getAbsolutePath(),"PAIRMI","analysis.tokens.all");
		String[] args = cmd.split(" ");
		args = (String[]) ArrayUtils.addAll(args, new String[]{"-pp","-m TOKENISE"});
		HadoopTwitterTokenTool.main(args);
		BufferedReader reader = PairwiseMutualInformationMode.sortedPMIReader(outputLocation);
		for (int i = 0; i < 10; i++) {
			System.out.println(reader.readLine());
		}
	}
}
