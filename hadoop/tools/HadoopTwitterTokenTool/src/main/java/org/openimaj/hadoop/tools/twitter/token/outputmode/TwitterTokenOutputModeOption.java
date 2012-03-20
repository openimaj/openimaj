package org.openimaj.hadoop.tools.twitter.token.outputmode;


import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.hadoop.tools.twitter.token.outputmode.jacard.JacardIndexOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.SparseCSVTokenOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.stats.StatsOutputMode;

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
//			return new CorrelationOutputMode();
			return null;
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
