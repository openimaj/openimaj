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
package org.openimaj.hadoop.tools.download;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.tools.download.URLConstructionMode.URLConstructionModeOp;



public class HadoopImageDownloadOptions {
	private static final String EXTRA_USAGE_INFO = "";
	private String[] args;
	
	@Option(name="--remove", aliases="-rm", required=false, usage="Remove the existing output location if it exists.", metaVar="BOOLEAN")
	private boolean replace = false;
	
	
	@Option(name="--n-reducers", aliases="-nr", required=false, usage="Number of reducers.", metaVar="INTEGER")
	private int nreducers = 100;
	
	@Option(name = "--threads", aliases = "-j", required = false, usage = "Use NUMBER threads for quantization.", metaVar = "NUMBER")
	private int concurrency = Runtime.getRuntime().availableProcessors();
	
	@SuppressWarnings("unused")
	@Option(name="--url-construction-mode", aliases="-u", required=false, usage="How should the URLs be processed to be downloaded.", metaVar="INTEGER", handler=ProxyOptionHandler.class)
	private URLConstructionMode urlConstructionMode = URLConstructionMode.IMAGE_NET;
	private URLConstructionModeOp urlConstructionModeOp;
	
	@Option(name="--forced-map-wait", aliases="-fmw", required=false, usage="Force the map jobs to wait a while before they finish, artificially increase the waiting time to be kind to a website.", metaVar="LONG")
	private long forcedMapWait = 0;
	private boolean beforeMap;
	
	public HadoopImageDownloadOptions(String[] args)
	{
		this(args,false);
	}
	public HadoopImageDownloadOptions(String[] args, boolean beforeMap) {
		this.args = args;
		this.beforeMap = beforeMap;
	}

	public void prepare() {
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			this.validate();
			this.urlConstructionModeOp.setup();
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar JClusterQuantiser.jar [options...] [files...]");
			parser.printUsage(System.err);
			System.err.print(HadoopImageDownloadOptions.EXTRA_USAGE_INFO);
			
			System.exit(1);
		}
		
	}

	private void validate() {
		if(replace && beforeMap){
			try {
				URI outuri = SequenceFileUtility.convertToURI(this.getOutputString());
				FileSystem fs = getFileSystem(outuri);
				fs.delete(new Path(outuri.toString()), true);
			} catch (IOException e) {
				
			}
		}
	}

	public static FileSystem getFileSystem(URI uri) throws IOException {
		Configuration config = new Configuration();
		FileSystem fs = FileSystem.get(uri, config);
		if (fs instanceof LocalFileSystem) fs = ((LocalFileSystem)fs).getRaw();
		return fs;
	}

	@Option(name="--input", aliases="-i", required=true, usage="Input image File or URL.", metaVar="STRING")
	private String input;
	
	@Option(name="--output", aliases="-o", required=true, usage="Output keypoint File or URL.", metaVar="STRING")
	private String output;
	
	
	public String getInputString() {
		return input;
	}

	public String getOutputString() {
		return output;
	}
	
	public Path[] getInputPaths() throws IOException {
		Path[] sequenceFiles = SequenceFileUtility.getFilePaths(this.getInputString(), "part");
		return sequenceFiles;
	}

	public Path getOutputPath() {
		return new Path(SequenceFileUtility.convertToURI(this.getOutputString()).toString());
	}
	public int getNumberOfReducers() {
		return this.nreducers;
	}
	public void setUrlConstructionMode(URLConstructionMode urlConstructionMode) {
		this.urlConstructionMode = urlConstructionMode;
	}
	public URLConstructionModeOp getUrlConstructionMode() {
		return urlConstructionModeOp;
	}
	public long getForcedMapWait() {
		return forcedMapWait;
	}
	public void setConcurrency(int concurrency) {
		this.concurrency = concurrency;
	}
	public int getConcurrency() {
		return concurrency;
	}

}
