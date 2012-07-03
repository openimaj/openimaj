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
package org.openimaj.hadoop.tools.downloader;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.tools.downloader.InputMode.Parser;

/**
 * Command-line options for the downloader tool
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HadoopDownloaderOptions {
	private String[] args;

	@Option(name="--input", aliases="-i", required=true, usage="Input file or URL.", metaVar="STRING")
	private String input;
	
	@Option(name="--output", aliases="-o", required=true, usage="Output file or URL.", metaVar="STRING")
	private String output;
	
	@Option(name="--remove", aliases="-rm", required=false, usage="Remove the existing output location if it exists.", metaVar="BOOLEAN")
	private boolean replace = false;
	
	@Option(name="--num-reducers", aliases="-nr", required=false, usage="Number of reducers. Controls the number of sequencefile parts created.")
	private int nreducers = 0;
	
	@SuppressWarnings("unused")
	@Option(name="--input-mode", aliases="-m", required=false, usage="How should the URLs be processed to be downloaded.", handler=ProxyOptionHandler.class)
	private InputMode inputMode = InputMode.PLAIN;
	private Parser inputModeOp;
	
	@Option(name="--sleep", aliases="-s", required=false, usage="Time in milliseconds to sleep after downloading a file.", metaVar="LONG")
	private long sleep = 0;
	
	/**
	 * Construct with the given arguments
	 * @param args the arguments
	 */
	public HadoopDownloaderOptions(String[] args) {
		this.args = args;
	}

	/**
	 * Prepare the options
	 * @param initial true if initial setup is being performed; false if inside the mapper
	 */
	public void prepare(boolean initial) {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument( args );
			this.validate( initial );
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: hadoop -jar HadoopImageDownloader [options...] [files...]");
			parser.printUsage(System.err);
			
			System.exit(1);
		}
	}

	private void validate(boolean initial) {
		if(replace && initial) {
			try {
				URI outuri = SequenceFileUtility.convertToURI(output);
				
				FileSystem fs = SequenceFileUtility.getFileSystem(outuri, new Configuration());
				
				fs.delete(new Path(outuri.toString()), true);
			} catch (IOException e) {
				
			}
		}
	}
	
	/**
	 * Get the input file(s) containing the URLs
	 * @return the input paths
	 * @throws IOException
	 */
	public Path[] getInputPaths() throws IOException {
		return SequenceFileUtility.getFilePaths(input, "part");
	}

	/**
	 * @return the output file location
	 */
	public Path getOutputPath() {
		return new Path(SequenceFileUtility.convertToURI(output).toString());
	}
	
	/**
	 * @return the number of reducers
	 */
	public int getNumberOfReducers() {
		return this.nreducers;
	}
		
	/**
	 * @return the {@link Parser} corresponding to the
	 * selected mode.
	 */
	public Parser getInputParser() {
		return inputModeOp;
	}
	
	/**
	 * @return the time in milliseconds to sleep after downloading a file
	 */
	public long getSleep() {
		return sleep;
	}
}
