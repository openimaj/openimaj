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
import org.openimaj.image.processing.algorithm.FourierTransform;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processor.ImageProcessor;

public class BlurredPixelProportion implements ImageProcessor<FImage>, FeatureVectorProvider<DoubleFV> {
	double bpp = 0;
	private float threshold = 2f;
	
	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { bpp });
	}

	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		FourierTransform ft = new FourierTransform(image, false);
		FImage mag = ft.getMagnitude();
		
		int count = 0;
		for(int y = 0; y < mag.height ; y++) {
			for(int x = 0; x < mag.width; x++) {
				if (mag.pixels[y][x] > threshold) count++; 
			}
		}
		bpp = (double)count / (double)(mag.height * mag.width);
		
		DisplayUtilities.display(image, ""+bpp);
	}

	public static void main(String [] args) throws MalformedURLException, IOException {
		BlurredPixelProportion s = new BlurredPixelProportion();
		FImage image = ImageUtilities.readF(new URL("http://farm1.static.flickr.com/8/9190606_8024996ff7.jpg"));
//		FImage image = ImageUtilities.readF(new URL("http://upload.wikimedia.org/wikipedia/commons/8/8a/Josefina_with_Bokeh.jpg"));
//		FImage image = ImageUtilities.readF(new URL("http://upload.wikimedia.org/wikipedia/commons/4/4a/Thumbs_up_for_bokeh.JPG"));
		
		FImage foo = image;
		for (int i=0; i<10; i++) {
			float sigma = 0.6f + (0.1f * i);
			
			DisplayUtilities.display(foo, ""+sigma);
			
			foo.process(s);
			
			System.out.println(sigma + " " + s.getFeatureVector());
			
			sigma = 0.6f + (0.1f * (i+1));
			foo = image.process(new FGaussianConvolve(sigma));
		}
	}

	public double getBlurredPixelProportion() {
		return bpp;
	}
}
