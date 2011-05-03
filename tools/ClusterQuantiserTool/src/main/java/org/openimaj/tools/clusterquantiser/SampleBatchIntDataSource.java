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
package org.openimaj.tools.clusterquantiser;

import gnu.trove.TIntArrayList;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.openimaj.data.RandomData;

import org.openimaj.ml.clustering.DataSource;

public class SampleBatchIntDataSource implements DataSource<byte[]> {
	private static final long serialVersionUID = 1L;
	
	private int total;
	private List<SampleBatch> batches;
	private int dims;

	public SampleBatchIntDataSource(List<SampleBatch> batches) throws IOException {
		this.batches = batches;
		this.total = batches.get(batches.size()-1).getEndIndex();
		this.dims = this.batches.get(0).getStoredSamples(0,1)[0].length;
	}

	@Override
	public void getData(int startRow, int stopRow, byte[][] output) {
		int added = 0;
		for(SampleBatch sb : batches){
			try {
				if(sb.getEndIndex() < startRow) continue; // Before this range
				if(sb.getStartIndex() > stopRow) continue; // After this range
			
				// So it must be within this range in some sense, find out where
				int startDelta = startRow - sb.getStartIndex();
				int stopDelta = stopRow - sb.getStartIndex();
			
				int interestedStart = startDelta < 0 ? 0 : startDelta;
				int interestedEnd = stopDelta + sb.getStartIndex() > sb.getEndIndex() ? sb.getEndIndex() - sb.getStartIndex() : stopDelta;
				byte[][] subSamples = sb.getStoredSamples(interestedStart,interestedEnd);
				
				for (int i=0; i<subSamples.length; i++) {
					System.arraycopy(subSamples[i], 0, output[added+i], 0, subSamples[i].length);
				}
				
				added+=subSamples.length;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void getRandomRows(byte[][] output) {
		int k = output.length;
		System.err.println("Requested random samples: " + k);
		int[] indices = RandomData.getUniqueRandomInts(k, 0, this.total);
		System.err.println("Array constructed");
		int l = 0;
		int j = 0;
		TIntArrayList samplesToLoad = new TIntArrayList();
		Arrays.sort(indices);
		int indicesMarker = 0;
		for(int sbIndex = 0 ; sbIndex < this.batches.size(); sbIndex++){
			samplesToLoad .clear();
			
			SampleBatch sb = this.batches.get(sbIndex);
			for(; indicesMarker < indices.length; indicesMarker++ ){
				int index = indices[indicesMarker]; 
				if(sb.getStartIndex() <= index && sb.getEndIndex() > index){
					samplesToLoad.add(index - sb.getStartIndex());
				}
				if(sb.getEndIndex() <= index)
					break;
			}
			
			try {
				if(samplesToLoad.size() == 0) continue;
				byte[][] features = sb.getStoredSamples(samplesToLoad.toNativeArray());
				for (int i=0; i<samplesToLoad.size(); i++) {
					System.arraycopy(features[i], 0, output[j++], 0, features[i].length);
					System.err.printf("\rCreating sample index hashmap %8d/%8d",l++,k);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int numDimensions() {
		return dims;
	}

	@Override
	public int numRows() {
		// TODO Auto-generated method stub
		return total;
	}

}
