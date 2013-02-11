/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class TemplateMatcher implements ImageAnalyser<FImage> {
	/**
	 * Different algorithms for comparing templates to images.
	 *
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public enum Mode {
		/**
		 * Compute the score at a point as the sum-squared difference between the image
		 * and the template with the top-left at the given point. The {@link TemplateMatcher}
		 * will account for the offset to the centre of the template internally.
		 *
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		SUM_SQUARED_DIFFERENCE {
			@Override
			protected float computeMatchScore(final FImage image, final FImage template, final int x, final int y, final Object workingSpace) {
				final float[][] imageData = image.pixels;
				final float[][] templateData = template.pixels;

				return computeMatchScore(imageData, x, y, templateData, 0, 0, template.width, template.height);
			}

			@Override
			public final float computeMatchScore(final float[][] img, int x, int y, final float[][] template, final int templateX, final int templateY, final int templateWidth, final int templateHeight) {
				final int stopX1 = templateWidth + x;
				final int stopY1 = templateHeight + y;
				final int stopX2 = templateWidth + templateX;
				final int stopY2 = templateHeight + templateY;

				float score = 0;
				for (int yy1=y, yy2=templateY; yy1<stopY1 && yy2 < stopY2; yy1++, yy2++) {
					for (int xx1=x, xx2=templateX; xx1<stopX1 && xx2 < stopX2; xx1++, xx2++) {
						float diff = (img[yy1][xx1] - template[yy2][xx2]);

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
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
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
			public final float computeMatchScore(final float[][] img, final int x, final int y, final float[][] template, final int templateX, final int templateY, final int templateWidth, final int templateHeight) {
				final int stopX1 = templateWidth + x;
				final int stopY1 = templateHeight + y;
				final int stopX2 = templateWidth + templateX;
				final int stopY2 = templateHeight + templateY;

				float s1 = 0;
				float s2 = 0;
				float score = 0;

				for (int yy1=y, yy2=templateY; yy1<stopY1 && yy2 < stopY2; yy1++, yy2++) {
					for (int xx1=x, xx2=templateX; xx1<stopX1 && xx2 < stopX2; xx1++, xx2++) {
						float diff = (img[yy1][xx1] - template[yy2][xx2]);
						score += diff*diff;
						s1 += (img[yy1][xx1] * img[yy1][xx1]);
						s2 += (template[yy2][xx2] * template[yy2][xx2]);
					}
				}

				return (float) (score / Math.sqrt(s1*s2));
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
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		CORRELATION {
			@Override
			protected float computeMatchScore(final FImage image, final FImage template, final int x, final int y, final Object workingSpace) {
				final float[][] imageData = image.pixels;
				final float[][] templateData = template.pixels;

				return computeMatchScore(imageData, x, y, templateData, 0, 0, template.width, template.height);
			}

			@Override
			public float computeMatchScore(final float[][] img, final int x, final int y, final float[][] template, final int templateX, final int templateY, final int templateWidth, final int templateHeight) {
				float score = 0;

				final int stopX1 = templateWidth + x;
				final int stopY1 = templateHeight + y;
				final int stopX2 = templateWidth + templateX;
				final int stopY2 = templateHeight + templateY;

				for (int yy1=y, yy2=templateY; yy1<stopY1 && yy2 < stopY2; yy1++, yy2++) {
					for (int xx1=x, xx2=templateX; xx1<stopX1 && xx2 < stopX2; xx1++, xx2++) {
						float prod = (img[yy1][xx1] * template[yy2][xx2]);

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
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
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
			public float computeMatchScore(final float[][] img, final int x, final int y, final float[][] template, final int templateX, final int templateY, final int templateWidth, final int templateHeight) {
				float score = 0;
				float s1 = 0;
				float s2 = 0;

				final int stopX1 = templateWidth + x;
				final int stopY1 = templateHeight + y;
				final int stopX2 = templateWidth + templateX;
				final int stopY2 = templateHeight + templateY;

				int xx1,xx2,yy1,yy2;
				for (yy1=y, yy2=templateY; yy1<stopY1 && yy2 < stopY2; yy1++, yy2++) {
					for (xx1=x, xx2=templateX; xx1<stopX1 && xx2 < stopX2; xx1++, xx2++) {
						float prod = (img[yy1][xx1] * template[yy2][xx2]);

						s1 += (img[yy1][xx1] * img[yy1][xx1]);
						s2 += (template[yy2][xx2] * template[yy2][xx2]);

						score += prod;
					}
				}

				return (float) (score / Math.sqrt(s1*s2));
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
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		CORRELATION_COEFFICIENT {
			@Override
			protected final float computeMatchScore(final FImage image, final FImage template, final int x, final int y, final Object workingSpace) {
				final float[][] imageData = image.pixels;
				final float[][] templateData = template.pixels;

				final float templateMean = (Float)workingSpace;
				final float imgMean = MeanCenter.patchMean(imageData);

				return computeMatchScore(imageData, x, y, imgMean, templateData, 0, 0, template.width, template.height, templateMean);
			}

			@Override
			public final float computeMatchScore(final float[][] img, final int x, final int y, final float[][] template, final int templateX, final int templateY, final int templateWidth, final int templateHeight) {
				float imgMean = MeanCenter.patchMean(img, x, y, templateWidth, templateHeight);
				float templateMean = MeanCenter.patchMean(template, templateX, templateY, templateWidth, templateHeight);

				return computeMatchScore(img, x, y, imgMean, template, templateX, templateY, templateWidth, templateHeight, templateMean);
			}

			private final float computeMatchScore(final float[][] img, final int x, final int y, final float imgMean, final float[][] template, final int templateX, final int templateY, final int templateWidth, final int templateHeight, final float templateMean) {
				final int stopX1 = templateWidth + x;
				final int stopY1 = templateHeight + y;
				final int stopX2 = templateWidth + templateX;
				final int stopY2 = templateHeight + templateY;

				float score = 0;
				for (int yy1=y, yy2=templateY; yy1<stopY1 && yy2 < stopY2; yy1++, yy2++) {
					for (int xx1=x, xx2=templateX; xx1<stopX1 && xx2 < stopX2; xx1++, xx2++) {
						float prod = ((img[yy1][xx1] - imgMean) * (template[yy2][xx2] - templateMean));
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
			public Float prepareWorkingSpace(FImage template) {
				return MeanCenter.patchMean(template.pixels);
			}
		},
		/**
		 * Compute the normalised score at a point as the summed product between the mean-centered image patch
		 * and the mean-centered template with the top-left at the point given. The {@link TemplateMatcher}
		 * will account for the offset to the centre of the template internally.
		 *
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		NORM_CORRELATION_COEFFICIENT {
			@Override
			protected final float computeMatchScore(final FImage image, final FImage template, final int x, final int y, final Object workingSpace) {
				final int width = template.width;
				final int height = template.height;

				float imgMean = MeanCenter.patchMean(image.pixels, x, y, width, height);

				float score = 0;
				float si = 0;
				final float st = (Float)workingSpace;

				final float[][] imageData = image.pixels;
				final float[][] templateData = template.pixels;

				for (int j=0; j<height; j++) {
					for (int i=0; i<width; i++) {
						float ival = imageData[j+y][i+x] - imgMean;

						float prod = (ival * templateData[j][i]);

						score += prod;

						si += (ival * ival);
					}
				}

				double norm = Math.sqrt(si * st);

				if (norm == 0) return 0;

				return (float) (score / norm);
			}

			@Override
			public final float computeMatchScore(final float[][] img, final int x, final int y, final float[][] template, final int templateX, final int templateY, final int templateWidth, final int templateHeight) {
				float imgMean = MeanCenter.patchMean(img, x,y,templateWidth, templateHeight);
				float templateMean = MeanCenter.patchMean(template, templateX, templateY, templateWidth, templateHeight);

				final int stopX1 = templateWidth + x;
				final int stopY1 = templateHeight + y;
				final int stopX2 = templateWidth + templateX;
				final int stopY2 = templateHeight + templateY;

				float score = 0;
				float s1 = 0;
				float s2 = 0;

				for (int yy1=y, yy2=templateY; yy1<stopY1 && yy2 < stopY2; yy1++, yy2++) {
					for (int xx1=x, xx2=templateX; xx1<stopX1 && xx2 < stopX2; xx1++, xx2++) {
						float ival = (img[yy1][xx1] - imgMean);
						float tval = (template[yy2][xx2] - templateMean);

						float prod = (ival * tval);

						score += prod;

						s1 += (ival * ival);
						s2 += (tval * tval);
					}
				}

				double norm = Math.sqrt(s1*s2);

				if (norm == 0) return 0;

				return (float) (score / norm);
			}

			@Override
			public boolean scoresAscending() {
				return true; //bigger scores are better
			}

			@Override
			public FImage prepareTemplate(FImage template) {
				return template.process(new MeanCenter());
			}

			@Override
			public Float prepareWorkingSpace(FImage template) {
				float sumsq = 0;

				for (int y=0; y<template.height; y++)
					for (int x=0; x<template.width; x++)
						sumsq += template.pixels[y][x]*template.pixels[y][x];

				return sumsq;
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
		 * Compute the matching score between the image and template, with the top-left of the
		 * template at (x, y) in the image. The coordinates of the template can be specified,
		 * so it is possible for the actual template to be a sub-image of the given data.
		 *
		 * @param img the image data
		 * @param x the x-ordinate of the top-left of the template
		 * @param y the y-ordinate of the top-left of the template
		 * @param template the template data
		 * @param templateX the top-left x-ordinate of the template in the template data
		 * @param templateY the top-left y-ordinate of the template in the template data
		 * @param templateWidth the width of the template in the template data
		 * @param templateHeight the height of the template in the template data
		 * @return the match score.
		 */
		public abstract float computeMatchScore(final float[][] img, int x, int y, final float[][] template, final int templateX, final int templateY, final int templateWidth, final int templateHeight);

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
	private Mode mode;
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
	public TemplateMatcher(FImage template, Mode mode) {
		this.mode = mode;
		this.template = mode.prepareTemplate(template);
		this.workingSpace = mode.prepareWorkingSpace(this.template);
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
	public TemplateMatcher(FImage template, Mode mode, Rectangle bounds) {
		this.searchBounds = bounds;
		this.mode = mode;
		this.template = mode.prepareTemplate(template);
		this.workingSpace = mode.prepareWorkingSpace(this.template);
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

			float x = Math.max(searchBounds.x - halfWidth, 0);
			x = Math.min(x, image.width-template.width);
			float width = searchBounds.width;
			if (searchBounds.x - halfWidth < 0) {
				width += (searchBounds.x - halfWidth);
			}
			if (x + width > image.width - template.width)
				width += (image.width - template.width) - (x+width);

			float y = Math.max(searchBounds.y - halfHeight, 0);
			y = Math.min(y, image.height - template.height);
			float height = searchBounds.height;
			if (searchBounds.y - halfHeight < 0) {
				height += (searchBounds.y - halfHeight);
			}
			if (y + height > image.height - template.height)
				height += (image.height - template.height) - (y+height);

			searchSpace = new Rectangle(
					x,
					y,
					width,
					height
			);

		} else {
			searchSpace = new Rectangle(
					0,
					0,
					image.width - template.width + 1,
					image.height - template.height + 1
			);
		}

		final int scanX = (int) searchSpace.x;
		final int scanY = (int) searchSpace.y;
		final int scanWidth = (int)searchSpace.width;
		final int scanHeight = (int)searchSpace.height;

		responseMap = new FImage(scanWidth, scanHeight);
		final float[][] responseMapData = responseMap.pixels;

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

		return getBestResponses(numResponses, responseMap, getXOffset(), getYOffset(), comparator);
	}

	/**
	 * Get the top-N "best" responses found by the template matcher.
	 *
	 * @param numResponses The number of responses
	 * @param responseMap The response map
	 * @param offsetX the amount to shift pixels in the x direction
	 * @param offsetY the amount to shift pixels in the y direction
	 * @param comparator the comparator for determining the "best" responses
	 * @return the best responses found
	 */
	public static FValuePixel[] getBestResponses(int numResponses, FImage responseMap, int offsetX, int offsetY, Comparator<FValuePixel> comparator) {
		BoundedPriorityQueue<FValuePixel> bestResponses = new BoundedPriorityQueue<FValuePixel>(numResponses, comparator);

		final float[][] responseMapData = responseMap.pixels;

		final int scanWidth = responseMap.width;
		final int scanHeight = responseMap.height;

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
		final int halfWidth = template.width / 2;

		if (this.searchBounds == null)
			return halfWidth;
		else
			return (int) Math.max(searchBounds.x , halfWidth);
	}

	/**
	 * @return The y-offset of the top-left of the response map
	 * 		returned by {@link #getResponseMap()} to the original image
	 * 		analysed by {@link #analyseImage(FImage)}.
	 */
	public int getYOffset() {
		final int halfHeight = template.height / 2;

		if (this.searchBounds == null)
			return halfHeight;
		else
			return (int) Math.max(searchBounds.y , halfHeight);
	}

	/**
	 * @return The responseMap generated from the last call to {@link #analyseImage(FImage)}
	 */
	public FImage getResponseMap() {
		return responseMap;
	}

	/**
	 * @return the template held by the matcher; this might be different
	 * to the image used in construction as it might have been pre-processed.
	 */
	public FImage getTemplate() {
		return template;
	}

	/**
	 * Testing
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		FImage image = ImageUtilities.readF(new File("/Users/jsh2/Desktop/image.png"));
		FImage template = image.extractROI(100, 100, 100, 100);
		image.fill(0f);
		image.drawImage(template, 100, 100);

		TemplateMatcher matcher = new TemplateMatcher(template, Mode.CORRELATION);
		matcher.setSearchBounds(new Rectangle(100,100,200,200));
		image.analyseWith(matcher);
		DisplayUtilities.display(matcher.responseMap.normalise());

		MBFImage cimg = image.toRGB();
		for (FValuePixel p : matcher.getBestResponses(10)) {
			System.out.println(p);
			cimg.drawPoint(p, RGBColour.RED, 1);
		}

		cimg.drawShape(matcher.getSearchBounds(), RGBColour.BLUE);
		cimg.drawShape(new Rectangle(100,100,100,100), RGBColour.GREEN);

		DisplayUtilities.display(cimg);
	}
}
