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
package org.openimaj.hadoop.tools.twitter.token.mode;


import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.token.mode.dfidf.DFIDFTokenMode;
import org.openimaj.hadoop.tools.twitter.token.mode.match.TokenMatchMode;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.PairwiseMutualInformationMode;

/**
 * A twitter tweet token counting mode
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public enum TwitterTokenModeOption implements CmdLineOptionsProvider {
	/**
	 * Calculates DF-IDF for each term as described by: "Event Detection in Twitter" by J. Weng et. al. 
	 */
	DFIDF {
		@Override
		public TwitterTokenMode getOptions() {
			return new DFIDFTokenMode();
		}
	},
	/**
	 * Calculates DF-IDF for each term as described by: "Event Detection in Twitter" by J. Weng et. al. 
	 */
	PAIRMI{
		@Override
		public TwitterTokenMode getOptions() {
			return new PairwiseMutualInformationMode();
		}
	},
	/**
	 * In the JSONPath requested locate tweets with terms which contain any of the tokens requested. The tokens may be regex
	 */
	MATCH_TERM {
		@Override
		public TwitterTokenMode getOptions() {
			return new TokenMatchMode();
		}
	},
	/**
	 * Skip the actual processing, assume the input contains the data needed by the output
	 */
	JUST_OUTPUT {
		@Override
		public TwitterTokenMode getOptions()  {
			return new TwitterTokenMode(){
				private String[] finalOutput;

				@Override
				public void perform(HadoopTwitterTokenToolOptions opts) throws Exception {
					this.finalOutput = opts.getAllInputs();
				}

				@Override
				public String[] finalOutput(HadoopTwitterTokenToolOptions opts) throws Exception {
					return finalOutput;
				}				
			};
		}
	}
	;
	
	@Override
	public abstract TwitterTokenMode getOptions();
}
