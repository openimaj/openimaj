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
package org.openimaj.hadoop.tools.twitter.token.outputmode;


import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.TwitterTokenMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.correlation.CorrelationOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.jacard.JacardIndexOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.SparseCSVTokenOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.stats.StatsOutputMode;
import org.openimaj.hadoop.tools.twitter.token.outputmode.timeseries.SpecificWordTimeSeries;

/**
 * A twitter tweet token counting mode
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
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
	 * outputs a SPECIFIC_WORDfile  
	 */
	SPECIFIC_WORD {

		@Override
		public TwitterTokenOutputMode getOptions() {
			return new SpecificWordTimeSeries();
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
		
	}, 
	/**
	 * don't do anything
	 */
	NONE {
		@Override
		public TwitterTokenOutputMode getOptions() {
			return new TwitterTokenOutputMode(){

				@Override
				public void write(HadoopTwitterTokenToolOptions opts,TwitterTokenMode completedMode) throws Exception {
					// do nothing
				}
				
			};
		}
	};

	@Override
	public abstract TwitterTokenOutputMode getOptions();
}
