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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.openimaj.hadoop.sequencefile.ExtractionPolicy;
import org.openimaj.hadoop.sequencefile.KeyValueDump;
import org.openimaj.hadoop.sequencefile.NamingPolicy;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.io.IOUtils;

import org.openimaj.ml.clustering.kmeans.fast.FastByteKMeansCluster;

public class ExactNNError {
	public static void main(String args[]) throws IOException{
		FastByteKMeansCluster vocab = IOUtils.read(new File("/Users/ss/Development/LiveMemories/trunk/shared/HadoopFastKMeans/errordata/fastkmeanscluster.voc"), FastByteKMeansCluster.class);
		
		SequenceFileUtility<Text, BytesWritable> utility = new TextBytesSequenceMemoryUtility(new Path("/Users/ss/Development/LiveMemories/trunk/shared/HadoopFastKMeans/errordata/image-net-10000.seq_select_100000/part-r-00000").toUri(), true);
		final HashMap<String,byte[]> allFeatures = new HashMap<String,byte[]>();
		utility.exportData(NamingPolicy.KEY, new ExtractionPolicy(), 0, new KeyValueDump<Text,BytesWritable>(){

			@Override
			public void dumpValue(Text key, BytesWritable val) {
				byte [] bytes = new byte[val.getLength()]; 
				System.arraycopy(val.getBytes(), 0, bytes, 0, bytes.length);
				allFeatures.put(key.toString(), bytes);
			}
		});
		vocab.optimize(true);
		String query = "5016";
		byte[] data = allFeatures.get(query);
		int cluster = vocab.push_one(data);
		System.out.println(query  + " matched to: " + cluster);
		System.out.println(Arrays.toString(data));
		System.out.println(Arrays.toString(((byte[][])vocab.getClusters())[cluster]));
		System.out.println(Arrays.toString(((byte[][])vocab.getClusters())[Integer.parseInt(query)]));
		
		
	}
}
