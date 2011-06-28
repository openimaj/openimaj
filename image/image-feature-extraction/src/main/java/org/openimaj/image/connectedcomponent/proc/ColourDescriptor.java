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

/**
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ColourDescriptor implements ConnectedComponentProcessor, FeatureVectorProvider<DoubleFV> {
	public enum ColourDescriptorType {
		MEAN {
			@Override
			public DoubleFV getFeatureVector(ColourDescriptor desc) {
				return new DoubleFV(desc.colmodel.mean);
			}
		},
		MODE {
			@Override
			public DoubleFV getFeatureVector(ColourDescriptor desc) {
				return new DoubleFV(desc.colmodel.mode);
			}
		},
		MEDIAN {
			@Override
			public DoubleFV getFeatureVector(ColourDescriptor desc) {
				return new DoubleFV(desc.colmodel.median);
			}
		},
		RANGE {
			@Override
			public DoubleFV getFeatureVector(ColourDescriptor desc) {
				return new DoubleFV(desc.colmodel.range);
			}
		},
		VARIANCE {
			@Override
			public DoubleFV getFeatureVector(ColourDescriptor desc) {
				return new DoubleFV(desc.colmodel.variance);
			}
		};
		
		public abstract DoubleFV getFeatureVector(ColourDescriptor desc); 
	}
	
	MBFImage rgbimage;
	BasicDescriptiveStatisticsModel colmodel = new BasicDescriptiveStatisticsModel(3);
	
	public ColourDescriptor() {
	}
	
	public ColourDescriptor(MBFImage image) {
		this.rgbimage = image;
	}
	
	@Override
	public void process(ConnectedComponent cc) {
		colmodel.estimateModel(cc.extractPixels1d(rgbimage));
	}

	public double[] getFeatureVectorArray() {
		return new double[] {
				colmodel.mean[0], colmodel.mean[1], colmodel.mean[2],
				colmodel.median[0], colmodel.median[1], colmodel.median[2],
				colmodel.mode[0], colmodel.mode[1], colmodel.mode[2],
				colmodel.range[0], colmodel.range[1], colmodel.range[2],
				colmodel.variance[0], colmodel.variance[1], colmodel.variance[2]
		};
	}

	public void setImage(MBFImage img) {
		rgbimage = img;
	}
	
	public BasicDescriptiveStatisticsModel getModel() {
		return colmodel;
	}
	
	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(getFeatureVectorArray());
	}
}