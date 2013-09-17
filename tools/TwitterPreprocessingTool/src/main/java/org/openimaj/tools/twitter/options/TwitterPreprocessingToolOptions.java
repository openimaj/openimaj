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
package org.openimaj.tools.twitter.options;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.openimaj.tools.FileToolsUtil;
import org.openimaj.twitter.USMFStatus;
import org.openimaj.twitter.collection.FileTwitterStatusList;
import org.openimaj.twitter.collection.StreamTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusList;

/**
 * The single processing command line version of the twitter tool
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TwitterPreprocessingToolOptions extends  AbstractTwitterPreprocessingToolOptions{
	
	/**
	 * this is available mainly for testing
	 */
	public static InputStream sysin = System.in;
	List<File> inputFiles;
	File inputFile;
	File outputFile;
	private PrintWriter outWriter = null;
	private boolean stdout;
	private Iterator<File> fileIterator;
	private boolean stdin;
	
	/**
	 * See: {@link AbstractTwitterPreprocessingToolOptions#AbstractTwitterPreprocessingToolOptions(String[])}
	 * @param args 
	 */
	public TwitterPreprocessingToolOptions(String[] args) throws CmdLineException{
		super(args);
		
		if(!this.stdin) this.fileIterator = this.inputFiles.iterator();
	}

	@Override
	public boolean validate() throws CmdLineException{
		try{
			if(FileToolsUtil.isStdin(this)){
				this.stdin = true;
			}
			else{				
				this.inputFiles = FileToolsUtil.validateLocalInput(this);
			}
			if(FileToolsUtil.isStdout(this)){
				this.stdout = true;
			}
			else
			{
				this.outputFile = FileToolsUtil.validateLocalOutput(this);
			}
			return true;
		}
		catch(Exception e){
			throw new CmdLineException(null,e.getMessage());
		}
		
	}

	/**
	 * @return the list of tweets from the input file
	 * @throws IOException
	 */
	public TwitterStatusList<USMFStatus> getTwitterStatusList() throws IOException {
		if(this.stdin){
			this.stdin = false;
			if(this.nTweets == -1)
			{				
				return StreamTwitterStatusList.readUSMF(sysin, this.statusType.type(),this.encoding);
			}
			else{
				return StreamTwitterStatusList.readUSMF(sysin, this.nTweets,this.statusType.type(),this.encoding);
			}
		}
		else{
			if(this.nTweets == -1){
				return FileTwitterStatusList.readUSMF(this.inputFile, this.encoding,this.statusType.type());
			}
			else{
				return FileTwitterStatusList.readUSMF(this.inputFile, this.nTweets, this.encoding,this.statusType.type());
			}
		}
		
		
	}

	/**
	 * @return a print writer to the output file or stdout
	 * @throws UnsupportedEncodingException
	 * @throws FileNotFoundException
	 */
	public PrintWriter outputWriter() throws UnsupportedEncodingException, FileNotFoundException {
		if(this.outWriter == null){
			if(this.stdout){
				PrintStream sysout = new PrintStream(System.out, true, this.encoding);
				this.outWriter = new PrintWriter(sysout);
			}else{
				this.outWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFile),this.encoding)),true);
			}
		}
			
		return this.outWriter;
	}

	/**
	 * @return is there another file to analyse
	 */
	public boolean hasNextFile() {
		if(!this.stdin) {
			if(fileIterator == null) return false;
			return fileIterator.hasNext();
		}
		return true;
	}

	/**
	 * Prepare the next file
	 */
	public void nextFile() {
		if(this.stdin) return;
		if(fileIterator.hasNext())
			TwitterPreprocessingToolOptions.this.inputFile = fileIterator.next();
		else
			TwitterPreprocessingToolOptions.this.inputFile = null;
		
	}
}
