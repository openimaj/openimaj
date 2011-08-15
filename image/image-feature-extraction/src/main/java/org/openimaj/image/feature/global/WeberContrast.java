package org.openimaj.image.feature.global;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Implementation of the Weber contrast feature.
 * 
 * See this paper for a description: 
 * Che-Hua Yeh, Yuan-Chen Ho, Brian A. Barsky, Ming Ouhyoung.
 * Personalized photograph ranking and selection system.
 * In Proceedings of ACM Multimedia'2010. pp.211~220
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class WeberContrast implements ImageProcessor<FImage>, FeatureVectorProvider<DoubleFV> {
	double contrast;
	
	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double [] {contrast} );
	}

	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		int width = 0;
		int height = 0;
		double avg = 0;
		
		for (int y=0; y<height; y++)
			for (int x=0; x<width; x++)
				avg += image.pixels[y][x];
		
		contrast = 0;
		for (int y=0; y<height; y++)
			for (int x=0; x<width; x++)
				contrast += (image.pixels[y][x] - avg) / avg;
		
		contrast /= (height * width);
	}

}
