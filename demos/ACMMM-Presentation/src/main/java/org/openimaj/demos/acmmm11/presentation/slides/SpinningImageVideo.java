package org.openimaj.demos.acmmm11.presentation.slides;



import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.transform.MBFProjectionProcessor;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.video.Video;

import Jama.Matrix;

public class SpinningImageVideo extends Video<MBFImage> {

	private double wh;
	private MBFImage canvas;
	private long startTime;
	private float step;
	private MBFImage image;
	private Matrix translate;
	private boolean hasNextFrame = true;
	private float start;
	private MBFImage lastFrame = null;
	private float oldStep;
	private boolean isPaused;
	public SpinningImageVideo(MBFImage carpet, float start, float step) {
		this.wh = Math.sqrt(carpet.getWidth()*carpet.getWidth() + carpet.getHeight() * carpet.getHeight());
		canvas = carpet.newInstance((int)wh, (int)wh);
		lastFrame = canvas;
		image = carpet;
		startTime = System.currentTimeMillis();
		this.step = step;
		this.start = start;
		
		translate = TransformUtilities.translateToPointMatrix(carpet.getBounds().getCOG(), canvas.getBounds().getCOG());
	}

	@Override
	public MBFImage getNextFrame() {
		canvas.fill(RGBColour.BLACK);
		double timePerFrame = 1000 / getFPS();
		double frame = getTimeStamp() / timePerFrame;
		double angle = start + (step * frame);
		int midx = image.getWidth()/2;
		int midy = image.getHeight()/2;
		Matrix spin = TransformUtilities.rotationMatrixAboutPoint(angle, midx, midy);
		
		MBFProjectionProcessor pp = new MBFProjectionProcessor();
		pp.setMatrix(translate.times(spin));
		pp.processImage(image);
		pp.performProjection(0, 0, canvas);
		this.lastFrame  = canvas;
		return canvas;
	}

	@Override
	public MBFImage getCurrentFrame() {
		return lastFrame;
	}

	@Override
	public int getWidth() {
		return canvas.getWidth();
	}

	@Override
	public int getHeight() {
		return canvas.getHeight();
	}

	@Override
	public long getTimeStamp() {
		long ellapsed = System.currentTimeMillis() - startTime;
		return ellapsed;
	}

	@Override
	public double getFPS() {
		return 30;
	}

	@Override
	public boolean hasNextFrame() {
		return hasNextFrame ;
	}

	@Override
	public long countFrames() {
		return -1;
	}

	@Override
	public void reset() {
		this.startTime = System.currentTimeMillis();
	}

	public void stop() {
		this.hasNextFrame = true;
	}

	public void adjustSpeed(float f) {
		if(isPaused)return;
		double timePerFrame = 1000 / getFPS();
		double frame = getTimeStamp() / timePerFrame;
		double angle = start + (step * frame);
		this.start = (float) angle;
		this.startTime = System.currentTimeMillis();
		this.step +=f;
	}
	
	public void togglePause() {
		if(this.step!=0){
			this.oldStep = this.step;
			this.isPaused = true;
			double timePerFrame = 1000 / getFPS();
			double frame = getTimeStamp() / timePerFrame;
			double angle = start + (step * frame);
			this.start = (float) angle;
			this.step = 0;
			
		}
		else{
			this.isPaused = false;
			this.step = this.oldStep;
			this.startTime = System.currentTimeMillis();
		}
	}

}
