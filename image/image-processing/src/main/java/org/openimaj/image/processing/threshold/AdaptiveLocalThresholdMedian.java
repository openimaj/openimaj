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
package org.openimaj.image.processing.threshold;

import java.util.Arrays;

import org.openimaj.image.FImage;

/**
 * Adaptive local thresholding using the median of the patch and
 * an offset.
 *  
 * @see <a href="http://homepages.inf.ed.ac.uk/rbf/HIPR2/adpthrsh.htm">http://homepages.inf.ed.ac.uk/rbf/HIPR2/adpthrsh.htm</a>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class AdaptiveLocalThresholdMedian extends AbstractLocalThreshold {
	float offset = 0;

	/**
	 * Construct the thresholding operator with the given
	 * patch size (assumed square)
	 * @param size size of the local image patch
	 */
	public AdaptiveLocalThresholdMedian(int size) {
		super(size);
	}

	/**
	 * Construct the thresholding operator with the given
	 * patch size
	 * @param size_x width of patch
	 * @param size_y height of patch
	 */
	public AdaptiveLocalThresholdMedian(int size_x, int size_y) {
		super(size_x, size_y);
	}

	/**
	 * Construct the thresholding operator with the given
	 * patch size (assumed square) and offset
	 * @param size size of the local image patch
	 * @param offset offset from the patch mean at which the threshold occurs
	 */
	public AdaptiveLocalThresholdMedian(int size, float offset) {
		this(size, size, offset);
	}

	/**
	 * Construct the thresholding operator with the given
	 * patch size and offset
	 * @param size_x width of patch
	 * @param size_y height of patch
	 * @param offset offset from the patch mean at which the threshold occurs
	 */
	public AdaptiveLocalThresholdMedian(int size_x, int size_y, float offset) {
		super(size_x, size_y);
		this.offset = offset;
	}

	@Override
	public Float processKernel(FImage patch) {
		int count = 0;
		float c;
		float [] vec = new float[size_x*size_y];

		c = patch.pixels[size_y/2][size_x/2];

		for (int j=0; j<size_y; j++) {
			for (int i=0; i<size_x; i++) {
				vec[count++] = patch.pixels[j][i];
			}
		}

		Arrays.sort(vec);

		float thresh = vec[vec.length / 2] - offset;

		return (c<thresh) ? 0F : 1F;
	}
}
