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

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.util.ToolRunner;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenModeOption;
import org.openimaj.hadoop.tools.twitter.token.outputmode.TwitterTokenOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.TwitterTokenOutputModeOption;
import org.openimaj.tools.InOutToolOptions;

/**
 * Hadoop specific options for twitter preprocessing
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class HadoopTwitterTokenToolOptions extends InOutToolOptions{
	@Option(name="--mode", aliases="-m", required=false, usage="How should the tweet tokens should be counted and processed.", handler=ProxyOptionHandler.class, multiValued=true)
	private List<TwitterTokenModeOption> modeOptions = new ArrayList<TwitterTokenModeOption>();
	List<TwitterTokenMode> modeOptionsOp = new ArrayList<TwitterTokenMode>();
	
	@SuppressWarnings("unused")
	@Option(name="--output-mode", aliases="-om", required=false, usage="How should tokens be outputted.", handler=ProxyOptionHandler.class)
	private TwitterTokenOutputModeOption outputModeOptions = TwitterTokenOutputModeOption.CSV;
	TwitterTokenOutputMode outputModeOptionsOp = TwitterTokenOutputModeOption.NONE.getOptions();
	
	@Option(name="--json-path", aliases="-j", required=false, usage="A JSONPath query defining the field to find tokens to count", metaVar="STRING")
	String tokensJSONPath = "analysis.stemmed";
	
	@Option(name="--time-delta", aliases="-t", required=false, usage="The length of a time window in minutes (defaults to 1 hour (60))", metaVar="STRING")
	private long timeDelta = 60;
	
	@Option(name="--preprocessing-tool", aliases="-pp", required=false, usage="Launch an initial stage where the preprocessing tool is used. The input and output values may be ignored", metaVar="STRING")
	private String preprocessingOptions = null;

	private String[] args;
	
	private boolean  beforeMaps;

	private String[] originalArgs;

	
	
	/**
	 * Initialise the options
	 * @param args the arguments after going through the hadoop tool (i.e. minus the -D hadoop arguments)
	 * @param originalArgs the original arguments as typed into the command line (useful for subhadoop tasks launched)
	 * @param beforeMaps whether this job is occuring before the maps
	 * @throws CmdLineException
	 */
	public HadoopTwitterTokenToolOptions(String[] args, String[] originalArgs, boolean beforeMaps) throws CmdLineException {
		this.args = args;
		this.originalArgs = originalArgs;
		this.beforeMaps = beforeMaps;
		if(this.beforeMaps)
			this.prepareCL();
		else
			this.prepare();
	}
	
	/**
	 * @param args Just the arguments (hadoop arguments assumed to be the same)
	 * @throws CmdLineException
	 */
	public HadoopTwitterTokenToolOptions(String[] args) throws CmdLineException {
		this(args,args,false);
	}
	
	/**
	 * prepare the tool for running (command line version)
	 */
	public void prepareCL(){
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			prepareMultivaluedArgument(modeOptions,TwitterTokenModeOption.JUST_OUTPUT);
			prepareMultivaluedArgument(modeOptionsOp,TwitterTokenModeOption.JUST_OUTPUT.getOptions());
			this.validate();
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar JClusterQuantiser.jar [options...] [files...]");
			parser.printUsage(System.err);
			System.exit(1);
		}
	}
	
	/**
	 * @throws CmdLineException
	 */
	public void prepare() throws CmdLineException{
		CmdLineParser parser = new CmdLineParser(this);
		parser.parseArgument(args);
		prepareMultivaluedArgument(modeOptions,TwitterTokenModeOption.JUST_OUTPUT);
		prepareMultivaluedArgument(modeOptionsOp,TwitterTokenModeOption.JUST_OUTPUT.getOptions());
//			System.out.println(Arrays.toString(args));
		System.out.println("Number of mode options: " + modeOptions.size());
		this.validate();
	}

	private void validate() throws CmdLineException {
		if(this.beforeMaps)
		{
			HadoopToolsUtil.validateInput(this);
			if(!noOutput())
				HadoopToolsUtil.validateOutput(this);
		}
	}

	/**
	 * @return is there any actual output this phase
	 */
	public boolean noOutput() {
		
		return 
			(		
					this.modeOptions.size() == 1 && 
					this.modeOptions.get(0) == TwitterTokenModeOption.JUST_OUTPUT
			);
	}

	/**
	 * @return the delta between time windows in minutes
	 */
	public long getTimeDelta() {
		return this.timeDelta;
	}

	/**
	 * @return the JSONPath query used to extract tokens
	 */
	public String getJsonPath() {
		return this.tokensJSONPath;
	}

	/**
	 * @return the original arguments including the hadoop arguments
	 */
	public String[] getArgs() {
		return this.originalArgs;
	}
	
	/**
	 * @return the arguments minus the hadoop arguments
	 */
	public String[] getNonHadoopArgs() {
		return this.args;
	}

	/**
	 * @param mode output a completed token mode
	 * @throws Exception
	 */
	public void output(TwitterTokenMode mode) throws Exception {
		this.outputModeOptionsOp.write(this,mode);
	}
	
	/**
	 * If there were any preprocessing arguments, perform the preprocessing and use the preprocessing output as the input
	 * to the rest of the process.
	 * @throws Exception
	 */
	public void performPreprocessing() throws Exception{
		if(noOutput()) return;
		if(this.preprocessingOptions==null)return;
		
		String output = this.getOutput() + "/preprocessing";
		boolean outExists = HadoopToolsUtil.fileExists(output);
		if(
			!outExists || // if the file doesn't exist
			SequenceFileUtility.getFilePaths(output, "part").length == 0 // or no part file was found
		){
			// if the file exists, the part file was not found, remove the file!
			if(outExists){
				HadoopToolsUtil.removeFile(output);
			}
			String inputPart = "";
			if(this.getInputFile() != null){
				inputPart = "-if " + this.getInputFile();
			}
			else{
				inputPart = "-i " + this.getInput();
			}
			this.preprocessingOptions = inputPart + " -o " + output + " " + preprocessingOptions;
			if(this.isForce())
				this.preprocessingOptions  += " -rm";
			String[] preprocessingArgs = this.preprocessingOptions.split(" ");
			ToolRunner.run(new HadoopTwitterPreprocessingTool(), preprocessingArgs);
		}
		else{
			System.out.println("Preprocessing exists, using...");
		}
		this.setInput(output);
		return;
		
	}
}
