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

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.log4j.Logger;
import org.openimaj.io.IOUtils;

/**
 * Assumes each key is a timeperiod split set of words ordered by single/pair words then by word order.
 * The key is only used to get time.
 * Using this time the values are combined and used to construct new keys
 * 
 * Emit for the given time a combined version of the word's count. 
 * The word might be a pair or a unary count.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PairEmitCombiner extends Reducer<BytesWritable, BytesWritable, BytesWritable, BytesWritable> {
	
	Logger logger = Logger.getLogger(PairEmitCombiner.class);
	
	@Override
	protected void reduce(BytesWritable timeword, Iterable<BytesWritable> paircounts, Reducer<BytesWritable,BytesWritable,BytesWritable,BytesWritable>.Context context) throws IOException ,InterruptedException {
		TokenPairCollector collector = new TokenPairCollector();
//		
		long time = TokenPairCount.timeFromBinaryIdentity(timeword.getBytes());
		
//		logger.info("Combining time: " + time);
		for (BytesWritable bytesWritable : paircounts) {
			TokenPairCount paircount = IOUtils.deserialize(bytesWritable.getBytes(), TokenPairCount.class);
			TokenPairCount collectorRet = collector.add(paircount);
			if(collectorRet != null){
				context.write(new BytesWritable(collectorRet.identifierBinary(time)), new BytesWritable(IOUtils.serialize(collectorRet)));
				if(!collectorRet.isSingle){
					context.getCounter(PairEnum.PAIR_COMBINED).increment(1);
				}else{
					context.getCounter(PairEnum.UNARY_COMBINED).increment(1);
				}
			}
		}
		// Final write
		TokenPairCount collectorRet = collector.getCurrent();
		context.write(new BytesWritable(collectorRet.identifierBinary(time)), new BytesWritable(IOUtils.serialize(collectorRet)));
		if(!collectorRet.isSingle){
			context.getCounter(PairEnum.PAIR_COMBINED).increment(1);
		}else{
			context.getCounter(PairEnum.UNARY_COMBINED).increment(1);
		}
	}
}
