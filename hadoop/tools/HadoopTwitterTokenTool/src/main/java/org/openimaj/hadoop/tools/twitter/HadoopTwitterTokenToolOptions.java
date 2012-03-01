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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenModeOption;
import org.openimaj.tools.InOutToolOptions;

/**
 * Hadoop specific options for twitter preprocessing
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class HadoopTwitterTokenToolOptions extends InOutToolOptions{
	@Option(name="--mode", aliases="-m", required=true, usage="How should the tweet tokens should be counted and processed.", handler=ProxyOptionHandler.class, multiValued=true)
	List<TwitterTokenModeOption> modeOptions = new ArrayList<TwitterTokenModeOption>();
	
	@Option(name="--json-path", aliases="-j", required=true, usage="A JSONPath query defining the field to find tokens to count", metaVar="STRING")
	String tokensJSONPath;

	private String[] args;
	
	private boolean  beforeMaps;
	
	public HadoopTwitterTokenToolOptions(String[] args,boolean beforeMaps) {
		this.args = args;
		this.beforeMaps = beforeMaps;
		this.prepare();
	}
	
	public HadoopTwitterTokenToolOptions(String[] args) {
		this(args,false);
	}
	
	/**
	 * prepare the tool for running
	 */
	public void prepare(){
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			// TODO: Fix this hack
			Set<TwitterTokenModeOption> modes = new HashSet<TwitterTokenModeOption>();
			for (TwitterTokenModeOption mode : modeOptions) {
				modes.add(mode);
			}
			modeOptions.clear();
			modeOptions.addAll(modes);
//			System.out.println(Arrays.toString(args));
			System.out.println("Number of mode options: " + modeOptions.size());
			this.validate();
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar JClusterQuantiser.jar [options...] [files...]");
			parser.printUsage(System.err);
			System.exit(1);
		}
		
	}

	private void validate() throws CmdLineException {
		if(this.beforeMaps)
		{
			HadoopToolsUtil.validateInput(this);
			HadoopToolsUtil.validateOutput(this);
		}
	}
}
