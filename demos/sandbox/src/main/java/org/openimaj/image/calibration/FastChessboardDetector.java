package org.openimaj.image.calibration;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.contour.Contour;
import org.openimaj.image.contour.SuzukiContourProcessor;
import org.openimaj.image.processing.algorithm.FilterSupport;
import org.openimaj.image.processing.algorithm.MinMaxAnalyser;
import org.openimaj.image.typography.hershey.HersheyFont;
import org.openimaj.math.geometry.shape.RotatedRectangle;
import org.openimaj.util.pair.FloatIntPair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayAdapter;
import org.openimaj.video.capture.VideoCapture;

// does a fast check if a chessboard is in the input image. This is a workaround to 
// a problem of cvFindChessboardCorners being slow on images with no chessboard
// - src: input image
// - size: chessboard size
// Returns 1 if a chessboard can be in this image and findChessboardCorners should be called, 
// 0 if there is no chessboard, -1 in case of error
public class FastChessboardDetector implements ImageAnalyser<FImage> {
	static int erosion_count = 1;
	static float black_level = 20.f / 255f;
	static float white_level = 130.f / 255f;
	static float black_white_gap = 70.f / 255f;

	private int patternHeight;
	private int patternWidth;
	private boolean result;

	public FastChessboardDetector(int patternWidth, int patternHeight) {
		this.patternWidth = patternWidth;
		this.patternHeight = patternHeight;
	}

	private void quickThresh(FImage in, FImage out, float thresh, boolean inverse) {
		int low = 0;
		int high = 1;

		if (inverse) {
			low = 1;
			high = 0;
		}

		for (int y = 0; y < in.height; y++) {
			for (int x = 0; x < in.width; x++) {
				out.pixels[y][x] = in.pixels[y][x] > thresh ? low : high;
			}
		}
	}

	@Override
	public void analyseImage(FImage src) {
		final FImage thresh = new FImage(src.width, src.height);

		final MinMaxAnalyser mma = new MinMaxAnalyser(FilterSupport.BLOCK_3x3);
		src.analyseWith(mma);

		final FImage white = mma.min;
		final FImage black = mma.max;

		result = false;
		for (float thresh_level = black_level; thresh_level < white_level && !result; thresh_level += (20.0f / 255f))
		{
			final List<FloatIntPair> quads = new ArrayList<FloatIntPair>();

			quickThresh(white, thresh, thresh_level + black_white_gap, false);
			getQuadrangleHypotheses(SuzukiContourProcessor.findContours(thresh), quads, 1);

			quickThresh(black, thresh, thresh_level, true);
			getQuadrangleHypotheses(SuzukiContourProcessor.findContours(thresh), quads, 0);

			final int min_quads_count = patternWidth * patternHeight / 2;
			Collections.sort(quads, FloatIntPair.FIRST_ITEM_ASCENDING_COMPARATOR);

			// now check if there are many hypotheses with similar sizes
			// do this by floodfill-style algorithm
			final float size_rel_dev = 0.4f;

			for (int i = 0; i < quads.size(); i++)
			{
				int j = i + 1;
				for (; j < quads.size(); j++)
				{
					if (quads.get(j).first / quads.get(i).first > 1.0f + size_rel_dev)
					{
						break;
					}
				}

				if (j + 1 > min_quads_count + i)
				{
					// check the number of black and white squares
					final int[] counts = new int[2];
					countClasses(quads, i, j, counts);
					final int black_count = (int) Math.round(Math.ceil(patternWidth / 2.0)
							* Math.ceil(patternHeight / 2.0));
					final int white_count = (int) Math.round(Math.floor(patternWidth / 2.0)
							* Math.floor(patternHeight / 2.0));
					if (counts[0] < black_count * 0.75 ||
							counts[1] < white_count * 0.75)
					{
						continue;
					}
					result = true;
					break;
				}
			}
		}
	}

	void countClasses(List<FloatIntPair> pairs, int idx1, int idx2, int[] counts)
	{
		// counts.assign(2, 0);
		// counts[2] = 0; // why?
		for (int i = idx1; i != idx2; i++)
		{
			counts[pairs.get(i).second]++;
		}
	}

	void getQuadrangleHypotheses(Contour contours, List<FloatIntPair> quads, int class_id) {
		final float min_aspect_ratio = 0.3f;
		final float max_aspect_ratio = 3.0f;
		final float min_box_size = 10.0f;

		for (final Contour seq : contours.contourIterable()) {
			// can assume the the contour is simple, thus making convex hull
			// computation much faster
			final RotatedRectangle box = seq.minimumBoundingRectangle(true);

			final float box_size = Math.max(box.width, box.height);
			if (box_size < min_box_size)
			{
				continue;
			}

			final float aspect_ratio = box.width / Math.max(box.height, 1);
			if (aspect_ratio < min_aspect_ratio || aspect_ratio > max_aspect_ratio)
			{
				continue;
			}
			quads.add(new FloatIntPair(box_size, class_id));
		}
	}

	public boolean chessboardDetected() {
		return this.result;
	}

	public static void main(String[] args) throws MalformedURLException, IOException {
		final FastChessboardDetector fcd = new FastChessboardDetector(9, 6);
		final VideoDisplay<MBFImage> vd = VideoDisplay.createVideoDisplay(new VideoCapture(640,
				480));
		vd.setCalculateFPS(true);
		vd.addVideoListener(new VideoDisplayAdapter<MBFImage>() {
			@Override
			public void beforeUpdate(MBFImage frame) {
				fcd.analyseImage(frame.flatten());
				frame.drawText(fcd.result + "", 100, 100, HersheyFont.FUTURA_LIGHT,
						20, RGBColour.RED);
				System.out.println(vd.getDisplayFPS());
			}
		});
		// final FImage chessboard = ImageUtilities.readF(new
		// URL("http://docs.opencv.org/_images/fileListImageUnDist.jpg"));
		// fcd.analyseImage(chessboard);

	}
}
