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
package org.openimaj.image.pixel.statistics;

import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntIntProcedure;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;

import static java.lang.Math.sqrt;

/**
 * A model of the all the values of the pixels in an image or set of images,
 * using basic descriptive statistics (mean, mode, median, range, variance).
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class BasicDescriptiveStatisticsModel extends AbstractPixelStatisticsModel {
	private static final long serialVersionUID = 1L;

	/**
	 * The mean pixel value
	 */
	public double [] mean;
	
	/**
	 * The mode of pixel values
	 */
	public double [] mode;
	
	/**
	 * The median of pixel values
	 */
	public double [] median;
	
	/**
	 * The range of pixel values
	 */
	public double [] range;
	
	/**
	 * The variance of pixel values
	 */
	public double [] variance;
	
	/**
	 * Construct a BasicDescriptiveStatisticsModel with the given
	 * number of dimensions. The number of dimensions should normally 
	 * be equal to the number of bands in the images from which the model
	 * is to be estimated. 
	 *  
	 * @param ndims number of dimensions
	 */
	public BasicDescriptiveStatisticsModel(int ndims) {
		super(ndims);
		
		mean = new double[ndims];
		mode = new double[ndims];
		median = new double[ndims];
		range = new double[ndims];
		variance = new double[ndims];
	}

	@Override
	public void estimateModel(MBFImage... images) {	
		for (int i=0; i<ndims; i++) {
			mean[i] = 0;
			TFloatArrayList values = new TFloatArrayList();
			TIntIntHashMap freqs = new TIntIntHashMap();
			
			int count = 0;
			for (MBFImage im : images) {
				FImage band = im.getBand(i);
				
				for (int r=0; r<band.height; r++) {
					for (int c=0; c<band.width; c++) {
						float val = band.pixels[r][c];
						mean[i] += val;
						values.add(val);
						freqs.adjustOrPutValue(Math.round(val*255F), 1, 1);
						count++;
					}
				}
			}
			
			//mean
			mean[i] /= count;
			
			//median
			values.sort();
			int idx = values.size() / 2;
			if (values.size() % 2 == 0) {
				median[i] = (values.get(idx) + values.get(idx - 1)) / 2.0;
			} else {
				median[i] = values.get(idx);
			}

			//mode
			HashMax hm = new HashMax();
			freqs.forEachEntry(hm);
			mode[i] = hm.idx / 255.0;
			
			//range
			range[i] = values.get(values.size() - 1) - values.get(0);
			
			//variance
			variance[i] = 0;
			for (int j=0; j<values.size(); j++) {
				variance[i] += (values.get(j) - mean[i]) * (values.get(j) - mean[i]);
			}
			variance[i] = sqrt(variance[i] / values.size());
		}
	}
	
	@Override
	public String toString() {
		String desc = "";
		for (int i=0; i<ndims; i++) desc += String.format("%2.2f, ", mean[i]);
		for (int i=0; i<ndims; i++) desc += String.format("%2.2f, ", mode[i]);
		for (int i=0; i<ndims; i++) desc += String.format("%2.2f, ", median[i]);
		for (int i=0; i<ndims; i++) desc += String.format("%2.2f, ", range[i]);
		for (int i=0; i<ndims; i++) desc += String.format("%2.2f, ", variance[i]);
		return desc.substring(0, desc.length()-2);
	}
}

class HashMax implements TIntIntProcedure {
	public int max = 0;
	public int idx = -1;
	
	@Override
	public boolean execute(int key, int value) {
		if (value > max) {
			max = value;
			idx = key;
		}
		return true;
	}
}
