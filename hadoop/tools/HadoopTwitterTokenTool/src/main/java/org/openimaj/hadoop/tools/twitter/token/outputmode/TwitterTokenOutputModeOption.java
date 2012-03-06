package org.openimaj.hadoop.tools.twitter.token.outputmode;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.SparseCSVTokenOutputMode;
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
			return new SparseCSVTokenOutputMode();
		}
		
	};

	@Override
	public Object getOptions() {
		return this;
	}
	
	@Option(name="--results-output", aliases="-ro", required=true, usage="Where should the results be outputted?", metaVar="STRING")
	String resultsOutput;
	
	@Option(name="--results-output-overwrite", aliases="-rorm", required=false, usage="Where should the results be outputted?", metaVar="STRING")
	boolean force = false;
	
	/**
	 * @return creates a mode instance
	 */
	public abstract TwitterTokenOutputMode mode();

	/**
	 * @param hadoopTwitterTokenToolOptions the options of the tool 
	 * @param mode the mode which completed and needs outputting
	 * @throws Exception 
	 */
	public void write(HadoopTwitterTokenToolOptions hadoopTwitterTokenToolOptions,TwitterTokenMode mode) throws Exception {
		TwitterTokenOutputMode outmode = mode();
		outmode.write(hadoopTwitterTokenToolOptions, mode, resultsOutput, force);
	}
}
