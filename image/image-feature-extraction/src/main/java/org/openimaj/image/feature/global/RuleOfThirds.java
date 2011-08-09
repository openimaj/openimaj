package org.openimaj.image.feature.global;

import gnu.trove.TObjectFloatHashMap;
import gnu.trove.TObjectFloatProcedure;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.Image;
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
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class RuleOfThirds implements ImageProcessor<MBFImage>, FeatureVectorProvider<DoubleFV> {
	private static final double SIGMA = 0.17;
	
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
		final Point2dImpl [] powerPoints = getPowerPoints(image);
		
		image.process(saliencyGenerator);
		TObjectFloatHashMap<ConnectedComponent> componentMap = saliencyGenerator.getSaliencyComponents();
		
		asSum = 0;
		aseSum = 0;
		componentMap.forEachEntry(new TObjectFloatProcedure<ConnectedComponent>() {
			@Override
			public boolean execute(ConnectedComponent c, float s) {
				double as = c.calculateArea() * s;
			
				double D = closestDistance(c, powerPoints);
				
				asSum += as;
				aseSum += as * Math.exp(- (D*D) / (2 * SIGMA));

				return true;
			}
		});
	}

	private double closestDistance(ConnectedComponent cc, Point2dImpl[] powerPoints) {
		double centroid[] = cc.calculateCentroid();
		double minDistance = Double.MAX_VALUE;

		for (Point2dImpl pt : powerPoints) {
			double dx = (centroid[0] - pt.x);
			double dy = (centroid[1] - pt.y);
			double d =  dx*dx + dy*dy;
				
			if (d<minDistance)
				minDistance = d;
		}
		
		return Math.sqrt(minDistance);
	}
	
	private Point2dImpl[] getPowerPoints(MBFImage image) {
		int w = image.getWidth();
		int h = image.getHeight();
		
		return new Point2dImpl[] {
			new Point2dImpl(w/3f, h/3f),
			new Point2dImpl(2*w/3f, h/3f),
			new Point2dImpl(w/3f, 2*h/3f),
			new Point2dImpl(2*w/3f, 2*h/3f)
		};
	}
}
