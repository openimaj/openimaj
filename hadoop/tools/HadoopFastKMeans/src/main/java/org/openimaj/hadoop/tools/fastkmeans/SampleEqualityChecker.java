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

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.openimaj.hadoop.sequencefile.ExtractionState;
import org.openimaj.hadoop.sequencefile.KeyValueDump;
import org.openimaj.hadoop.sequencefile.NamingStrategy;


public class SampleEqualityChecker {
	
	static class ByteArrayDump extends KeyValueDump<Text,BytesWritable>{
		int index = 0;
		int randomGens = 0;
		ArrayList<byte[]> centroids;
		
		ByteArrayDump(){
			centroids = new ArrayList<byte[]>();
		}
		@Override
		public void dumpValue(Text key, BytesWritable val) {
			byte [] bytes = new byte[val.getLength()]; 
			System.arraycopy(val.getBytes(), 0, bytes, 0, bytes.length);
			centroids.add(bytes);
		}
		
	}
	public static void checkSampleEquality(String selected,HadoopFastKMeansOptions options) throws IOException {
		ByteArrayDump neededdump = new ByteArrayDump();
		TextBytesSequenceMemoryUtility utility = new TextBytesSequenceMemoryUtility(selected, true);
		utility.exportData(NamingStrategy.KEY, new ExtractionState(), 0, neededdump);
		System.out.println("Finished loading all byte arrays");
		int total = 0;
		long done = 0;
		for(int i = 0; i < neededdump.centroids.size(); i++){
			byte[] a = neededdump.centroids.get(i);
			for(int j = i+1; j < neededdump.centroids.size(); j++){
				
				done++;
				if(distanceUnderThreshold(a,neededdump.centroids.get(j),options.checkSampleEqualityThreshold) ){
					total++;
				}
			}
			System.out.print("\r" + done + "/" + ((long)(neededdump.centroids.size()) * (long)(neededdump.centroids.size()))/2l + " total: " + total);
		}
		System.out.println();
		System.out.println("There were " + total + " identical samples");
	}
	private static boolean distanceUnderThreshold(byte[] a, byte[] b, int threshold) {
		int totalDistance = 0;
		for(int i = 0; i < a.length; i++){
			int diff = ((int)a[i]) - ((int)b[i]);
			totalDistance += diff * diff;
			if(totalDistance > threshold){
//					System.out.println("Total distance is: " + totalDistance);
				return false;
			}
		}
		return true;
	}

}
