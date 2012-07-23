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
package org.openimaj.hadoop.tools.globalfeature;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocalFileSystem;
import org.apache.hadoop.fs.Path;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.tools.globalfeature.GlobalFeatureExtractor;
import org.openimaj.tools.globalfeature.GlobalFeatureType;


/**
 * Options for the Hadoop version of the GlobalFeaturesTool
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class HadoopGlobalFeaturesOptions {
	private String[] args;
	
	@Option(name="--feature-type", aliases="-f", handler=ProxyOptionHandler.class, usage="Feature type", required=true)
	protected GlobalFeatureType feature;
	protected GlobalFeatureExtractor featureOp;
	
	@Option(name = "-input", aliases="-i", required=true, usage="Set the input path(s) or uri(s)")
	protected List<String> input;
	
	@Option(name = "-output", aliases="-o", required=true, usage="Set the output location")
	protected String output;
	
	@Option(name = "--binary", aliases="-b", required=false, usage="Set output mode to binary")
	protected boolean binary = false;
	
	@Option(name="--remove", aliases="-rm", required=false, usage="Remove the existing output location if it exists.", metaVar="BOOLEAN")
	private boolean replace = false;

	private boolean beforeMaps;
	
	/**
	 * Construct with the argument string.
	 * @param args the command line argumens
	 */
	public HadoopGlobalFeaturesOptions(String[] args)
	{
		this(args, false);
	}
	
	/**
	 * Construct with the argument string.
	 * @param args the command line argumens
	 * @param beforeMaps if true, then in the tool; false in a mapper.
	 */
	public HadoopGlobalFeaturesOptions(String[] args, boolean beforeMaps) {
		this.args = args;
		this.beforeMaps = beforeMaps;
		prepare();
	}
	
	private void prepare() {
		CmdLineParser parser = new CmdLineParser(this);

		try {
			parser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar HadoopGlobalFeaturesTool.jar [options...]");
			parser.printUsage(System.err);
			
			if (feature == null) {
				for (GlobalFeatureType m : GlobalFeatureType.values()) {
					System.err.println();
					System.err.println(m + " options: ");
					new CmdLineParser(m.getOptions()).printUsage(System.err);
				}
			}
			System.exit(1);
		}
	}

	protected void validate() {
		if(replace && beforeMaps) {
			try {
				URI outuri = SequenceFileUtility.convertToURI(output);
				FileSystem fs = getFileSystem(outuri);
				fs.delete(new Path(outuri.toString()), true);
			} catch (IOException e) {
				
			}
		}
	}
	
	static FileSystem getFileSystem(URI uri) throws IOException {
		Configuration config = new Configuration();
		FileSystem fs = FileSystem.get(uri, config);
		if (fs instanceof LocalFileSystem) fs = ((LocalFileSystem)fs).getRaw();
		return fs;
	}
}
