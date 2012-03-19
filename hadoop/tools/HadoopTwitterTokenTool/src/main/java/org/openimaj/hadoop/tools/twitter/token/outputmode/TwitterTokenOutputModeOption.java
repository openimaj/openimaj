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
import org.openimaj.hadoop.tools.twitter.token.outputmode.correlation.CorrelationOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.jacard.JacardIndexOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.SparseCSVTokenOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.stats.StatsOutputMode;
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
		public TwitterTokenOutputMode getOptions() {
			return new SparseCSVTokenOutputMode();
		}
		
	},
	/**
	 * outputs the jacard index at each time step, a measure for how similar the sets of words are between two timesteps
	 */
	JACARD_INDEX {

		@Override
		public TwitterTokenOutputMode getOptions() {
			return new JacardIndexOutputMode();
		}
		
	},
	/**
	 * 
	 */
	CORRELATION{

		@Override
		public TwitterTokenOutputMode getOptions() {
			return new CorrelationOutputMode();
		}
		
	},
	/**
	 * Output some statistics about the words
	 */
	WORD_STATS{

		@Override
		public TwitterTokenOutputMode getOptions() {
			return new StatsOutputMode();
		}
		
	};

	@Override
	public abstract TwitterTokenOutputMode getOptions();
}
