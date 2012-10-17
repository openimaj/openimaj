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
package org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.sort;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.PairMutualInformation;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.TokenPairCount;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.TokenPairUnaryCount;
import org.openimaj.io.IOUtils;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PMISortMapper extends Mapper<BytesWritable, BytesWritable, BytesWritable, BytesWritable> {
	private static double minp = -1;
	private static int minPairCount;
	private static Path pairmiloc;
	private static Map<Long, Long> timecounts;
	/**
	 * does nothing
	 */
	public PMISortMapper(){
		
	}
	
	@Override
	public void setup(Mapper<BytesWritable, BytesWritable,BytesWritable,BytesWritable>.Context context) throws IOException ,InterruptedException {
		load(context);
	};
	private synchronized static void load(Mapper<BytesWritable, BytesWritable,BytesWritable,BytesWritable>.Context context) throws IOException {
		if(timecounts==null){			
			minp = context.getConfiguration().getFloat(PMIPairSort.MINP_KEY, -1);
			minPairCount = context.getConfiguration().getInt(PMIPairSort.MINPAIRCOUNT_KEY, 0);
			pairmiloc = new Path(context.getConfiguration().get(PMIPairSort.PAIRMI_LOC));
			timecounts = PairMutualInformation.loadTimeCounts(pairmiloc);
		}
	}

	@Override
	public void map(BytesWritable key, BytesWritable value, Mapper<BytesWritable, BytesWritable,BytesWritable,BytesWritable>.Context context) throws IOException ,InterruptedException {
		TokenPairUnaryCount tpuc = IOUtils.deserialize(value.getBytes(), TokenPairUnaryCount.class);
		long timet = TokenPairCount.timeFromBinaryIdentity(key.getBytes());
		long n = timecounts.get(timet);
		if( minPairCount != -1 && tpuc.paircount < minPairCount ) return;
		double pmi = tpuc.pmi(n);
		if(new Double(pmi).equals(Double.NaN)) return;
		if( minp == -1 || pmi > minp){
			byte[] serialized = PMIPairSort.timePMIBinary(timet,pmi);
			context.write(new BytesWritable(serialized), value);
		}
	};
}
