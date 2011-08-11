package org.openimaj.image.saliency;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.algorithm.HorizontalProjection;
import org.openimaj.image.processing.algorithm.VerticalProjection;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Extract the subject region of an image based on the
 * part that is least blurred (most in-focus).
 * 
 * Algorithm based on:
 * Yiwen Luo and Xiaoou Tang. 2008. 
 * Photo and Video Quality Evaluation: Focusing on the Subject. 
 * In Proceedings of the 10th European Conference on Computer Vision: 
 * Part III (ECCV '08), David Forsyth, Philip Torr, and Andrew Zisserman (Eds.). 
 * Springer-Verlag, Berlin, Heidelberg, 386-399. DOI=10.1007/978-3-540-88690-7_29 
 * http://dx.doi.org/10.1007/978-3-540-88690-7_29
 *
 * Note that this is not scale invariant - you will get different results with
 * different sized images...
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class LuoTangSubjectRegion implements SaliencyMapGenerator<FImage> {
	DepthOfFieldEstimator dofEstimator = new DepthOfFieldEstimator();
	
	Rectangle roi;
	private FImage dofMap;
	private float alpha = 0.9f;
	
	@Override
	public void processImage(FImage image, Image<?, ?>... otherimages) {
		image.processInline(dofEstimator);
		dofMap = dofEstimator.getSaliencyMap();
		
		for (int y=0; y<dofMap.height; y++) {
			for (int x=0; x<dofMap.width; x++) {
				if (dofMap.pixels[y][x] == 0) 
					dofMap.pixels[y][x] = 1;
				else 
					dofMap.pixels[y][x] = 0;
			}
		}
	}
	
	public Rectangle calculateROI() {		
		float [] pUx = HorizontalProjection.project(dofMap);
		float [] pUy = VerticalProjection.project(dofMap);
		
		float energy = 0;
		for (float f : pUx) energy += f;
		float thresh = energy * ((1 - alpha) / 2);
		
		int x1 = 0;
		float tmp = pUx[x1];
		while (tmp < thresh) {
			x1++;
			tmp += pUx[x1];
		}
		
		int y1 = 0;
		tmp = pUy[y1];
		while (tmp < thresh) {
			y1++;
			tmp += pUy[y1];
		}
		
		int x2 = pUx.length - 1;
		tmp = pUx[x2];
		while (tmp < thresh) {
			x2--;
			tmp += pUx[x2];
		}
		
		int y2 = pUy.length - 1;
		tmp = pUy[y2];
		while (tmp < thresh) {
			y2--;
			tmp += pUy[y2];
		}
		
		return new Rectangle(x1, y1, x2-x1, y2-y1);
	}

	@Override
	public FImage getSaliencyMap() {
		return dofMap;
	}
	
	public static void main(String [] args) throws MalformedURLException, IOException {
		FImage image = ImageUtilities.readF(new URL("http://farm5.static.flickr.com/4045/4202390037_aff1cb7627.jpg"));
		image = ResizeProcessor.halfSize(image);
		DisplayUtilities.display(image);
		
		LuoTangSubjectRegion re = new LuoTangSubjectRegion();
		image.processInline(re);
		
		
		Rectangle roi = re.calculateROI();
		System.out.println(roi);
		re.dofMap.drawShape(roi, 1f);
		
		DisplayUtilities.display(re.dofMap);
		
	}
}
