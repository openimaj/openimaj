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
import java.io.StringWriter;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.tools.twitter.token.mode.pointwisemi.count.TokenPairUnaryCount;
import org.openimaj.io.IOUtils;
import org.openimaj.util.pair.IndependentPair;

import com.Ostermiller.util.CSVPrinter;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PMISortReducer extends Reducer<BytesWritable, BytesWritable, NullWritable,Text> {
	/**
	 * 
	 */
	public PMISortReducer(){
		
	}
	@Override
	protected void reduce(BytesWritable timepmi, Iterable<BytesWritable> textvalues, Reducer<BytesWritable,BytesWritable,NullWritable,Text>.Context context) throws IOException ,InterruptedException {
		String[] firstsecond = new String[2];
		for (BytesWritable value : textvalues) {
			IndependentPair<Long, Double> timepmii = PMIPairSort.parseTimeBinary(timepmi.getBytes());
			long time = timepmii.firstObject();
			TokenPairUnaryCount tpuc = IOUtils.deserialize(value.getBytes(), TokenPairUnaryCount.class);
			StringWriter swrit = new StringWriter();
			CSVPrinter csvp = new CSVPrinter(swrit);
			firstsecond[0] = tpuc.firstObject();
			firstsecond[1] = tpuc.secondObject();
//			System.out.println(Arrays.toString(firstsecond));
			csvp.write(new String[]{time+"",firstsecond[0],firstsecond[1],tpuc.paircount+"",tpuc.tok1count+"",tpuc.tok2count+"",timepmii.secondObject()+""});
			csvp.flush();
			context.write(NullWritable.get(), new Text(swrit.toString()));
		}
	};
}
