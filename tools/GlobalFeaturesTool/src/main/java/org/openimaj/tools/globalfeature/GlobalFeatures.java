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
import org.openimaj.image.feature.global.ColourContrast;
import org.openimaj.image.feature.global.HorizontalIntensityDistribution;
import org.openimaj.image.feature.global.HueStats;
import org.openimaj.image.feature.global.LRIntensityBalance;
import org.openimaj.image.feature.global.LuoSimplicity;
import org.openimaj.image.feature.global.ModifiedLuoSimplicity;
import org.openimaj.image.feature.global.Naturalness;
import org.openimaj.image.feature.global.ROIProportion;
import org.openimaj.image.feature.global.RuleOfThirds;
import org.openimaj.image.feature.global.SharpPixelProportion;
import org.openimaj.image.feature.global.Sharpness;
import org.openimaj.image.feature.global.WeberContrast;
import org.openimaj.image.pixel.statistics.BlockHistogramModel;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.image.pixel.statistics.MaskingHistogramModel;
import org.openimaj.image.pixel.statistics.MaskingLocalHistogramModel;
import org.openimaj.image.processing.algorithm.EdgeDirectionCoherenceVector;
import org.openimaj.image.processing.face.detection.FaceDetectorFeatures;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector.BuiltInCascade;
import org.openimaj.image.processing.face.detection.SandeepFaceDetector;

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
    		EdgeDirectionCoherenceVector cldo = new EdgeDirectionCoherenceVector();
			image.flatten().analyse(cldo);
			
			if (mask != null)
				System.err.println("Warning: EDGE_DIRECTION_COHERENCE_HISTOGRAM doesn't support masking");
			
    		return cldo.getFeatureVector();
    	}
    },
    AVERAGE_BRIGHTNESS {
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			AvgBrightness f = new AvgBrightness(mask);
			return f.getFeatureVector();
		}
	},
	SHARPNESS {
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			Sharpness f = new Sharpness(mask);
			Transforms.calculateIntensityNTSC(image).process(f);
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
			HueStats f = new HueStats(mask);
			image.process(f);
			return f.getFeatureVector();
		}
	},
    NATURALNESS {
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			Naturalness f = new Naturalness(mask);
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
	},
	COLOUR_CONTRAST {
		@Option(name="--sigma", aliases="-s", required=false, usage="the amount of Gaussian blurring applied prior to segmentation (default 0.5)")
		float sigma = 0.5f;
		
		@Option(name="--threshold", aliases="-k", required=false, usage="the segmentation threshold (default 500/255)")
		float k = 500f / 255f;
		
		@Option(name="--min-size", aliases="-m", required=false, usage="the minimum segment size (default 50)")
		int minSize = 50;
		
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			ColourContrast cc = new ColourContrast(sigma, k, minSize);
			image.processInline(cc);
			return cc.getFeatureVector();
		}
	},
	WEBER_CONTRAST {
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			WeberContrast cc = new WeberContrast();
			Transforms.calculateIntensityNTSC(image).processInline(cc);
			return cc.getFeatureVector();
		}
	},
	LR_INTENSITY_BALANCE {
		@Option(name="--num-bins", aliases="-n", required=false, usage="number of histogram bins (default 64)")
		int nbins = 64;
		
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			LRIntensityBalance cc = new LRIntensityBalance(nbins);
			Transforms.calculateIntensityNTSC(image).processInline(cc);
			return cc.getFeatureVector();
		}
	},
	RULE_OF_THIRDS {
		@Option(name="--saliency-sigma", aliases="-sals", required=false, usage="the amount of Gaussian blurring for the saliency estimation (default 1.0)")
		float saliencySigma = 1f;
		
		@Option(name="--segment-sigma", aliases="-segs", required=false, usage="the amount of Gaussian blurring applied prior to segmentation (default 0.5)")
		float segmenterSigma = 0.5f;
		
		@Option(name="--threshold", aliases="-k", required=false, usage="the segmentation threshold (default 500/255)")
		float k = 500f / 255f;
		
		@Option(name="--min-size", aliases="-m", required=false, usage="the minimum segment size (default 50)")
		int minSize = 50;
		
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			RuleOfThirds cc = new RuleOfThirds(saliencySigma, segmenterSigma, k, minSize);
			image.processInline(cc);
			return cc.getFeatureVector();
		}
	},
	ROI_PROPORTION {
		@Option(name="--saliency-sigma", aliases="-sals", required=false, usage="the amount of Gaussian blurring for the saliency estimation (default 1.0)")
		float saliencySigma = 1f;
		
		@Option(name="--segment-sigma", aliases="-segs", required=false, usage="the amount of Gaussian blurring applied prior to segmentation (default 0.5)")
		float segmenterSigma = 0.5f;
		
		@Option(name="--threshold", aliases="-k", required=false, usage="the segmentation threshold (default 500/255)")
		float k = 500f / 255f;
		
		@Option(name="--min-size", aliases="-m", required=false, usage="the minimum segment size (default 50)")
		int minSize = 50;
		
		@Option(name="--alpha", aliases="-a", required=false, usage="The proportion of the maximum saliency value at which we choose the ROI components (default 0.67)")
		float alpha = 0.67f;
		
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			ROIProportion cc = new ROIProportion(saliencySigma, segmenterSigma, k, minSize, alpha);
			image.processInline(cc);
			return cc.getFeatureVector();
		}
	},
	HORIZONTAL_INTENSITY_DISTRIBUTION {
		@Option(name="--num-bins", aliases="-n", required=false, usage="number of histogram bins (default 64)")
		int nbins = 64;
		
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			HorizontalIntensityDistribution cc = new HorizontalIntensityDistribution(nbins);
			Transforms.calculateIntensityNTSC(image).processInline(cc);
			return cc.getFeatureVector();
		}
	},
	SHARP_PIXEL_PROPORTION {
		@Option(name="--threshold", aliases="-t", required=false, usage="frequency power threshold (default 2.0)")
		float thresh = 2f;
		
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			SharpPixelProportion cc = new SharpPixelProportion(thresh);
			Transforms.calculateIntensityNTSC(image).processInline(cc);
			return cc.getFeatureVector();
		}
	},
	LUO_SIMPLICITY {
		@Option(name="--bins-per-band", aliases="-bpb", required=false, usage="Number of bins to split the R, G and B bands into when constructing the histogram (default 16)")
		int binsPerBand = 16;
		
		@Option(name="--gamma", required=false, usage="percentage threshold on the max value of the histogram for counting high-valued bins (default 0.01)")
		float gamma = 0.01f;
		
		@Option(name="--no-box", required=false, usage="use the actual predicted foreground/background pixels rather than their bounding box (default false)")
		boolean noBoxMode = false;
		
		@Option(name="--alpha", required=false, usage="alpha parameter for determining bounding box size based on the energy ratio (default 0.9)")
		float alpha = 0.9f;
		
		@Option(name="--max-kernel-size", required=false, usage="maximum smoothing kernel size (default 50)")
		int maxKernelSize;
		
		@Option(name="--kernel-size-step", required=false, usage="step size to increment smoothing kernel by (default 1)")
		int kernelSizeStep = 1;
		
		@Option(name="--num-bins", required=false, usage="number of bins for the gradiant histograms (default 41)")
		int nbins = 41;
		
		@Option(name="--window-size", required=false, usage="window size for estimating depth of field (default 3)")
		int windowSize = 3;
		
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			LuoSimplicity cc = new LuoSimplicity(binsPerBand, gamma, !noBoxMode, alpha, maxKernelSize, kernelSizeStep, nbins, windowSize);
			image.processInline(cc);
			return cc.getFeatureVector();
		}
	},
	MODIFIED_LUO_SIMPLICITY {
		@Option(name="--bins-per-band", aliases="-bpb", required=false, usage="Number of bins to split the R, G and B bands into when constructing the histogram (default 16)")
		int binsPerBand = 16;
		
		@Option(name="--gamma", required=false, usage="percentage threshold on the max value of the histogram for counting high-valued bins (default 0.01)")
		float gamma = 0.01f;
		
		@Option(name="--no-box", required=false, usage="use the actual predicted foreground/background pixels rather than their bounding box (default false)")
		boolean noBoxMode = false;
		
		@Option(name="--alpha", aliases="-a", required=false, usage="The proportion of the maximum saliency value at which we choose the ROI components (default 0.67)")
		float alpha = 0.67f;
		
		@Option(name="--saliency-sigma", aliases="-sals", required=false, usage="the amount of Gaussian blurring for the saliency estimation (default 1.0)")
		float saliencySigma = 1f;
		
		@Option(name="--segment-sigma", aliases="-segs", required=false, usage="the amount of Gaussian blurring applied prior to segmentation (default 0.5)")
		float segmenterSigma = 0.5f;
		
		@Option(name="--threshold", aliases="-k", required=false, usage="the segmentation threshold (default 500/255)")
		float k = 500f / 255f;
		
		@Option(name="--min-size", aliases="-m", required=false, usage="the minimum segment size (default 50)")
		int minSize = 50;
		
		@Override
		public FeatureVector execute(MBFImage image, FImage mask) {
			ModifiedLuoSimplicity cc = new ModifiedLuoSimplicity(binsPerBand, gamma, !noBoxMode, alpha, saliencySigma, segmenterSigma, k, minSize);
			image.processInline(cc);
			return cc.getFeatureVector();
		}
	},
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
