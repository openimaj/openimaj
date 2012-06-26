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
package org.openimaj.hadoop.tools.twitter.token.outputmode.jacard;

import java.io.StringWriter;
import java.util.HashSet;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.io.IOUtils;

/**
 * A cumulative jacard reducer designed to work as a single reducer. This reducer recieves the timeperiods
 * in order and holds a set of words seen. Using this set the intersection and union with the current time's words can be
 * calculated
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class CumulativeJacardReducer extends Reducer<LongWritable, Text, NullWritable, Text>{
	private HashSet<String> seenwords;

	public CumulativeJacardReducer() {
		this.seenwords = new HashSet<String>();
	}
	
	@Override
	protected void reduce(LongWritable time, java.lang.Iterable<Text> words, org.apache.hadoop.mapreduce.Reducer<LongWritable,Text,NullWritable,Text>.Context context) throws java.io.IOException ,InterruptedException {
		HashSet<String> unseenwords = new HashSet<String>();
		StringWriter writer = new StringWriter();
		
		for (Text text : words) {
			unseenwords.add(text.toString());
		}
		long intersection = 0;
		for (String string : unseenwords) {
			if(this.seenwords.contains(string)) intersection += 1;
			this.seenwords.add(string);
		}
		
		JacardIndex index = new JacardIndex(time.get(),intersection,this.seenwords.size());
		IOUtils.writeASCII(writer, index);
		context.write(NullWritable.get(), new Text(writer.toString()));
	};
}
