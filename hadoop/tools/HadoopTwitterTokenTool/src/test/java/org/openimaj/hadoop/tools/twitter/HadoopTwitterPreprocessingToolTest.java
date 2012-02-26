/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
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
package org.openimaj.hadoop.tools.twitter;

import static org.junit.Assert.assertTrue;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openimaj.data.RandomData;
import org.openimaj.io.FileUtils;
import org.openimaj.tools.twitter.modes.preprocessing.TwitterPreprocessingMode;
import org.openimaj.twitter.TwitterStatus;
import org.openimaj.twitter.collection.FileTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusList;




/**
 * Test some key functionality of the twitter preprocessing tool over hadoop
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class HadoopTwitterPreprocessingToolTest {
	private static final String JSON_TWITTER = "/org/openimaj/twitter/json_tweets.txt";
	private static final String RAW_TWITTER = "/org/openimaj/twitter/tweets.txt";
	private static final String BROKEN_RAW_TWITTER = "/org/openimaj/twitter/broken_raw_tweets.txt";
	private File jsonTwitterInputFile;
	private File rawTwitterInputFile;
	private String commandFormat;
	private File brokenRawTwitterInputFile;
	private String modeFormat;
	/**
	 * Prepare all input files
	 * @throws IOException 
	 */
	@Before
	public void setup() throws IOException{
		jsonTwitterInputFile = fileFromStream(HadoopTwitterPreprocessingToolTest.class.getResourceAsStream(JSON_TWITTER));
		rawTwitterInputFile = fileFromStream(HadoopTwitterPreprocessingToolTest.class.getResourceAsStream(RAW_TWITTER));
		brokenRawTwitterInputFile = fileFromStream(HadoopTwitterPreprocessingToolTest.class.getResourceAsStream(BROKEN_RAW_TWITTER));
		
		commandFormat = "-i %s -o %s %s -om %s -rm -v";
		modeFormat = "-m %s";
	}
	
	private File fileFromStream(InputStream stream) throws IOException {
		File f = File.createTempFile("tweet", ".txt");
		PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(f)));
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		String line = null;
		while((line = reader.readLine()) != null){writer.println(line);}
		writer.flush(); writer.close();
		return f;
	}
	
}
