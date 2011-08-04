package org.openimaj.image.processing.saliency;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processor.ImageProcessor;

/**
 * Implementation of the saliency map algorithm described in:
 * 
 * R. Achanta, S. Hemami, F. Estrada and S. SŸsstrunk, Frequency-tuned Salient 
 * Region Detection, IEEE International Conference on Computer Vision and 
 * Pattern Recognition (CVPR), 2009.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class AchantaSaliency implements ImageProcessor<MBFImage> {
	protected float sigma;
	protected FImage map;
	
	public AchantaSaliency(float sigma) {
		this.sigma = sigma;
	}
	
	public AchantaSaliency() {
		this.sigma = 1;
	}
	
	@Override
	public void processImage(MBFImage image, Image<?, ?>... otherimages) {
		int width = image.getWidth();
		int height = image.getHeight();
		
		MBFImage lab = ColourSpace.convert(image, ColourSpace.CIE_Lab);
		
		float[][] Lb = lab.getBand(0).pixels;
		float[][] ab = lab.getBand(1).pixels;
		float[][] bb = lab.getBand(2).pixels;
		float mL = 0, ma = 0, mb = 0;
		
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				mL += Lb[y][x];
				ma += ab[y][x];
				mb += bb[y][x];
			}
		}
		
		mL /= (height*width);
		ma /= (height*width);
		mb /= (height*width);
		
		//blur
		MBFImage blur = lab.process(new FGaussianConvolve(sigma));
		Lb = blur.getBand(0).pixels;
		ab = blur.getBand(1).pixels;
		bb = blur.getBand(2).pixels;
		
		//create map
		map = new FImage(width, height);
		for (int y=0; y<height; y++) {
			for (int x=0; x<width; x++) {
				float dL = (Lb[y][x]-mL);
				float da = (ab[y][x]-ma);
				float db = (bb[y][x]-mb);
				
				map.pixels[y][x] = dL*dL + da*da + db*db;
			}
		}
		map.normalise();
	}
	
	/**
	 * Get the saliency map
	 * @return
	 */
	public FImage getSaliencyMap() {
		return map;
	}
	
	public static void main(String [] args) throws MalformedURLException, IOException {
		MBFImage img = ImageUtilities.readMBF(new URL("http://ivrg.epfl.ch/supplementary_material/RK_CVPR09/Images/comparison/orig/0_5_5108.jpg"));
		
		AchantaSaliency sal = new AchantaSaliency(1);
		img.process(sal);
		
		DisplayUtilities.display(sal.getSaliencyMap());
	}
}
