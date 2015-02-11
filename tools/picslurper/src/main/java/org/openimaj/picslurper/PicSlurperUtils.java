package org.openimaj.picslurper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.openimaj.io.IOUtils;
import twitter4j.Status;

import com.google.gson.Gson;

/**
 * Functions for writing various parts of PicSlurper
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PicSlurperUtils {
	private static String TWEET_FILE_NAME = "tweets.json";
	/**
	 * Update a specific file with statistics of URLs being consumed
	 *
	 * @param statsFile
	 * @param statusConsumption
	 * @throws IOException
	 */
	public static synchronized void updateStats(File statsFile, StatusConsumption statusConsumption) throws IOException {
		updateStats(statsFile, statusConsumption, false);
	}

	/**
	 * Update a specific file with statistics of URLs being consumed
	 *
	 * @param statsFile
	 * @param statusConsumption
	 * @param forgetImageURLs whether the actual image urls collected should be saved. On the global stats file this should be true
	 * @throws IOException
	 */
	public static synchronized void updateStats(File statsFile, StatusConsumption statusConsumption, boolean forgetImageURLs) throws IOException {
		StatusConsumption current = new StatusConsumption();
		if (statsFile.exists())
			current = IOUtils.read(statsFile, current);
		current.incr(statusConsumption);
		if(forgetImageURLs){
			// emtpy the imageURLs before you save
			current.imageURLs.clear();
		}
		IOUtils.writeASCII(statsFile, current); // initialise the output file
	}

	private static transient Gson gson = new Gson();

	/**
	 * Updated a tweets.json file in the specified location with the given
	 * {@link ReadableWritableJSON} instance
	 *
	 * @param outRoot
	 * @param status
	 * @throws IOException
	 */
	public static synchronized void updateTweets(File outRoot, Status status) throws IOException {
		if(status==null)return;
		File outFile = new File(outRoot, TWEET_FILE_NAME);
		FileOutputStream fstream = new FileOutputStream(outFile, true);
		PrintWriter pwriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fstream, "UTF-8")));
		pwriter.println(gson.toJson(status));
		pwriter.println();
		pwriter.flush();
		pwriter.close();
	}
}
