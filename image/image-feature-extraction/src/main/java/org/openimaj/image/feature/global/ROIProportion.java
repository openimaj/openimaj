package org.openimaj.image.feature.global;

import gnu.trove.TObjectFloatHashMap;
import gnu.trove.TObjectFloatProcedure;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.pixel.ConnectedComponent;
import org.openimaj.image.processor.ImageProcessor;
import org.openimaj.image.processor.connectedcomponent.render.BoundingBoxRenderer;
import org.openimaj.image.saliency.YehSaliency;
import org.openimaj.util.array.ArrayUtils;

/**
 * Implementation of the region of interest based image simplicity
 * measure described in:
 * 
 * Che-Hua Yeh, Yuan-Chen Ho, Brian A. Barsky, Ming Ouhyoung.
 * Personalized photograph ranking and selection system.
 * In Proceedings of ACM Multimedia'2010. pp.211~220
 * 
 * Basically returns the proportion of the image that can be considered
 * interesting.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class ROIProportion implements ImageProcessor<MBFImage>, FeatureVectorProvider<DoubleFV> {
	protected YehSaliency saliencyGenerator;
	protected float alpha = 0.67f;
	
	protected double roiProportion;
	
	public ROIProportion() { 
		saliencyGenerator = new YehSaliency();
	}
	
	public ROIProportion(float alpha) { 
		this();
		this.alpha = alpha;
	}
	
	public ROIProportion(float saliencySigma, float segmenterSigma, float k, int minSize, float alpha) {
		saliencyGenerator = new YehSaliency(saliencySigma, segmenterSigma, k, minSize);
		this.alpha = alpha;
	}

	@Override
	public DoubleFV getFeatureVector() {
		return new DoubleFV(new double[] { roiProportion });
	}

	@Override
	public void processImage(MBFImage image, Image<?, ?>... otherimages) {
		image.process(saliencyGenerator);
		TObjectFloatHashMap<ConnectedComponent> componentMap = saliencyGenerator.getSaliencyComponents();
		
		float max = ArrayUtils.maxValue(componentMap.getValues());
		
		final FImage map = new FImage(image.getWidth(), image.getHeight());
		final float thresh = max * alpha;
		final BoundingBoxRenderer<Float> renderer = new BoundingBoxRenderer<Float>(map, 1F, true);
				
		componentMap.forEachEntry(new TObjectFloatProcedure<ConnectedComponent>() {
			@Override
			public boolean execute(ConnectedComponent cc, float sal) {
				if (sal >= thresh) { //note that this is reversed from the paper, which doesn't seem to make sense.
					renderer.process(cc);
				}
				
				return true;
			}
		});
		
		roiProportion = 0;
		for (int y=0; y<map.height; y++)
			for (int x=0; x<map.width; x++)
				roiProportion += map.pixels[y][x];
	
		roiProportion /= (map.width * map.height); //smaller simplicity means smaller ROI
	}

	public static void main(String [] args) throws MalformedURLException, IOException {
		ROIProportion s = new ROIProportion();
		MBFImage image = ImageUtilities.readMBF(new URL("http://farm7.static.flickr.com/6016/6014546789_b83745c057.jpg"));
		image.process(s);
		System.out.println(s.getFeatureVector());
	}
}
