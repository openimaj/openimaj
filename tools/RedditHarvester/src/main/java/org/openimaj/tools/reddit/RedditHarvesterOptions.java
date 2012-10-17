/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.tools.reddit;

import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.tools.FileToolsUtil;
import org.openimaj.tools.InOutToolOptions;

/**
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class RedditHarvesterOptions extends InOutToolOptions{
	
	/**
	 * the root reddit url  
	 */
	public static final String REDDIT_BASE_URL = "http://www.reddit.com";
	
	@Option(name="--split-mode", aliases="-sm", required=false, usage="How to split the reddit inputs.", handler=ProxyOptionHandler.class)
	SplitModeOption mode = SplitModeOption.TIME;
	SplitMode modeOp = mode.getOptions();
	
	private String[] args;
	/**
	 * @param args the arguments, prepared using the prepare method
	 * @param prepare whether prepare should be called now or later
	 */
	public RedditHarvesterOptions(String[] args, boolean prepare) {
		this.args = args;
		if(prepare) this.prepare();
	}
	
	/**
	 * @param args the arguments, prepared using the prepare method
	 */
	public RedditHarvesterOptions(String[] args) {
		this(args,true);
	}
	
	/**
	 * prepare the tool for running
	 */
	public void prepare(){
		CmdLineParser parser = new CmdLineParser(this);
		try {
			parser.parseArgument(args);
			this.validate();
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: java -jar RedditHarvester.jar [options...] [files...]");
			parser.printUsage(System.err);
			System.err.println(this.getExtractUsageInfo());
			System.exit(1);
		}
		
	}

	private String getExtractUsageInfo() {
		return "";
	}

	private void validate() throws CmdLineException {
		if(this.getInput() == null || this.getInput() == ""){
			this.setInput("/all.json");
		}
		else if(!this.getInput().endsWith(".json")){
			this.setInput(this.getInput()+".json");
		}
		this.setOutput(this.getOutput() + File.separator + this.getInput());
		try {
			File fileOut = FileToolsUtil.validateLocalOutput(this);
			fileOut.mkdirs();
			
		} catch (IOException e) {
			throw new CmdLineException(null,e.getMessage());
		}
		
	}
}
