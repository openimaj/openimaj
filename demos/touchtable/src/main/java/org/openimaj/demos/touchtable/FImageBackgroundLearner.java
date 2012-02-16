package org.openimaj.demos.touchtable;

import org.openimaj.image.FImage;
import org.openimaj.image.processor.ImageProcessor;

public class FImageBackgroundLearner implements ImageProcessor<FImage> {
	
	private FImage background;
	private int nImages = 0;
	private int toLearnWith;

	public FImageBackgroundLearner() {
		this.background = null;
		toLearnWith = 100;
	}
	
	public void relearn(){
		this.background = null;
		this.nImages = 0;
	}

	@Override
	public void processImage(FImage image) {
		if(this.background == null){
			background = image.clone().multiply(-1f);
			nImages  += 1;
			return;
		}
		
		for(int y = 0; y < image.height; y++){
			for(int x = 0; x < image.width; x++){
				double newSum = ( - background.pixels[y][x] * nImages) + image.pixels[y][x];
				background.pixels[y][x] = -(float)(newSum / (nImages+1));
			}
		}
		nImages++;
	}

	public boolean ready() {
		return nImages > toLearnWith;
	}

	public FImage getBackground() {
		return this.background;
	}

}
