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

import java.util.Comparator;

import org.openimaj.image.FImage;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.pixel.FValuePixel;
import org.openimaj.image.processing.algorithm.FourierCorrelation;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.util.FloatArrayStatsUtils;

/**
 * Basic template matching for {@link FImage}s. Template matching is
 * performed in the frequency domain using an FFT. 
 * <p>
 * The implementation is heavily inspired by the OpenCV code. 
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FourierTemplateMatcher implements ImageAnalyser<FImage> {
	/**
	 * Different algorithms for comparing templates to images. 
	 * 
	 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
	 */
	public enum Mode {
		/**
		 * Compute the score at a point as the sum-squared difference between the image
		 * and the template.
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		SUM_SQUARED_DIFFERENCE {
			@Override
			public boolean scoresAscending() {
				return false; //smaller scores are better
			}

			@Override
			public void processCorrelationMap(FImage img, FImage template, FImage corr) {
				SummedSqAreaTable sum = new SummedSqAreaTable();
				img.analyseWith(sum);
				
				float templateMean = FloatArrayStatsUtils.mean(template.pixels);
				float templateStdDev = FloatArrayStatsUtils.std(template.pixels);
				
				float templateNorm = templateStdDev * templateStdDev;		        
				float templateSum2 = templateNorm + templateMean * templateMean;

				templateNorm = templateSum2;
		        
			    double invArea = 1.0 / ((double)template.width * template.height);
		        templateSum2 /= invArea;
		        templateNorm = (float) Math.sqrt(templateNorm);
		        templateNorm /= Math.sqrt(invArea);
		        
		        final float[][] pix = corr.pixels;
		        
		        for( int y = 0; y < corr.height; y++ ) {
		            for( int x = 0; x < corr.width; x++ ) {
		                double num = pix[y][x];
		                double wndSum2 = 0;
		                
		                double t = sum.calculateSqSumArea(x, y, x+template.width, y+template.height);
		                wndSum2 += t;
		                    
		                num = wndSum2 - 2*num + templateSum2;

		                pix[y][x] = (float)num;
		            }
		        }
			}
		},
		/**
		 * Compute the normalised score at a point as the sum-squared difference between the image
		 * and the template. 
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		NORM_SUM_SQUARED_DIFFERENCE {
			@Override
			public boolean scoresAscending() {
				return false; //smaller scores are better
			}

			@Override
			public void processCorrelationMap(FImage img, FImage template, FImage corr) {
				SummedSqAreaTable sum = new SummedSqAreaTable();
				img.analyseWith(sum);
				
				float templateMean = FloatArrayStatsUtils.mean(template.pixels);
				float templateStdDev = FloatArrayStatsUtils.std(template.pixels);
				
				float templateNorm = templateStdDev * templateStdDev;		        
				float templateSum2 = templateNorm + templateMean * templateMean;
		        
				templateNorm = templateSum2;
				
			    double invArea = 1.0 / ((double)template.width * template.height);
		        templateSum2 /= invArea;
		        templateNorm = (float) Math.sqrt(templateNorm);
		        templateNorm /= Math.sqrt(invArea);
		        
		        final float[][] pix = corr.pixels;
		        
		        for( int y = 0; y < corr.height; y++ ) {
		            for( int x = 0; x < corr.width; x++ ) {
		                double num = pix[y][x];
		                double wndMean2 = 0, wndSum2 = 0;
		                
		                double t = sum.calculateSqSumArea(x, y, x+template.width, y+template.height);
		                wndSum2 += t;
		                    
		                num = wndSum2 - 2*num + templateSum2;

		                t = Math.sqrt( Math.max(wndSum2 - wndMean2, 0) ) * templateNorm;
		                num /= t;

		                pix[y][x] = (float)num;
		            }
		        }
			}
		},
		/**
		 * Compute the score at a point as the summed product between the image
		 * and the template.
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		CORRELATION {
			@Override
			public boolean scoresAscending() {
				return true; //bigger scores are better
			}

			@Override
			public void processCorrelationMap(FImage img, FImage template, FImage corr) {
				// Do nothing - image is already 
			}
		},
		/**
		 * Compute the normalised score at a point as the summed product between the image
		 * and the template. 
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		NORM_CORRELATION {
			@Override
			public boolean scoresAscending() {
				return true; //bigger scores are better
			}

			@Override
			public void processCorrelationMap(FImage img, FImage template, FImage corr) {
				SummedSqAreaTable sum = new SummedSqAreaTable();
				img.analyseWith(sum);
				
				float templateMean = FloatArrayStatsUtils.mean(template.pixels);
				float templateStdDev = FloatArrayStatsUtils.std(template.pixels);
				
				float templateNorm = templateStdDev * templateStdDev;		        
				templateNorm += templateMean * templateMean;

			    double invArea = 1.0 / ((double)template.width * template.height);
		        templateNorm = (float) Math.sqrt(templateNorm);
		        templateNorm /= Math.sqrt(invArea);
		        
		        final float[][] pix = corr.pixels;
		        
		        for( int y = 0; y < corr.height; y++ ) {
		            for( int x = 0; x < corr.width; x++ ) {
		                double num = pix[y][x];
		                double wndMean2 = 0, wndSum2 = 0;
		                
		                double t = sum.calculateSqSumArea(x, y, x+template.width, y+template.height);
		                wndSum2 += t;
		                    
		                t = Math.sqrt( Math.max(wndSum2 - wndMean2, 0) ) * templateNorm;
		                num /= t;

		                pix[y][x] = (float)num;
		            }
		        }
			}
		},
		/**
		 * Compute the score at a point as the summed product between the mean-centered image patch
		 * and the mean-centered template.
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		CORRELATION_COEFFICIENT {
			@Override
			public boolean scoresAscending() {
				return true; //bigger scores are better
			}

			@Override
			public void processCorrelationMap(FImage img, FImage template, FImage corr) {
				SummedAreaTable sum = new SummedAreaTable();
				img.analyseWith(sum);

				final float templateMean = FloatArrayStatsUtils.mean(template.pixels); //TODO: cache this
				final float[][] pix = corr.pixels;
				
				for( int y = 0; y < corr.height; y++ ) {
					for( int x = 0; x < corr.width; x++ ) {
						double num = pix[y][x];
						double t = sum.calculateArea(x, y, x+template.width, y+template.height);
						
						num -= t * templateMean;

						pix[y][x] = (float)num;
					}
				}
			}
		},
		/**
		 * Compute the normalised score at a point as the summed product between the mean-centered image patch
		 * and the mean-centered template.
		 * 
		 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
		 */
		NORM_CORRELATION_COEFFICIENT {
			@Override
			public boolean scoresAscending() {
				return true; //bigger scores are better
			}

			@Override
			public void processCorrelationMap(FImage img, FImage template, FImage corr) {
				SummedSqAreaTable sum = new SummedSqAreaTable();
				img.analyseWith(sum);
				
				float templateMean = FloatArrayStatsUtils.mean(template.pixels);
				float templateStdDev = FloatArrayStatsUtils.std(template.pixels);
				
				float templateNorm = templateStdDev;

		        if( templateNorm == 0 )
		        {
		            corr.fill(1);
		            return;
		        }
		        
			    double invArea = 1.0 / ((double)template.width * template.height);
		        templateNorm /= Math.sqrt(invArea);
		        
		        final float[][] pix = corr.pixels;
		        
		        for( int y = 0; y < corr.height; y++ ) {
		            for( int x = 0; x < corr.width; x++ ) {
		                double num = pix[y][x];
		                
		                double t = sum.calculateSumArea(x, y, x+template.width, y+template.height);
		                double wndMean2 = t * t * invArea;
						num -= t * templateMean;
						
						double wndSum2 = sum.calculateSqSumArea(x, y, x+template.width, y+template.height);

		                t = Math.sqrt( Math.max(wndSum2 - wndMean2, 0) ) * templateNorm;
		                num /= t;

		                pix[y][x] = (float)num;
		            }
		        }
			}
		}
		;

		/**
		 * Are the scores ascending (i.e. bigger is better) or descending (smaller is better)?
		 * @return true is bigger scores are better; false if smaller scores are better.
		 */
		public abstract boolean scoresAscending();

		/**
		 * Process the cross-correlation image to the contain the relevant output values for the
		 * chosen mode. 
		 * @param img the image
		 * @param template the template
		 * @param corr the cross correlation map produced by {@link FourierCorrelation}.
		 */
		public abstract void processCorrelationMap(FImage img, FImage template, FImage corr);
	}

	private FourierCorrelation correlation;
	private Mode mode;
	private Rectangle searchBounds;
	private FImage responseMap;
	private int templateWidth;
	private int templateHeight;

	/**
	 * Default constructor with the template to match. When matching is
	 * performed by {@link #analyseImage(FImage)}, the whole image
	 * will be searched.
	 * 
	 * @param template The template.
	 * @param mode The matching mode.
	 */
	public FourierTemplateMatcher(FImage template, Mode mode) {
		this.correlation = new FourierCorrelation(template);
		this.mode = mode;
		this.templateWidth = template.width;
		this.templateHeight = template.height;
	}

	/**
	 * Construct with the template to match and the bounds rectangle in which
	 * to search. The search bounds rectangle is defined with respect
	 * to the centre of the template.
	 * 
	 * @param template The template
	 * @param bounds The bounding box for search.
	 * @param mode The matching mode.
	 */
	public FourierTemplateMatcher(FImage template, Rectangle bounds, Mode mode) {
		this(template, mode);
		this.searchBounds = bounds;
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
		FImage subImage;

		if (this.searchBounds != null) {
			final int halfWidth = templateWidth / 2;
			final int halfHeight = templateHeight / 2;

			int x = (int) Math.max(searchBounds.x - halfWidth, 0);
			int width = (int) searchBounds.width + templateWidth;
			if (searchBounds.x - halfWidth < 0) {
				width += (searchBounds.x - halfWidth);
			}
			if (x + width > image.width)
				width = image.width;

			int y = (int) Math.max(searchBounds.y - halfHeight, 0);
			int height = (int) searchBounds.height + templateHeight;
			if (searchBounds.y - halfHeight < 0) {
				height += (searchBounds.y - halfHeight);
			}
			if (y + height > image.height)
				height = image.height;

			//FIXME: this is doing an additional copy; should be rolled into FFT data prep step in FourierTransform
			subImage = image.extractROI(
					x,
					y,
					width,
					height
			);
		} else {
			subImage = image.clone();
		}

		responseMap = subImage.process(correlation);
		responseMap.height = responseMap.height - correlation.template.height + 1;
		responseMap.width = responseMap.width - correlation.template.width + 1;

		mode.processCorrelationMap(subImage, correlation.template, responseMap);
	}

	/**
	 * Get the top-N "best" responses found by the template matcher.
	 * 
	 * @param numResponses The number of responses
	 * @return the best responses found
	 */
	public FValuePixel[] getBestResponses(int numResponses) {
		Comparator<FValuePixel> comparator = mode.scoresAscending() ? FValuePixel.ReverseValueComparator.INSTANCE : FValuePixel.ValueComparator.INSTANCE;
		
		return TemplateMatcher.getBestResponses(numResponses, responseMap, getXOffset(), getYOffset(), comparator);
	}

	/**
	 * @return The x-offset of the top-left of the response map
	 * 		returned by {@link #getResponseMap()} to the original image
	 * 		analysed by {@link #analyseImage(FImage)}.
	 */
	public int getXOffset() {
		final int halfWidth = templateWidth / 2;

		if (this.searchBounds == null)
			return halfWidth;
		else 
			return (int) Math.max(searchBounds.x - halfWidth, halfWidth);
	}

	/**
	 * @return The y-offset of the top-left of the response map
	 * 		returned by {@link #getResponseMap()} to the original image
	 * 		analysed by {@link #analyseImage(FImage)}.
	 */
	public int getYOffset() {
		final int halfHeight = templateHeight / 2;

		if (this.searchBounds == null)
			return halfHeight;
		else 
			return (int) Math.max(searchBounds.y - halfHeight, halfHeight);
	}

	/**
	 * @return The responseMap generated from the last call to {@link #analyseImage(FImage)}
	 */
	public FImage getResponseMap() {
		return responseMap;
	}
}
