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
import org.openimaj.image.processor.ImageProcessor;

/**
 * Produce a feature vector that describes the average intensity
 * distribution across the from left to right. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class HorizontalIntensityDistribution implements ImageProcessor<FImage>, FeatureVectorProvider<DoubleFV> {
	DoubleFV fv;
	int nbins = 10;
	
	public HorizontalIntensityDistribution() {
	}
	
	public HorizontalIntensityDistribution(int nbins) {
		this.nbins = nbins;
	}
	
	@Override
	public DoubleFV getFeatureVector() {
		return fv;
	}

	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		fv = new DoubleFV(nbins);
		int [] counts = new int [nbins]; 
		
		float stripWidth = (float)image.width / (float)nbins;
		for (int y=0; y<image.height; y++) {
			for (int x=0; x<image.width; x++) {
				int bin = (int)(x / stripWidth);
				
				fv.values[bin] += image.pixels[y][x];
				counts[bin]++;
			}
		}
		
		for (int i=0; i<nbins; i++)
			fv.values[i] /= counts[i];
	}
	
	public static void main(String [] args) throws MalformedURLException, IOException {
		HorizontalIntensityDistribution s = new HorizontalIntensityDistribution();
//		FImage image = ImageUtilities.readF(new URL("http://farm1.static.flickr.com/8/9190606_8024996ff7.jpg"));
//		FImage image = ImageUtilities.readF(new URL("http://farm7.static.flickr.com/6201/6051101476_57afb46324.jpg"));
		FImage image = ImageUtilities.readF(new URL("http://farm5.static.flickr.com/4076/4905664253_17e7195206.jpg"));
		DisplayUtilities.display(image);
		image.process(s);
		System.out.println(s.getFeatureVector());
	}
}

