package org.openimaj.image.feature.local.detector.pyramid;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.analysis.pyramid.Octave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramidOptions;
import org.openimaj.image.processor.SinglebandImageProcessor;

public class BasicOctaveGridFinder<
		OCTAVE extends Octave<?,?, IMAGE>, 
		IMAGE extends Image<?,IMAGE> & SinglebandImageProcessor.Processable<Float,FImage,IMAGE>>
	extends AbstractOctaveInterestPointFinder<OCTAVE, IMAGE>  
{
	int skipX = 50;
	int skipY = 50;
	int borderX = 5;
	int borderY = 5;
	int startScaleIndex = 1;
	int scaleSkip = 1;
	int stopScaleIndex = 2;
	
	@SuppressWarnings("unchecked")
	@Override
	public void process(OCTAVE octave) {
		this.octave = octave;
		int scales = 0;
		
		if (octave instanceof GaussianOctave) {
			scales = ((GaussianPyramidOptions<IMAGE>) octave.options).getScales();
		}
		
		IMAGE[] images = octave.images;
		int height = images[0].getHeight();
		int width = images[0].getWidth();
		
		//search through the scale-space images, leaving a border 
		for (currentScaleIndex = startScaleIndex; currentScaleIndex < stopScaleIndex; currentScaleIndex+=scaleSkip) {
			for (int y = borderY; y < height - borderY; y+=skipY) {
				for (int x = borderX; x < width - borderX; x+=skipX) {
					float octaveScale = currentScaleIndex;
					
					if (octave instanceof GaussianOctave) {
						octaveScale = ((GaussianPyramidOptions<IMAGE>) octave.options).getInitialSigma() * (float) Math.pow(2.0, currentScaleIndex / scales);
					}
					
					listener.foundInterestPoint(this, x, y, octaveScale);
				}
			}
		}
	}
}
