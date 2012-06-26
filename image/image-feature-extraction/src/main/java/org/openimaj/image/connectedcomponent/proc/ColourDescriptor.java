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
package org.openimaj.image.connectedcomponent.proc;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.statistics.BasicDescriptiveStatisticsModel;
import org.openimaj.image.processor.connectedcomponent.ConnectedComponentProcessor;
import org.openimaj.util.array.ArrayUtils;

/**
 * Descriptors based on the first-order statistics
 * of the colour of pixel values in an image.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ColourDescriptor implements ConnectedComponentProcessor, FeatureVectorProvider<DoubleFV> {
	/**
	 * The different types of statistic available. This provides
	 * a convenient way of getting a single statistic, as the {@link ColourDescriptor}
	 * computes them all.
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 *
	 */
	public enum ColourDescriptorType {
		/**
		 * The mean colour.
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		MEAN {
			@Override
			public DoubleFV getFeatureVector(ColourDescriptor desc) {
				return new DoubleFV(desc.colmodel.mean);
			}
		},
		/**
		 * The modal colour.
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		MODE {
			@Override
			public DoubleFV getFeatureVector(ColourDescriptor desc) {
				return new DoubleFV(desc.colmodel.mode);
			}
		},
		/**
		 * The median colour.
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		MEDIAN {
			@Override
			public DoubleFV getFeatureVector(ColourDescriptor desc) {
				return new DoubleFV(desc.colmodel.median);
			}
		},
		/**
		 * The range of the colours in the image.
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		RANGE {
			@Override
			public DoubleFV getFeatureVector(ColourDescriptor desc) {
				return new DoubleFV(desc.colmodel.range);
			}
		},
		/**
		 * The variance of the colours in the image.
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		VARIANCE {
			@Override
			public DoubleFV getFeatureVector(ColourDescriptor desc) {
				return new DoubleFV(desc.colmodel.variance);
			}
		};
		
		/**
		 * Extract the feature for the given descriptor.
		 * @param desc the descriptor to extract from
		 * @return the extracted feature.
		 */
		public abstract DoubleFV getFeatureVector(ColourDescriptor desc); 
	}
	
	protected MBFImage image;
	protected BasicDescriptiveStatisticsModel colmodel = new BasicDescriptiveStatisticsModel(3);
	
	/**
	 * Construct with no image. The image must be set with 
	 * {@link #setImage(MBFImage)} before processing. 
	 */
	public ColourDescriptor() {
	}
	
	/**
	 * Construct with the given image.
	 * @param image the image to extract pixels from.
	 */
	public ColourDescriptor(MBFImage image) {
		this.image = image;
	}
	
	@Override
	public void process(ConnectedComponent cc) {
		colmodel.estimateModel(cc.extractPixels1d(image));
	}

	/**
	 * @return the extracted feature (containing all statistics) from the last call to {@link #process(ConnectedComponent)}.
	 */
	public double[] getFeatureVectorArray() {
		return ArrayUtils.concatenate(
				colmodel.mean,
				colmodel.median,
				colmodel.mode,
				colmodel.range,
				colmodel.variance
		);
	}

	/**
	 * Set the image to extract pixels from.
	 * @param img the image.
	 */
	public void setImage(MBFImage img) {
		image = img;
	}
	
	/**
	 * @return the extracted colour model from the last call to {@link #process(ConnectedComponent)}
	 */
	public BasicDescriptiveStatisticsModel getModel() {
		return colmodel;
	}
	
	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(getFeatureVectorArray());
	}
}