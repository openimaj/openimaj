package org.openimaj.tools.twitter.options;

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

public class TwitterPreprocessingToolOptions extends  AbstractTwitterPreprocessingToolOptions{
	
	File inputFile;
	File outputFile;
	private PrintWriter outWriter = null;
	
	public TwitterPreprocessingToolOptions(String[] args) {
		super(args);
	}

	@Override
	public boolean validate() throws CmdLineException {
		inputFile = new File(input);
		if(!inputFile.exists()) throw new CmdLineException(null,"Couldn't Find Input File");
		outputFile = new File(output);
		if(outputFile.exists()){
			if(force){
				if(!outputFile.delete()) throw new CmdLineException(null,"Output file exists, could not remove");
			}
			else throw new CmdLineException(null,"Output file exists, not removing");
		}
		return true;
	}

	public TwitterStatusList getTwitterStatusList() throws IOException {
		return FileTwitterStatusList.read(this.inputFile);
	}

	public PrintWriter outputWriter() throws UnsupportedEncodingException, FileNotFoundException {
		if(this.outWriter == null) this.outWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.outputFile),this.encoding));
		return this.outWriter;
	}
	

}
