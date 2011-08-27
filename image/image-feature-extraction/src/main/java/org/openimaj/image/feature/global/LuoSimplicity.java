package org.openimaj.image.feature.global;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.pixel.statistics.MaskingHistogramModel;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.saliency.LuoTangSubjectRegion;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Estimate the simplicity of an image by looking at the
 * colour distribution of the background
 * 
 * Algorithm based on:
 * Yiwen Luo and Xiaoou Tang. 2008. 
 * Photo and Video Quality Evaluation: Focusing on the Subject. 
 * In Proceedings of the 10th European Conference on Computer Vision: 
 * Part III (ECCV '08), David Forsyth, Philip Torr, and Andrew Zisserman (Eds.). 
 * Springer-Verlag, Berlin, Heidelberg, 386-399. DOI=10.1007/978-3-540-88690-7_29 
 * http://dx.doi.org/10.1007/978-3-540-88690-7_29
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class LuoSimplicity implements ImageProcessor<MBFImage>, FeatureVectorProvider<DoubleFV> {
	LuoTangSubjectRegion extractor;
	int binsPerBand = 16;
	float gamma = 0.01f;
	double simplicity;
	boolean boxMode = true;
	
	public LuoSimplicity() {
		extractor = new LuoTangSubjectRegion();
	}
	
	public LuoSimplicity(int binsPerBand, float gamma, boolean boxMode, float alpha, int maxKernelSize, int kernelSizeStep, int nbins, int windowSize) {
		extractor = new LuoTangSubjectRegion(alpha, maxKernelSize, kernelSizeStep, nbins, windowSize);
		this.binsPerBand = binsPerBand;
		this.gamma = gamma;
		this.boxMode = boxMode;
	}
	
	@Override
	public void processImage(MBFImage image, Image<?, ?>... otherimages) {
		Transforms.calculateIntensityNTSC(image).process(extractor);
		FImage mask = extractor.getROIMap().inverse();
		
		MaskingHistogramModel hm = new MaskingHistogramModel(mask, binsPerBand, binsPerBand, binsPerBand);
		hm.estimateModel(image);
		
		Histogram fv = hm.getFeatureVector();
		double thresh = gamma* fv.max();
		int count = 0;
		for (double f : fv.values) {
			if (f >= thresh) 
				count++;
		}
		
		simplicity = (double)count / (double)fv.values.length;
	}
	
	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { simplicity });
	}
	
	public static void main(String [] args) throws MalformedURLException, IOException {
		LuoSimplicity s = new LuoSimplicity();
		MBFImage image = ImageUtilities.readMBF(new URL("http://farm7.static.flickr.com/6192/6070918114_8474816781.jpg"));	
		image.process(s);
		System.out.println(s.getFeatureVector());
	}
}
