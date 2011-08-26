package org.openimaj.image.feature.global;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processor.GridProcessor;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * Implementation of the Bokeh estimation feature described in:
 * 
 * Che-Hua Yeh, Yuan-Chen Ho, Brian A. Barsky, Ming Ouhyoung.
 * Personalized photograph ranking and selection system.
 * In Proceedings of ACM Multimedia'2010. pp.211~220
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class YehBokehEstimator implements ImageProcessor<FImage>, FeatureVectorProvider<DoubleFV> {
	class Sharpness implements GridProcessor<Float, FImage> {
		SharpPixelProportion bpp = new SharpPixelProportion();
		
		@Override
		public int getHorizontalGridElements() {
			return nBlocksX;
		}

		@Override
		public int getVericalGridElements() {
			return nBlocksY;
		}

		@Override
		public Float processGridElement(FImage patch) {
			patch.processInline(bpp);
			return (float) bpp.getBlurredPixelProportion();
		}
	}
	
	class GreyLevelVariance implements GridProcessor<Float, FImage> {
		@Override
		public int getHorizontalGridElements() {
			return nBlocksX;
		}

		@Override
		public int getVericalGridElements() {
			return nBlocksY;
		}

		@Override
		public Float processGridElement(FImage patch) {
			return FloatArrayStatsUtils.var(patch.pixels);
		}
	}
	
	Sharpness sharpProcessor = new Sharpness();
	GreyLevelVariance varProcessor = new GreyLevelVariance();
	
	int nBlocksX = 5;
	int nBlocksY = 5;
	
	float varThreshold = 0.1f;
	float sharpnessThreshold = 0.5f;
	float lowerBound = 0.3f;
	float upperBound = 0.7f;
	
	double bokeh;

	public YehBokehEstimator() {}

	public YehBokehEstimator(int nBlocksX, int nBlocksY, float varThreshold, float sharpnessThreshold, float lowerBound, float upperBound) {
		this.nBlocksX = nBlocksX;
		this.nBlocksY = nBlocksY;
		this.varThreshold = varThreshold;
		this.sharpnessThreshold = sharpnessThreshold;
		this.lowerBound = lowerBound; 
		this.upperBound = upperBound; 
	}
	
	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double [] { bokeh });
	}
	
	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		FImage sharpness = image.process(sharpProcessor);
		FImage variance = image.process(varProcessor);
		
		double Qbokeh = 0;
		int validBlocks = 0;
		for (int y=0; y<sharpness.height; y++) {
			for (int x=0; x<sharpness.width; x++) {
				if (variance.pixels[y][x] >= varThreshold) {
					Qbokeh += sharpness.pixels[y][x] > 0.5 ? 1 : 0;
					validBlocks++;
				}
			}
		}
		Qbokeh /= (validBlocks);
			
		bokeh = (Qbokeh>=lowerBound && Qbokeh<=upperBound) ? 1 : 0;
	}
	
	public static void main(String [] args) throws MalformedURLException, IOException {
		YehBokehEstimator s = new YehBokehEstimator();
		FImage image = ImageUtilities.readF(new URL("http://upload.wikimedia.org/wikipedia/commons/8/8a/Josefina_with_Bokeh.jpg"));
		DisplayUtilities.display(image);
		image.process(s);
		System.out.println(s.bokeh);
	}
}
