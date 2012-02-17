package org.openimaj.image.analysis.algorithm;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.util.queue.BoundedPriorityQueue;

/**
 * Basic template matching for {@link FImage}s
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class TemplateMatcher implements ImageAnalyser<FImage> {
	/**
	 * Different algorithms for comparing templates to images. 
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	public enum TemplateMatcherMode {
		/**
		 * Compute the score at a point as the sum-squared difference between the image
		 * and the template with the top-left at that point. The {@link TemplateMatcher}
		 * will account for the offset to the centre of the template internally.
		 * 
		 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
		 */
		SUM_SQUARED_DIFFERENCE {
			@Override
			protected float computeMatchScore(FImage image, FImage template, final int x, final int y) {
				float score = 0;
				
				final float[][] imageData = image.pixels;
				final float[][] templateData = template.pixels;
				
				final int stopX = template.width + x;
				final int stopY = template.height + y;
				
				for (int yy=y, j=0; yy<stopY; yy++, j++) {
					for (int xx=x, i=0; xx<stopX; xx++, i++) {
						float diff = (imageData[yy][xx] - templateData[j][i]);
						score += diff*diff;
					}
				}
				
				return score;
			}

			@Override
			public boolean scoresAscending() {
				return false; //smaller scores are better
			}
			
		}
		;
		
		/**
		 * Compute the matching score between the image and template, with the top-left of the
		 * template at (x, y) in the image.
		 * @param image The image.
		 * @param template The template.
		 * @param x The x-ordinate top-left of the template in the image
		 * @param y The y-ordinate top-left of the template in the image
		 * @return The match score. 
		 */
		protected abstract float computeMatchScore(FImage image, FImage template, final int x, final int y);
		
		/**
		 * Are the scores ascending (i.e. bigger is better) or descending (smaller is better)?
		 * @return true is bigger scores are better; false if smaller scores are better.
		 */
		public abstract boolean scoresAscending();
	}
	
	protected FImage template;
	protected TemplateMatcherMode mode;
	protected FImage responseMap;
	
	/**
	 * Default constructor with the template to match and the mode
	 * with which to estimate template responses.
	 * @param template
	 * @param mode
	 */
	public TemplateMatcher(FImage template, TemplateMatcherMode mode) {
		this.template = template;
		this.mode = mode;
	}
	
	/* (non-Javadoc)
	 * @see org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		responseMap = new FImage(image.width - template.width, image.height - template.height);
		
		final float[][] responseMapData = responseMap.pixels;
		
		final int scanWidth = responseMap.width;
		final int scanHeight = responseMap.height;
		
		for (int y=0; y<scanHeight; y++) {
			for (int x=0; x<scanWidth; x++) {
				responseMapData[y][x] = mode.computeMatchScore(image, template, x, y);
			}
		}
	}
	
	/**
	 * Get the top-N "best" responses found by the template matcher.
	 * 
	 * @param numResponses The number of responses
	 * @return the best responses found
	 */
	public FValuePixel[] getBestResponses(int numResponses) {
		Comparator<FValuePixel> comparator = mode.scoresAscending() ? FValuePixel.ReverseValueComparator.INSTANCE : FValuePixel.ValueComparator.INSTANCE;
		
		BoundedPriorityQueue<FValuePixel> bestResponses = new BoundedPriorityQueue<FValuePixel>(numResponses, comparator);
		
		final float[][] responseMapData = responseMap.pixels;
		
		final int scanWidth = responseMap.width;
		final int scanHeight = responseMap.height;
		
		final int offsetX = template.width / 2;
		final int offsetY = template.height / 2;
		
		FValuePixel tmpPixel = new FValuePixel(0, 0, 0);
		for (int y=0; y<scanHeight; y++) {
			for (int x=0; x<scanWidth; x++) {
				tmpPixel.x = x + offsetX; //account for offset to centre
				tmpPixel.y = y + offsetY; //account for offset to centre
				tmpPixel.value = responseMapData[y][x];
				
				FValuePixel removed = bestResponses.offerItem(tmpPixel);
				
				if (removed == null) 
					tmpPixel = new FValuePixel(0, 0, 0);
				else
					tmpPixel = removed;
			}
		}
		
		return bestResponses.toOrderedArray(new FValuePixel[numResponses]);
	}
	
	public static void main(String[] args) throws IOException {
		FImage image = ImageUtilities.readF(new File("/Users/jsh2/Desktop/image.png"));
		FImage template = ImageUtilities.readF(new File("/Users/jsh2/Desktop/template.png"));
		
		TemplateMatcher matcher = new TemplateMatcher(template, TemplateMatcherMode.SUM_SQUARED_DIFFERENCE);
		image.analyse(matcher);
		DisplayUtilities.display(matcher.responseMap.normalise());
		
		
		MBFImage cimg = image.toRGB();
		for (FValuePixel p : matcher.getBestResponses(10)) {
			cimg.drawPoint(p, RGBColour.RED, 1);
		}
		
		DisplayUtilities.display(cimg);
	}
}
