package org.openimaj.image.feature.global;

import gnu.trove.TObjectFloatHashMap;
import gnu.trove.TObjectFloatProcedure;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processing.saliency.YehSaliency;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.connectedcomponent.render.BoundingBoxRenderer;
import org.openimaj.util.array.ArrayUtils;

/**
 * Implementation of the region of interest based image simplicity
 * measure described in:
 * 
 * Che-Hua Yeh, Yuan-Chen Ho, Brian A. Barsky, Ming Ouhyoung. 
 * Personalized photograph ranking and selection system. 
 * In Proceedings of ACM Multimedia'2010. pp.211~220   
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ROISimplicity implements ImageProcessor<MBFImage>, FeatureVectorProvider<DoubleFV> {
	protected YehSaliency saliencyGenerator;
	protected float alpha = 0.67f;
	protected double simplicity;
	
	public ROISimplicity() { 
		saliencyGenerator = new YehSaliency();
	}
	
	public ROISimplicity(float alpha) { 
		this();
		this.alpha = alpha;
	}
	
	@Override
	public DoubleFV getFeatureVector() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processImage(MBFImage image, Image<?, ?>... otherimages) {
		image.process(saliencyGenerator);
		TObjectFloatHashMap<ConnectedComponent> componentMap = saliencyGenerator.getSaliencyComponents();
		
		float max = ArrayUtils.maxValue(componentMap.getValues());
		
		final FImage map = new FImage(image.getWidth(), image.getHeight());
		final float thresh = max * alpha;
		final BoundingBoxRenderer<Float> renderer = new BoundingBoxRenderer<Float>(map, 1F);
		
		componentMap.forEachEntry(new TObjectFloatProcedure<ConnectedComponent>() {
			@Override
			public boolean execute(ConnectedComponent cc, float sal) {
				if (sal < thresh) {
					renderer.process(cc);
				}
				
				return true;
			}
		});
		
		simplicity = 0;
		for (int y=0; y<map.height; y++)
			for (int x=0; x<map.width; x++)
				simplicity += map.pixels[y][x];
	}

}
