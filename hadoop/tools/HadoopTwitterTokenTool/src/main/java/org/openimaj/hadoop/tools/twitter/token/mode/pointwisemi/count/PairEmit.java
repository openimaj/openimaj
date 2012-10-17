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
package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.io.IOUtils;
import org.openimaj.twitter.USMFStatus;

/**
 * For each pair of tokens in a given document emit a count. Also defines a combiner
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PairEmit extends Mapper<LongWritable, Text, BytesWritable, BytesWritable> {
	
	/**
	 * The string which splits times and places. Constructed to be unlikely to be an actual token (words and punctuation)
	 */
	private static final long DEFAULT_TIME = -1;
	private static HadoopTwitterTokenToolOptions options;
	private static long timeDeltaMillis = DEFAULT_TIME;
	Logger logger = Logger.getLogger(PairEmit.class);

	protected static synchronized void loadOptions(Mapper<LongWritable, Text, BytesWritable, BytesWritable>.Context context) throws IOException {
		if (options == null) {
			try {
				options = new HadoopTwitterTokenToolOptions(context.getConfiguration().getStrings(HadoopTwitterTokenToolOptions.ARGS_KEY));
				options.prepare();
				timeDeltaMillis = context.getConfiguration().getLong(PairMutualInformation.TIMEDELTA, DEFAULT_TIME) * 60 * 1000;
				
			} catch (CmdLineException e) {
				throw new IOException(e);
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	protected void setup(Mapper<LongWritable, Text, BytesWritable, BytesWritable>.Context context) throws IOException, InterruptedException {
		loadOptions(context);
	}
	
	@Override
	protected void map(LongWritable key, Text value, Mapper<LongWritable,Text,BytesWritable,BytesWritable>.Context context) throws IOException ,InterruptedException {
		List<String> tokens = null;
		DateTime time = null;
		try {
			USMFStatus status = options.readStatus(value.toString());
			time = status.createdAt();
			if(time == null) return;
			tokens = options.readStatusPart(value.toString());
		} catch (Exception e) {
			return;
		}
		long timeIndex = DEFAULT_TIME;
		if(timeDeltaMillis > 0)
			timeIndex = (time.getMillis() / timeDeltaMillis) * timeDeltaMillis;
		for (int i = 0; i < tokens.size(); i++) 
		{
			String tok1 = tokens.get(i);
			String tok2;
			for (int j = i+1; j < tokens.size(); j++) {
				tok2 = tokens.get(j);
				TokenPairCount tpc = null;
				int cmp = tok1.compareTo(tok2);
				if(cmp > 0){
					tpc = new TokenPairCount(tok2, tok1);
				}
				else{
					tpc = new TokenPairCount(tok1, tok2);
				}
				tpc.paircount = 1;
				BytesWritable keywrite = new BytesWritable(tpc.identifierBinary(timeIndex));
				context.write(keywrite, new BytesWritable(IOUtils.serialize(tpc)));
				context.getCounter(PairEnum.PAIR).increment(1);
			}
			TokenPairCount tpc = new TokenPairCount(tok1);
			tpc.paircount = tokens.size() - 1;
			context.write(new BytesWritable(tpc.identifierBinary(timeIndex)), new BytesWritable(IOUtils.serialize(tpc)));
			context.getCounter(PairEnum.UNARY).increment(1);
		}
		
	};
}
