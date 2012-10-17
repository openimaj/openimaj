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
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PairwiseMutualInformationTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	private String hadoopCommand;

	private File jsonTweets;
	private File outputLocation;

	private File jsonTweetsMonth;
	private static final String JSON_TWITTER = "/org/openimaj/twitter/json_tweets.txt";
	private static final String JSON_TWITTER_MONTH = "/org/openimaj/twitter/sample-2010-10.json";
	/**
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		hadoopCommand = "-i %s -o %s -m %s -j %s";
		jsonTweets = FileUtils.copyStreamToFile(HadoopTwitterTokenToolTest.class.getResourceAsStream(JSON_TWITTER),folder.newFile("tweets.json"));
		jsonTweetsMonth = FileUtils.copyStreamToFile(HadoopTwitterTokenToolTest.class.getResourceAsStream(JSON_TWITTER_MONTH),folder.newFile("tweets.json"));
		outputLocation = folder.newFile("out.counted");
		outputLocation.delete();
	}
	
	/**
	 * @throws Exception
	 */
	@Test
	public void testPointwiseMutualInformationSingleTime() throws Exception {
		String cmd = String.format(hadoopCommand,jsonTweets.getAbsolutePath(),outputLocation.getAbsolutePath(),"PAIRMI","analysis.tokens.all");
		cmd += " -minpc 10";
		String[] args = cmd.split(" ");
		args = (String[]) ArrayUtils.addAll(args, new String[]{"-pp","-m TOKENISE"});
		HadoopTwitterTokenTool.main(args);
		BufferedReader reader = PairwiseMutualInformationMode.sortedPMIReader(outputLocation);
		for (int i = 0; i < 10; i++) {
			System.out.println(reader.readLine());
		}
	}
	
	@Test
	public void testPointwiseMutualInformationMultipleTimes() throws Exception {
		String cmd = String.format(hadoopCommand,jsonTweetsMonth.getAbsolutePath(),outputLocation.getAbsolutePath(),"PAIRMI","analysis.tokens.all");
		cmd += " -minpc 10 -t 60";
		String[] args = cmd.split(" ");
		args = (String[]) ArrayUtils.addAll(args, new String[]{"-pp","-m TOKENISE"});
		HadoopTwitterTokenTool.main(args);
		BufferedReader reader = PairwiseMutualInformationMode.sortedPMIReader(outputLocation);
		for (int i = 0; i < 10; i++) {
			System.out.println(reader.readLine());
		}
	}
}
