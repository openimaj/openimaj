package org.openimaj.image.feature.global;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.pixel.statistics.BlockHistogramModel;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.math.statistics.distribution.Histogram;

/**
 * Implementation of the intensity balance algorithm described in:
 * 
 * Che-Hua Yeh, Yuan-Chen Ho, Brian A. Barsky, Ming Ouhyoung.
 * Personalized photograph ranking and selection system.
 * In Proceedings of ACM Multimedia'2010. pp.211~220
 *
 * The intensity balance measures how different the intensity
 * is on the left side of the image compared to the right.
 * A balance of zero means exactly balanced. Higher values
 * are produced for more unbalanced images.
 *
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class LRIntensityBalance implements ImageProcessor<FImage>, FeatureVectorProvider<DoubleFV> {
	int nbins = 64;
	double balance;
	
	public LRIntensityBalance() {
	}
	
	public LRIntensityBalance(int nbins) {
		this.nbins = nbins;
	}

	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { balance });
	}

	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		BlockHistogramModel hm = new BlockHistogramModel(2, 1, nbins);
		
		hm.estimateModel(image);
		
		Histogram left = hm.histograms[0][0];
		Histogram right = hm.histograms[0][1];
		
		balance = left.compare(right, DoubleFVComparison.CHI_SQUARE);
	}
	
	public static void main(String [] args) throws MalformedURLException, IOException {
		LRIntensityBalance s = new LRIntensityBalance();
		FImage image = ImageUtilities.readF(new URL("http://farm1.static.flickr.com/8/9190606_8024996ff7.jpg"));
//		FImage image = ImageUtilities.readF(new URL("http://farm7.static.flickr.com/6201/6051101476_57afb46324.jpg"));
//		FImage image = ImageUtilities.readF(new URL("http://farm5.static.flickr.com/4076/4905664253_17e7195206.jpg"));
		DisplayUtilities.display(image);
		image.process(s);
		System.out.println(s.getFeatureVector());
	}
}
