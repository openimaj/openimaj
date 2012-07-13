package org.openimaj.image.feature;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.image.Image;
import org.openimaj.image.analyser.ImageAnalyser;

/**
 * A {@link FeatureExtractor} that wraps {@link ImageAnalyser}s that
 * can provide {@link FeatureVector}s through {@link FeatureVectorProvider}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <ANALYSER> Type of ImageAnalyser 
 * @param <IMAGE> Type of image
 * @param <FEATURE> Type of feature
 */
public class ImageAnalyserFVFeatureExtractor<
	ANALYSER extends ImageAnalyser<IMAGE> & FeatureVectorProvider<FEATURE>,
	IMAGE extends Image<?, IMAGE>,
	FEATURE extends FeatureVector
> implements FeatureExtractor<FEATURE, IMAGE> 
{
	private ANALYSER analyser;
	
	/**
	 * Construct with the given analyser
	 * @param analyser the analyser
	 */
	public ImageAnalyserFVFeatureExtractor(ANALYSER analyser) {
		this.analyser = analyser;
	}
	
	@Override
	public synchronized FEATURE extractFeature(IMAGE object) {
		analyser.analyseImage(object);
		return analyser.getFeatureVector();
	}
}
