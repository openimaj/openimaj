package org.openimaj.hadoop.tools.twitter.token.outputmode;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.openimaj.tools.FileToolsUtil;
import org.openimaj.tools.InOutToolOptions;

/**
 * A twitter tweet token counting mode
 * 
 * @author ss
 *
 */
public enum TwitterTokenOutputModeOption implements CmdLineOptionsProvider{
	
	/**
	 * outputs a CSV file  
	 */
	CSV {

		@Override
		public TwitterTokenOutputMode mode() {
			return new CSVTokenOutputMode(dmode,emptyValue);
		}
		
	};

	@Override
	public Object getOptions() {
		return this;
	}
	enum DataMode{
		ROWMAJOR,COLMAJOR
	}
	/**
	 * the output if the stdout is to be used
	 */
	public static final String STDOUT = "-";
	
	@Option(name="--data-mode", aliases="-dm", required=false, usage="Should the output be row-major (one row per vector) or column-major (one row per feature)", metaVar="STRING")
	DataMode dmode = DataMode.COLMAJOR;
	
	@Option(name="--empty-value", aliases="-ev", required=false, usage="If a value is missing and output mode is dense (CSV etc.) what should empty valyes be?", metaVar="STRING")
	double emptyValue = 0l;
	
	@Option(name="--results-output", aliases="-ro", required=false, usage="Where should the results be outputted?", metaVar="STRING")
	String resultsOutput = STDOUT;
	
	@Option(name="--results-output-overwrite", aliases="-rorm", required=false, usage="Where should the results be outputted?", metaVar="STRING")
	boolean force = false;
	
	/**
	 * @return creates a mode instance
	 */
	public abstract TwitterTokenOutputMode mode();
	
	public Writer writer() throws IOException, CmdLineException{
		if(this.resultsOutput.equals(STDOUT)){
			return new PrintWriter(System.out);
		}
		else{
			File f = FileToolsUtil.validateLocalOutput(this.resultsOutput,force);
			return new FileWriter(f);
		}
	}
	
	
}
