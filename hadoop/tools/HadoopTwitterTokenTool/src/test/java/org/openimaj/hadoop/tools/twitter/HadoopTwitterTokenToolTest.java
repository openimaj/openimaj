package org.openimaj.hadoop.tools.twitter;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.mapred.JobHistory.Values;
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
	
	private String hadoopCommand;
	private File outputLocation;
	private File resultsOutputLocation;
	private File stemmedTweets;
	private File jsonTweets;
	private static final String JSON_TWITTER = "/org/openimaj/twitter/json_tweets.txt";
	/**
	 * load files prepare outputs
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException{
		org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.Values.Map.options = null;
		stemmedTweets = FileUtils.copyStreamToTemp(HadoopTwitterTokenToolTest.class.getResourceAsStream("/org/openimaj/twitter/json_tweets-stemmed.txt"), "stemmed", ".txt");
		jsonTweets = FileUtils.copyStreamToTemp(HadoopTwitterTokenToolTest.class.getResourceAsStream(JSON_TWITTER),"tweets",".json");
		outputLocation = File.createTempFile("out", "counted");
		outputLocation.delete();
		resultsOutputLocation = File.createTempFile("out", "result");
		resultsOutputLocation.delete();
		hadoopCommand = "-i %s -o %s -om %s -ro %s -m %s -j %s -t 1";
	}
	
	@Test
	public void testResumingIncompleteJob() throws Exception{
		String command = String.format(
				hadoopCommand,
				jsonTweets.getAbsolutePath(),
				outputLocation.getAbsolutePath(),
				"CSV",
				resultsOutputLocation.getAbsolutePath(),
				"DFIDF",
				"analysis.stemmed"
		);
		String[] args = command.split(" ");
		args = (String[]) ArrayUtils.addAll(args, new String[]{"-pp","-m PORTER_STEM"});
		HadoopTwitterTokenTool.main(args);
		// Now delete the part file from the output location and delete the results output location
		File[] deletedPartFiles = FileUtils.findRecursive(outputLocation,new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.contains("part")){
					System.out.println("Removing " + name);
					FileUtils.deleteRecursive(dir);
					return true;
				}
				return false;
			}
		});
		FileUtils.deleteRecursive(this.resultsOutputLocation);
		for (File file : deletedPartFiles) {
			assertTrue(!file.exists());
		}
		// Now run the command again
		HadoopTwitterTokenTool.main(args);
		// The part files that were deleted should now exist again
		for (File file : deletedPartFiles) {
			assertTrue(file.exists());
		}
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
				"CSV",
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
				"CSV",
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
	
	/**
	 * test DFIDF mode on a file with stemmed tweets and output some word statistics
	 * @throws Exception
	 */
	@Test
	public void testWordStatsDFIDF() throws Exception{
		String command = String.format(
				hadoopCommand,
				stemmedTweets.getAbsolutePath(),
				outputLocation.getAbsolutePath(),
				"WORD_STATS",
				resultsOutputLocation.getAbsolutePath(),
				"DFIDF",
				"analysis.stemmed"
		);
		HadoopTwitterTokenTool.main(command.split(" "));
		HashMap<String,IndependentPair<Long,Long>> wordLineCounts = WordIndex.readWordCountLines(resultsOutputLocation.getAbsolutePath());
		assertTrue(wordLineCounts.get(".").firstObject() == 12);
	}
	
	/**
	 * test DFIDF mode on a file with stemmed tweets and output some word statistics
	 * @throws Exception
	 */
	@Test
	public void testMultiFileInput() throws Exception{
		hadoopCommand = "-if %s -o %s -om %s -ro %s -m %s -j %s -t 1";
		File inputList  = File.createTempFile("inputs", ".txt");
		PrintWriter listWriter = new PrintWriter(new FileWriter(inputList));
		listWriter.println(jsonTweets.getAbsolutePath());
		listWriter.println(stemmedTweets.getAbsolutePath());
		listWriter.flush();
		listWriter.close();
		String command = String.format(
				hadoopCommand,
				inputList.getAbsolutePath(),
				outputLocation.getAbsolutePath(),
				"WORD_STATS",
				resultsOutputLocation.getAbsolutePath(),
				"DFIDF",
				"analysis.stemmed"
		);
		String[] args = command.split(" ");
		args = (String[]) ArrayUtils.addAll(args, new String[]{"-pp","-m PORTER_STEM"});
		HadoopTwitterTokenTool.main(args );
		HashMap<String,IndependentPair<Long,Long>> wordLineCounts = WordIndex.readWordCountLines(resultsOutputLocation.getAbsolutePath());
		assertTrue(wordLineCounts.get(".").firstObject() == 24);
	}
	
	/**
	 * test DFIDF mode on a file with stemmed tweets and output some word statistics
	 * @throws Exception
	 */
	@Test
	public void testMultiFileInputOnlyOutput() throws Exception{
		// Produce one set of processes (no output)
		String firstOuputLocation = this.outputLocation.getAbsolutePath() + "-first";
		String secondOuputLocation = this.outputLocation.getAbsolutePath() + "-second";
		String firstResultsOut = resultsOutputLocation.getAbsolutePath() + "-first";
		String secondResultsOut = resultsOutputLocation.getAbsolutePath() + "-second";
		String commandFirst = String.format(
				hadoopCommand,
				stemmedTweets.getAbsolutePath(),
				firstOuputLocation,
				"WORD_STATS",
				firstResultsOut,
				"DFIDF",
				"analysis.stemmed"
		);
		HadoopTwitterTokenTool.main(commandFirst.split(" "));
		String commandSecond = String.format(
				hadoopCommand,
				stemmedTweets.getAbsolutePath(),
				secondOuputLocation,
				"WORD_STATS",
				secondResultsOut,
				"DFIDF",
				"analysis.stemmed"
		);
		HadoopTwitterTokenTool.main(commandSecond.split(" "));
		hadoopCommand = "-if %s -om %s -ro %s";
		File inputList  = File.createTempFile("inputs", ".txt");
		PrintWriter listWriter = new PrintWriter(new FileWriter(inputList));
		listWriter.println(firstOuputLocation );
		listWriter.println(secondOuputLocation );
		listWriter.flush();
		listWriter.close();
		String command = String.format(
				hadoopCommand,
				inputList.getAbsolutePath(),
				"WORD_STATS",
				resultsOutputLocation.getAbsolutePath()
		);
		HadoopTwitterTokenTool.main(command.split(" "));
		HashMap<String,IndependentPair<Long,Long>> wordLineCounts = WordIndex.readWordCountLines(resultsOutputLocation.getAbsolutePath());
		assertTrue(wordLineCounts.get(".").firstObject() == 24);
	}

}
