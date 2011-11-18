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
	public SpinningImageVideo(MBFImage carpet, float f) {
		this.wh = Math.sqrt(carpet.getWidth()*carpet.getWidth() + carpet.getHeight() * carpet.getHeight());
		canvas = carpet.newInstance((int)wh, (int)wh);
		image = carpet;
		startTime = System.currentTimeMillis();
		step = f;
		
		translate = TransformUtilities.translateToPointMatrix(carpet.getBounds().getCOG(), canvas.getBounds().getCOG());
	}

	@Override
	public MBFImage getNextFrame() {
		return getCurrentFrame();
	}

	@Override
	public MBFImage getCurrentFrame() {
		canvas.fill(RGBColour.BLACK);
		double timePerFrame = 1000 / getFPS();
		double frame = getTimeStamp() / timePerFrame;
		double angle = step * frame;
		MBFProjectionProcessor pp = new MBFProjectionProcessor();
		int midx = image.getWidth()/2;
		int midy = image.getHeight()/2;
		Matrix spin = TransformUtilities.rotationMatrixAboutPoint(angle, midx, midy);
		pp.setMatrix(translate.times(spin));
		pp.processImage(image);
		pp.performProjection(0, 0, canvas);
		return canvas;
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
		return true;
	}

	@Override
	public long countFrames() {
		return -1;
	}

	@Override
	public void reset() {
		this.startTime = System.currentTimeMillis();
	}

}
