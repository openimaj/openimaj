package org.openimaj.image.feature.local.interest;

import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.feature.local.interest.AbstractIPD.InterestPointData;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.shape.Ellipse;

/**
 * Visualise the interest points extracted using an {@link InterestPointDetector}. Allows for points and areas of interest to be drawn
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>, Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 * @param <T>
 * @param <Q>
 */
public class InterestPointVisualiser <T, Q extends Image<T,Q> & SinglebandImageProcessor.Processable<Float,FImage,Q>> {
	Q image;
	List<? extends InterestPointData> interestPoints;
	
	/**
	 * Image from which interest points were extract and the extracted points.
	 * @param image source image
	 * @param keys extracted interest points
	 */
	public InterestPointVisualiser(Q image, List<? extends InterestPointData> keys) {
		this.image = image;
		this.interestPoints = keys;
	}
	
	/**
	 * Draw the interest points, a central dot for in the pointCol and a bordered area of interest by borderCol.
	 * If either is null it is not drawn.
	 * 
	 * @param pointCol
	 * @param borderCol
	 * @return
	 */
	public Q drawPatches(T pointCol, T borderCol) {
		Q output = image.clone();
		
		for (InterestPointData k : interestPoints) {
			if(pointCol!=null){
				output.drawPoint(k, pointCol, 3);
			}
			if (borderCol != null) {
				float[] fpoints = new float[5];
				fpoints[0] = k.x;
				fpoints[1] = k.y;
				fpoints[2] = (float) k.getCovarianceMatrix().get(0,0);
				fpoints[3] = (float) k.getCovarianceMatrix().get(0,1);
				fpoints[4] = (float) k.getCovarianceMatrix().get(1,1);
				output.drawPolygon(Ellipse.ellipseFromHA(fpoints, (float) 5f), borderCol);
			}
		}
		
		return output;
	}
	
	public Q drawCenter(T col) {
		Q output = image.clone();
		output.drawPoints(interestPoints, col,2);
		return output;
	}

	public static MBFImage draw(MBFImage im, List<InterestPointData> ips, Float[] pointCol, Float[] borderCol) {
		InterestPointVisualiser<Float[], MBFImage> a = new InterestPointVisualiser<Float[],MBFImage >(im, ips);
		return a.drawPatches(pointCol, borderCol);
	}
}
