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
package org.openimaj.tools.globalfeature;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.global.AvgBrightness;
import org.openimaj.image.feature.global.Colorfulness;
import org.openimaj.image.feature.global.HueStats;
import org.openimaj.image.feature.global.Naturalness;
import org.openimaj.image.feature.global.Sharpness;
import org.openimaj.image.pixel.statistics.BlockHistogramModel;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.image.pixel.statistics.MaskingHistogramModel;
import org.openimaj.image.pixel.statistics.MaskingLocalHistogramModel;
import org.openimaj.image.processing.face.detection.FaceDetectorFeatures;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.SandeepFaceDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector.BuiltInCascade;

import uk.ac.soton.ecs.dpd.ir.filters.CityLandscapeDetector;

public enum GlobalFeatures implements CmdLineOptionsProvider
{
    HISTOGRAM {
    	@Option(name="--color-space", aliases="-c", usage="Specify colorspace model", required=true)
    	ColourSpace converter;
    	
    	@Argument(required=true, usage="Number of bins per dimension")
    	List<Integer> bins = new ArrayList<Integer>();
    	
    	@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
    		MBFImage converted = converter.convert(image);
    		
    		if (converted.numBands() != bins.size()) {
    			throw new RuntimeException("Incorrect number of dimensions - recieved " + bins.size() +", expected " + converted.numBands() +".");
    		}
    		
    		int [] ibins = new int[bins.size()];
    		for (int i=0; i<bins.size(); i++)
    			ibins[i] = bins.get(i);
    		
    		HistogramModel hm = null; 
    		if (mask == null)
    			hm = new HistogramModel(ibins);
    		else 
    			hm = new MaskingHistogramModel(mask, ibins);
    		
    		hm.estimateModel(converted);
    		return hm.histogram;
    	}
    },
    LOCAL_HISTOGRAM {
    	@Option(name="--color-space", aliases="-c", usage="Specify colorspace model", required=true)
    	ColourSpace converter;
    	
    	@Option(name="--blocks-x", aliases="-bx", usage="Specify number of blocks in x-direction", required=true)
    	int blocks_x;
    	
    	@Option(name="--blocks-y", aliases="-by", usage="Specify number of blocks in y-direction", required=true)
    	int blocks_y;
    	
    	@Argument(required=true, usage="Number of bins per dimension")
    	List<Integer> bins = new ArrayList<Integer>();
    	
    	@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
    		MBFImage converted = converter.convert(image);
    		
    		if (converted.numBands() != bins.size()) {
    			throw new RuntimeException("Incorrect number of dimensions - recieved " + bins.size() +", expected " + converted.numBands() +".");
    		}
    		
    		int [] ibins = new int[bins.size()];
    		for (int i=0; i<bins.size(); i++)
    			ibins[i] = bins.get(i);
    		
    		BlockHistogramModel hm = null;
    		if (mask == null)
    			hm = new BlockHistogramModel(blocks_x, blocks_y, ibins);
    		else
    			hm = new MaskingLocalHistogramModel(mask, blocks_x, blocks_y, ibins);
    		
    		hm.estimateModel(converted);
    		return hm.toSingleHistogram();
    	}
    },
    EDGE_DIRECTION_COHERENCE_HISTOGRAM {
    	@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
    		CityLandscapeDetector<MBFImage> cldo = new CityLandscapeDetector<MBFImage>();
			image.process(cldo);
			
			if (mask != null)
				System.err.println("Warning: EDGE_DIRECTION_COHERENCE_HISTOGRAM doesn't support masking");
			
    		return cldo.getFeatureVector();
    	}
    },
    AVERAGE_BRIGHTNESS {
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			AvgBrightness f = new AvgBrightness();
			image.process(f, mask);
			return f.getFeatureVector();
		}
	},
	SHARPNESS {
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			Sharpness f = new Sharpness();
			Transforms.calculateIntensityNTSC(image).process(f, mask);
			return f.getFeatureVector();
		}
	},
    COLORFULNESS {
		@Option(name="--classes", usage="output class value (i.e. extremely, ..., not) instead of actual value.")
		boolean classMode = false;
		
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			Colorfulness f = new Colorfulness();
			
			if (mask == null)
				image.process(f);
			else
				image.processMasked(mask, f);
			
			if (classMode)
				return f.getColorfulnessAttribute().getFeatureVector();
			return f.getFeatureVector();
		}
	},
    HUE_STATISTICS {
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			HueStats f = new HueStats();
			image.process(f, mask);
			return f.getFeatureVector();
		}
	},
    NATURALNESS {
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			Naturalness f = new Naturalness();
			image.process(f, mask);
			return f.getFeatureVector();
		}
	},
	COLOR_FACES {
		@Option(name="--face-feature", aliases="-ff", required=true, usage="type of face feature to extract")
		FaceDetectorFeatures mode;
		
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			if (mask != null)
				System.err.println("Warning: COLOR_FACES doesn't support masking");
			
			SandeepFaceDetector fd = new SandeepFaceDetector();
			return mode.getFeatureVector(fd.detectFaces(image), image);
		}
	},
	HAAR_FACES {
		@Option(name="--face-feature", aliases="-ff", required=true, usage="type of face feature to extract")
		FaceDetectorFeatures mode;
		
		@Option(name="--cascade", aliases="-c", required=true, usage="the detector cascade to use")
		BuiltInCascade cascade;
		
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			if (mask != null)
				System.err.println("Warning: HAAR_FACES doesn't support masking");
			
			HaarCascadeDetector fd = cascade.load();
			return mode.getFeatureVector(fd.detectFaces(Transforms.calculateIntensityNTSC(image)), image);
		}
	}
    ;
    
    @Override
	public Object getOptions() {
		return this;
	}
    
    public abstract FeatureVector execute(MBFImage image, FImage mask);
    
    public FeatureVector execute(MBFImage image) {
    	return execute(image, null);
    }
}
