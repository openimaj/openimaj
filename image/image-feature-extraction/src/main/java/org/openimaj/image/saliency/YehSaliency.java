package org.openimaj.image.saliency;

import gnu.trove.TObjectFloatHashMap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.pixel.Pixel;
import org.openimaj.image.segmentation.FelzenszwalbHuttenlocherSegmenter;

/**
 * Implementation of the region-based saliency algorithm described in:
 * 
 * Che-Hua Yeh, Yuan-Chen Ho, Brian A. Barsky, Ming Ouhyoung.
 * Personalized photograph ranking and selection system.
 * In Proceedings of ACM Multimedia'2010. pp.211~220
 * 
 * This algorithm is used to create a Rule-of-Thirds feature for images.
 * 
 * The algorithm uses the {@link AchantaSaliency} approach to get the saliency
 * values for individual pixels. Regions are segmented from the image 
 * using a {@link FelzenszwalbHuttenlocherSegmenter}. Saliency values are
 * generated for each region by averaging the saliency values of the
 * pixels within the region.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class YehSaliency implements SaliencyMapGenerator<MBFImage> {
	AchantaSaliency saliencyGenerator;
	FelzenszwalbHuttenlocherSegmenter<MBFImage> segmenter;
	protected FImage map;
	protected TObjectFloatHashMap<ConnectedComponent> componentMap;
	
	public YehSaliency() {
		saliencyGenerator = new AchantaSaliency();
		segmenter = new FelzenszwalbHuttenlocherSegmenter<MBFImage>();
	}
	
	public YehSaliency(float saliencySigma, float segmenterSigma, float k, int minSize) {
		saliencyGenerator = new AchantaSaliency(saliencySigma);
		segmenter = new FelzenszwalbHuttenlocherSegmenter<MBFImage>(segmenterSigma, k, minSize);
	}
	
	@Override
	public void processImage(MBFImage image, Image<?, ?>... otherimages) {
		List<ConnectedComponent> ccs = segmenter.segment(image);
		
		image.process(saliencyGenerator);
		map = saliencyGenerator.getSaliencyMap();
		componentMap = new TObjectFloatHashMap<ConnectedComponent>();
		
		for (ConnectedComponent cc : ccs) {
			float mean = 0;
			
			for (Pixel p : cc.pixels) {
				mean += map.pixels[p.y][p.x];
			}
			
			mean /= cc.pixels.size();
			
			for (Pixel p : cc.pixels) {
				map.pixels[p.y][p.x] = mean;
			}
			
			componentMap.put(cc, mean);
		}
	}

	@Override
	public FImage getSaliencyMap() {
		return map;
	}
	
	/**
	 * Get a map of component->saliency for all the components in
	 * the image
	 * @return component->saliency map
	 */
	public TObjectFloatHashMap<ConnectedComponent> getSaliencyComponents() {
		return componentMap;
	}
	
	public static void main(String [] args) throws MalformedURLException, IOException {
		MBFImage img = ImageUtilities.readMBF(new URL("http://ivrg.epfl.ch/supplementary_material/RK_CVPR09/Images/comparison/orig/0_5_5108.jpg"));
		
		YehSaliency sal = new YehSaliency();
		img.process(sal);
		
		DisplayUtilities.display(sal.getSaliencyMap());
	}
}
