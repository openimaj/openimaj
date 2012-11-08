package org.openimaj;

import java.util.LinkedList;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.convolution.FImageConvolveSeparable;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SlitScanVideoDemo {

	public static void main(String[] args) throws VideoCaptureException {
		VideoCapture capture = new VideoCapture(640,480,VideoCapture.getVideoDevices().get(0));
//		final int cacheSize = (int) (5 * capture.getFPS());
		final int cacheSize = 240;
		VideoDisplay<MBFImage> display = VideoDisplay.createVideoDisplay(capture);
		final float[] blurKern = FGaussianConvolve.makeKernel(0.5f);
		display.addVideoListener(new VideoDisplayListener<MBFImage>() {
			LinkedList<float[][][]> cache = new LinkedList<float[][][]>();
			@Override
			public void beforeUpdate(MBFImage frame) {
				addToCache(cache,frame);

				int height = frame.getHeight();
				float prop = (float)(cacheSize)/height;
				frame.fill(RGBColour.BLACK);
				float[][] framer = frame.getBand(0).pixels;
				float[][] frameg = frame.getBand(1).pixels;
				float[][] frameb = frame.getBand(2).pixels;
				for (int y = 0; y < height; y++) {
					int index = (int) (y * prop);
					if(index >= cache.size()){
						break;
					}
//					System.out.println("y = " + y);
//					System.out.println("index = " + index);
					float[][][] cacheImage = cache.get(index);
					System.arraycopy(cacheImage[0][y], 0, framer[y], 0, cacheImage[0][y].length);
					System.arraycopy(cacheImage[1][y], 0, frameg[y], 0, cacheImage[1][y].length);
					System.arraycopy(cacheImage[2][y], 0, frameb[y], 0, cacheImage[2][y].length);
				}
				for (FImage f : frame.bands) {
					FImageConvolveSeparable.convolveVertical(f, blurKern);
				}
				if(cache.size()>=cacheSize)
					cache.removeLast();
			}

			private void addToCache(LinkedList<float[][][]> cache, MBFImage frame) {
				MBFImage f = frame.clone();
				float[][][] entry = new float[3][][];
				entry[0] = f.getBand(0).pixels;
				entry[1] = f.getBand(1).pixels;
				entry[2] = f.getBand(2).pixels;
				cache.addFirst(entry);
			}

			@Override
			public void afterUpdate(VideoDisplay<MBFImage> display) {
				// TODO Auto-generated method stub

			}
		});
	}
}
