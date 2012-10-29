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
package org.openimaj.hadoop.tools.fastkmeans;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.tools.clusterquantiser.FileType;
import org.openimaj.tools.clusterquantiser.Header;

/**
 * Map Reduce job to count features
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class FeatureCount {
	private static final String FILETYPE_KEY = "clusterquantiser.FileType";

	/**
	 * Map class. Emits number of features per record.
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class Map extends Mapper<Text, BytesWritable, Text, IntWritable>
	{
		private final static IntWritable nfeatures = new IntWritable(1);
		private Text word = new Text("total");
		private static FileType fileType = null;

		@Override
		protected void setup(Mapper<Text, BytesWritable, Text, IntWritable>.Context context) throws IOException,
				InterruptedException
		{
			if (fileType == null) {
				fileType = FileType.valueOf(context.getConfiguration().get(FILETYPE_KEY));
			}
		}

		@Override
		public void map(Text key, BytesWritable value, Context context) throws IOException, InterruptedException {
			final Header input = fileType.readHeader(new ByteArrayInputStream(value.getBytes()));
			nfeatures.set(input.nfeatures);
			context.write(word, nfeatures);
		}
	}

	/**
	 * Reduce class. Combines counts per map record into overall sum.
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 * 
	 */
	public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {
		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException,
				InterruptedException
		{
			int sum = 0;
			for (final IntWritable val : values) {
				sum += val.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}
}
