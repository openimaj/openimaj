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
package org.openimaj.hadoop.sequencefile;

import java.util.Arrays;

import org.apache.hadoop.fs.Path;
import org.openimaj.data.RandomData;


public class ExtractionPolicy {
	private int count = 0;
	private int max = -1;
	private int[] randomInts = null;
	private boolean autoFileExtention;
	
	public int getCount() {
		return count;
	}
	
	public boolean validate(){
		if(this.max != -1 && this.count >= this.max) return false;
		if(this.randomInts == null) return true;
		int thingLocation = Arrays.binarySearch(randomInts, this.count);
		return thingLocation>=0; 
	}
	
	public boolean stop(){
		return this.max != -1 && this.count >= this.max;
	}
	
	public <K,V> void tick(K key, V val,Path outFilePath ){
		this.count++;
	}
	
	public void setRandomSelection(int random, int totalRecords) {
		this.randomInts = RandomData.getUniqueRandomInts(random, 0, totalRecords);
		Arrays.sort(randomInts);
		System.out.println(this.randomInts .length + " random ints selected");
		System.out.println(Arrays.toString(randomInts));
	}

	public boolean autoFileExtention() {
		return autoFileExtention;
	}
	
	public void setAutoFileExtention(boolean w) {
		autoFileExtention = w;
	}

	public void setMaxFileExtract(int max) {
		this.max = max;
	}

}
