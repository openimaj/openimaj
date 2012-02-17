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
import org.openimaj.image.processing.algorithm.MeanCenter;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.queue.BoundedPriorityQueue;

/**
 * Basic template matching for {@link FImage}s. Template matching is
 * performed in the spatial domain.
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
		 * and the template with the top-left at the given point. The {@link TemplateMatcher}
		 * will account for the offset to the centre of the template internally.
		 * 
		 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
		 */
		SUM_SQUARED_DIFFERENCE {
			@Override
			protected float computeMatchScore(final FImage image, final FImage template, final int x, final int y, final Object workingSpace) {
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
		},
		/**
		 * Compute the normalised score at a point as the sum-squared difference between the image
		 * and the template with the top-left at the given point. The {@link TemplateMatcher}
		 * will account for the offset to the centre of the template internally.
		 * 
		 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
		 */
		NORM_SUM_SQUARED_DIFFERENCE {
			@Override
			protected float computeMatchScore(final FImage image, final FImage template, final int x, final int y, final Object workingSpace) {
				float score = 0;
				float si = 0;
				final float st = (Float)workingSpace;
				
				final float[][] imageData = image.pixels;
				final float[][] templateData = template.pixels;
				
				final int stopX = template.width + x;
				final int stopY = template.height + y;
				
				for (int yy=y, j=0; yy<stopY; yy++, j++) {
					for (int xx=x, i=0; xx<stopX; xx++, i++) {						
						float diff = (imageData[yy][xx] - templateData[j][i]);
						score += diff*diff;
						si += (imageData[yy][xx] * imageData[yy][xx]);
					}
				}
				
				return (float) (score / Math.sqrt(si*st));
			}

			@Override
			public boolean scoresAscending() {
				return false; //smaller scores are better
			}
			
			@Override
			public Float prepareWorkingSpace(FImage template) {
				float sumsq = 0;
				
				for (int y=0; y<template.height; y++) 
					for (int x=0; x<template.width; x++)
						sumsq += template.pixels[y][x]*template.pixels[y][x];
				
				return new Float(sumsq);
			}
		},
		/**
		 * Compute the score at a point as the summed product between the image
		 * and the template with the top-left at the point given. The {@link TemplateMatcher}
		 * will account for the offset to the centre of the template internally.
		 * 
		 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
		 */
		CORRELATION {
			@Override
			protected float computeMatchScore(final FImage image, final FImage template, final int x, final int y, final Object workingSpace) {
				float score = 0;
				
				final float[][] imageData = image.pixels;
				final float[][] templateData = template.pixels;
				
				final int stopX = template.width + x;
				final int stopY = template.height + y;
				
				for (int yy=y, j=0; yy<stopY; yy++, j++) {
					for (int xx=x, i=0; xx<stopX; xx++, i++) {
						float prod = (imageData[yy][xx] * templateData[j][i]);
						score += prod;
					}
				}
				
				return score;
			}

			@Override
			public boolean scoresAscending() {
				return true; //bigger scores are better
			}
		},
		/**
		 * Compute the normalised score at a point as the summed product between the image
		 * and the template with the top-left at the point given. The {@link TemplateMatcher}
		 * will account for the offset to the centre of the template internally.
		 * 
		 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
		 */
		NORM_CORRELATION {
			@Override
			protected float computeMatchScore(final FImage image, final FImage template, final int x, final int y, final Object workingSpace) {
				float score = 0;
				float si = 0;
				final float st = (Float)workingSpace;
				
				final float[][] imageData = image.pixels;
				final float[][] templateData = template.pixels;
				
				final int stopX = template.width + x;
				final int stopY = template.height + y;
				
				for (int yy=y, j=0; yy<stopY; yy++, j++) {
					for (int xx=x, i=0; xx<stopX; xx++, i++) {						
						float prod = (imageData[yy][xx] * templateData[j][i]);
						score += prod;
						si += (imageData[yy][xx] * imageData[yy][xx]);
					}
				}
				
				return (float) (score / Math.sqrt(si*st));
			}

			@Override
			public boolean scoresAscending() {
				return true; //bigger scores are better
			}
			
			@Override
			public Float prepareWorkingSpace(FImage template) {
				float sumsq = 0;
				
				for (int y=0; y<template.height; y++) 
					for (int x=0; x<template.width; x++)
						sumsq += template.pixels[y][x]*template.pixels[y][x];
				
				return new Float(sumsq);
			}
		},
		/**
		 * Compute the score at a point as the summed product between the mean-centered image patch
		 * and the mean-centered template with the top-left at the point given. The {@link TemplateMatcher}
		 * will account for the offset to the centre of the template internally.
		 * 
		 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
		 */
		CORRELATION_COEFFICIENT {
			private final MeanCenter mc = new MeanCenter();
			
			@Override
			protected float computeMatchScore(final FImage image, final FImage template, final int x, final int y, final Object workingSpace) {
				final int width = template.width;
				final int height = template.height;

				FImage subImage = image.extractROI(x, y, (FImage)workingSpace);
				subImage.processInline(mc);
				
				float score = 0;
				
				final float[][] imageData = subImage.pixels;
				final float[][] templateData = template.pixels;
				
				for (int j=0; j<height; j++) {
					for (int i=0; i<width; i++) {
						float prod = (imageData[j][i] * templateData[j][i]);
						score += prod;
					}
				}
				
				return score;
			}

			@Override
			public boolean scoresAscending() {
				return true; //bigger scores are better
			}
			
			@Override
			public FImage prepareTemplate(FImage template) {
				return template.process(mc);
			}
			
			@Override
			public FImage prepareWorkingSpace(FImage template) {
				return new FImage(template.width, template.height);
			}
		},
		/**
		 * Compute the normalised score at a point as the summed product between the mean-centered image patch
		 * and the mean-centered template with the top-left at the point given. The {@link TemplateMatcher}
		 * will account for the offset to the centre of the template internally.
		 * 
		 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
		 */
		NORM_CORRELATION_COEFFICIENT {
			private final MeanCenter mc = new MeanCenter();
			
			@Override
			protected float computeMatchScore(final FImage image, final FImage template, final int x, final int y, final Object workingSpace) {
				final int width = template.width;
				final int height = template.height;

				FImage subImage = image.extractROI(x, y, (FImage)((Object[])workingSpace)[0]);
				subImage.processInline(mc);
				
				float score = 0;
				float si = 0;
				final float st = (Float)((Object[])workingSpace)[1];
				
				final float[][] imageData = subImage.pixels;
				final float[][] templateData = template.pixels;
				
				for (int j=0; j<height; j++) {
					for (int i=0; i<width; i++) {
						float prod = (imageData[j][i] * templateData[j][i]);
						score += prod;
						si += (imageData[j][i] * imageData[j][i]);
					}
				}
				
				return (float) (score / Math.sqrt(si*st));
			}

			@Override
			public boolean scoresAscending() {
				return true; //bigger scores are better
			}
			
			@Override
			public FImage prepareTemplate(FImage template) {
				return template.process(mc);
			}
			
			@Override
			public Object[] prepareWorkingSpace(FImage template) {
				float sumsq = 0;
				
				for (int y=0; y<template.height; y++) 
					for (int x=0; x<template.width; x++)
						sumsq += template.pixels[y][x]*template.pixels[y][x];
				
				return new Object[] { new FImage(template.width, template.height), new Float(sumsq) };
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
		 * @param workingSpace The working space created by #prepareWorkingSpace() 
		 * @return The match score. 
		 */
		protected abstract float computeMatchScore(final FImage image, final FImage template, final int x, final int y, final Object workingSpace);
		
		/**
		 * Are the scores ascending (i.e. bigger is better) or descending (smaller is better)?
		 * @return true is bigger scores are better; false if smaller scores are better.
		 */
		public abstract boolean scoresAscending();
				
		/**
		 * Prepare the template if necessary. The default implementation 
		 * just returns the template, but subclasses can override.
		 * @param template the template image
		 * @return the processed template image
		 */
		protected FImage prepareTemplate(FImage template) {
			return template;
		}
		
		/**
		 * Prepare an object to hold the working space required by the
		 * mode during score computation. Most modes don't require
		 * this, so the default implementation returns null, but
		 * it can be overridden. 
		 * 
		 * @param template the template
		 * @return an object representing the required working space for
		 * 		the {@link #computeMatchScore(FImage, FImage, int, int, Object)} method.
		 */
		protected Object prepareWorkingSpace(FImage template) {
			return null;
		}
	}
	
	private FImage template;
	private TemplateMatcherMode mode;
	private Object workingSpace;
	private Rectangle searchBounds;
	private FImage responseMap;
		
	/**
	 * Default constructor with the template to match and the mode
	 * with which to estimate template responses. When matching is
	 * performed by {@link #analyseImage(FImage)}, the whole image
	 * will be searched.
	 * 
	 * @param template The template
	 * @param mode The mode.
	 */
	public TemplateMatcher(FImage template, TemplateMatcherMode mode) {
		this.mode = mode;
		this.template = mode.prepareTemplate(template);
		this.workingSpace = mode.prepareWorkingSpace(template);
	}
	
	/**
	 * Construct with the template to match, the mode with which to 
	 * estimate template responses and the bounds rectangle in which
	 * to search. The search bounds rectangle is defined with respect
	 * to the centre of the template.
	 * 
	 * @param template The template
	 * @param mode The mode.
	 * @param bounds The bounding box for search.
	 */
	public TemplateMatcher(FImage template, TemplateMatcherMode mode, Rectangle bounds) {
		this.mode = mode;
		this.template = mode.prepareTemplate(template);
		this.workingSpace = mode.prepareWorkingSpace(template);
	}
	
	/**
	 * @return the search bound rectangle
	 */
	public Rectangle getSearchBounds() {
		return searchBounds;
	}

	/**
	 * Set the search bounds rectangle. The search bounds rectangle 
	 * is defined with respect to the centre of the template.
	 * Setting to <code>null</code> results in the entire image
	 * being searched.
	 * 
	 * @param searchBounds the search bounds to set
	 */
	public void setSearchBounds(Rectangle searchBounds) {
		this.searchBounds = searchBounds;
	}

	/**
	 * Perform template matching. If a bounds rectangle
	 * is has not been set or is null, then the whole
	 * image will be searched. Otherwise the area of the image
	 * which lies in the previously set search bounds will be
	 * searched.
	 * 
	 * @see org.openimaj.image.analyser.ImageAnalyser#analyseImage(org.openimaj.image.Image)
	 */
	@Override
	public void analyseImage(FImage image) {
		Rectangle searchSpace = null;
		
		if (this.searchBounds != null) {
			final int halfWidth = template.width / 2;
			final int halfHeight = template.height / 2;
			
			searchSpace = new Rectangle(					
					Math.max(searchBounds.x - halfWidth, 0), 
					Math.max(searchBounds.y - halfHeight, 0), 
					Math.min(image.width - template.width, searchBounds.width), 
					Math.min(image.height - template.height, searchBounds.height)
			);
		} else {
			searchSpace = new Rectangle(
					0, 
					0, 
					image.width - template.width, 
					image.height - template.height
			);
		}
		
		responseMap = new FImage((int)searchSpace.width, (int)searchSpace.height);
		final float[][] responseMapData = responseMap.pixels;

		final int scanX = (int) searchSpace.x;
		final int scanY = (int) searchSpace.y;
		final int scanWidth = responseMap.width;
		final int scanHeight = responseMap.height;
		
		for (int y=0; y<scanHeight; y++) {
			for (int x=0; x<scanWidth; x++) {
				responseMapData[y][x] = mode.computeMatchScore(image, template, x+scanX, y+scanY, workingSpace);
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
		
		final int offsetX = getXOffset();
		final int offsetY = getYOffset();
		
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
	
	/**
	 * @return The x-offset of the top-left of the response map
	 * 		returned by {@link #getResponseMap()} to the original image
	 * 		analysed by {@link #analyseImage(FImage)}.
	 */
	public int getXOffset() {
		if (this.searchBounds == null)
			return template.width / 2;
		else 
			return (int) Math.max(this.searchBounds.x - (int)(template.width / 2), 0);
	}
	
	/**
	 * @return The y-offset of the top-left of the response map
	 * 		returned by {@link #getResponseMap()} to the original image
	 * 		analysed by {@link #analyseImage(FImage)}.
	 */
	public int getYOffset() {
		if (this.searchBounds == null)
			return template.height / 2;
		else 
			return (int) Math.max(this.searchBounds.y - (int)(template.height / 2), 0);
	}
	
	/**
	 * @return The responseMap generated from the last call to {@link #analyseImage(FImage)}
	 */
	public FImage getResponseMap() {
		return responseMap;
	}
	
	public static void main(String[] args) throws IOException {
		FImage image = ImageUtilities.readF(new File("/Users/jsh2/Desktop/image.png"));
		FImage template = ImageUtilities.readF(new File("/Users/jsh2/Desktop/template.png"));
		
		TemplateMatcher matcher = new TemplateMatcher(template, TemplateMatcherMode.NORM_SUM_SQUARED_DIFFERENCE);
		matcher.setSearchBounds(new Rectangle(500,150,75,75));
		image.analyseWith(matcher);
		DisplayUtilities.display(matcher.responseMap.normalise());
		
		MBFImage cimg = image.toRGB();
		for (FValuePixel p : matcher.getBestResponses(10)) {
			System.out.println(p);
			cimg.drawPoint(p, RGBColour.RED, 1);
		}
		
		cimg.drawShape(matcher.getSearchBounds(), RGBColour.BLUE);
		
		DisplayUtilities.display(cimg);
	}
}
