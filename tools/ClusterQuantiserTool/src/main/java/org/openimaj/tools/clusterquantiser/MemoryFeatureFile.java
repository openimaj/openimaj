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

import java.util.Arrays;
import java.util.Iterator;

/**
 * A feature file held in memory
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class MemoryFeatureFile extends FeatureFile {
	private String[] locations;
	private byte[][] data;
	
	/**
	 * Construct with the given data
	 * @param data
	 * @param locations
	 */
	public MemoryFeatureFile(byte[][]data, String[] locations){
		this.data = data;
		this.locations = locations;
	}
	
	@Override
	public Iterator<FeatureFileFeature> iterator() {
		// TODO Auto-generated method stub
		return new Iterator<FeatureFileFeature>(){

			int total = 0;
			@Override
			public boolean hasNext() {
				return total < data.length;
			}

			@Override
			public FeatureFileFeature next() {
				FeatureFileFeature f = new FeatureFileFeature();
				f.data = data[total];
				f.location = locations[total];
				total++;
				return f;
			}

			@Override
			public void remove() {
				Arrays.asList(data).remove(total-1);
				Arrays.asList(locations).remove(total-1);
			}
			
		};
	}
	@Override
	public int size() {
		// TODO Auto-generated method stub
		return this.data.length;
	}
	
	@Override
	public FeatureFileFeature get(int index){
		FeatureFileFeature fff = new FeatureFileFeature ();
		fff.data = this.data[index];
		fff.location = this.locations[index];
		return fff;
	}
	
	@Override
	public void close(){
		data= null;
		locations = null;
	}

}
