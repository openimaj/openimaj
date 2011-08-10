package org.openimaj.image.feature.global;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import gnu.trove.TObjectFloatHashMap;
import gnu.trove.TObjectFloatProcedure;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processing.saliency.YehSaliency;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 * Implementation of the rule-of-thirds algorithm described in:
 * 
 * Che-Hua Yeh, Yuan-Chen Ho, Brian A. Barsky, Ming Ouhyoung.
 * Personalized photograph ranking and selection system.
 * In Proceedings of ACM Multimedia'2010. pp.211~220
 * 
 * I've assumed that the distances to the power-points should be 
 * normalized with respect to the image size - this isn't explicit
 * in the paper, but given that the sigma of the gaussian is fixed,
 * it seems likely...
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class RuleOfThirds implements ImageProcessor<MBFImage>, FeatureVectorProvider<DoubleFV> {
	private static final double SIGMA = 0.17;
	private static final Point2dImpl [] powerPoints = getPowerPoints();
	
	YehSaliency saliencyGenerator;
	private double asSum;
	private double aseSum;
	
	public RuleOfThirds() {
		saliencyGenerator = new YehSaliency();
	}
	
	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { aseSum / asSum });
	}

	@Override
	public void processImage(MBFImage image, Image<?, ?>... otherimages) {
		final int width = image.getWidth();
		final int height = image.getHeight();
				
		image.process(saliencyGenerator);
		TObjectFloatHashMap<ConnectedComponent> componentMap = saliencyGenerator.getSaliencyComponents();
		
		asSum = 0;
		aseSum = 0;
		componentMap.forEachEntry(new TObjectFloatProcedure<ConnectedComponent>() {
			@Override
			public boolean execute(ConnectedComponent c, float s) {
				double as = c.calculateArea() * s;
			
				double D = closestDistance(c, width, height);
				
				asSum += as;
				aseSum += as * Math.exp(- (D*D) / (2 * SIGMA));

				System.out.println(c.calculateArea() +"\t" + s + "\t" + D + "\t" + Math.exp(- (D*D) / (2 * SIGMA)));
				
				return true;
			}
		});
	}

	private double closestDistance(ConnectedComponent cc, int width, int height) {
		double centroid[] = cc.calculateCentroid();
		double minDistance = Double.MAX_VALUE;

		for (Point2dImpl pt : powerPoints) {
			double dx = (centroid[0] / width) - pt.x;
			double dy = (centroid[1] / width) - pt.y;
			double d =  dx*dx + dy*dy;
				
			if (d<minDistance)
				minDistance = d;
		}
		
		return Math.sqrt(minDistance);
	}
	
	private static Point2dImpl[] getPowerPoints() {
		return new Point2dImpl[] {
			new Point2dImpl(1/3f, 1/3f),
			new Point2dImpl(2/3f, 1/3f),
			new Point2dImpl(1/3f, 2/3f),
			new Point2dImpl(2/3f, 2/3f)
		};
	}
	
	public static void main(String [] args) throws MalformedURLException, IOException {
		RuleOfThirds s = new RuleOfThirds();
		MBFImage image = ImageUtilities.readMBF(new URL("http://farm1.static.flickr.com/8/9190606_8024996ff7.jpg"));
		image.process(s);
		System.out.println(s.getFeatureVector());
	}
}
