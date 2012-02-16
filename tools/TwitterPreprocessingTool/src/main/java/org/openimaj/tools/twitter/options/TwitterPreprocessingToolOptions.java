package org.openimaj.tools.twitter.options;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.kohsuke.args4j.CmdLineException;
import org.openimaj.twitter.collection.FileTwitterStatusList;
import org.openimaj.twitter.collection.TwitterStatusList;

/**
 * The single processing command line version of the twitter tool
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 */
public class TwitterPreprocessingToolOptions extends  AbstractTwitterPreprocessingToolOptions{
	
	File inputFile;
	File outputFile;
	private PrintWriter outWriter = null;
	private boolean stdout;
	
	/**
	 * See: {@link AbstractTwitterPreprocessingToolOptions#AbstractTwitterPreprocessingToolOptions(String[])}
	 * @param args 
	 */
	public TwitterPreprocessingToolOptions(String[] args) {
		super(args);
	}

	@Override
	public boolean validate() throws CmdLineException {
		inputFile = new File(input);
		if(!inputFile.exists()) throw new CmdLineException(null,"Couldn't Find Input File");
		if(output.equals("-")){
			this.stdout = true;
		}
		else{
			outputFile = new File(output);
			if(outputFile.exists()){
				if(force){
					if(!outputFile.delete()) throw new CmdLineException(null,"Output file exists, could not remove");
				}
				else throw new CmdLineException(null,"Output file exists, not removing");
			}
		}
		return true;
	}

	/**
	 * @return the list of tweets from the input file
	 * @throws IOException
	 */
	public TwitterStatusList getTwitterStatusList() throws IOException {
		if(this.nTweets == -1){
			return FileTwitterStatusList.read(this.inputFile,this.encoding);
		}
		else{
			return FileTwitterStatusList.read(this.inputFile,this.encoding,this.nTweets);
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
				this.outWriter = new PrintWriter(System.out);
			}else{
				this.outWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.outputFile),this.encoding)),true);
			}
		}
			
		return this.outWriter;
	}
}
