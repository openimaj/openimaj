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
package org.openimaj.demos.sandbox.tld;

import org.openimaj.image.FImage;
import org.openimaj.image.analysis.algorithm.TemplateMatcher;
import org.openimaj.image.processing.algorithm.MeanCenter;
/**
 * Different algorithms for comparing templates to images. 
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public abstract class PatchMatcher {
	/**
	 * Compute the score at a point as the sum-squared difference between the image
	 * and the template with the top-left at the given point. The {@link TemplateMatcher}
	 * will account for the offset to the centre of the template internally.
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	public static class SUM_SQUARED_DIFFERENCE extends PatchMatcher{
		public float computeMatchScore(final float[][] img1, int x1, int y1, final float[][] img2, int x2, int y2, int w, int h){
			final int stopX1 = w + x1; 
			final int stopY1 = h + y1;
			final int stopX2 = w + x2; 
			final int stopY2 = h + y2;
			
			float score = 0;
			int xx1,xx2,yy1,yy2;
			for (yy1=y1, yy2=y2; yy1<stopY1 && yy2 < stopY2; yy1++, yy2++) {
				for (xx1=x1, xx2=x2; xx1<stopX1 && xx2 < stopX2; xx1++, xx2++) {
					float diff = (img1[yy1][xx1] - img2[yy2][xx2]);
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
	/**
	 * Compute the normalised score at a point as the sum-squared difference between the image
	 * and the template with the top-left at the given point. The {@link TemplateMatcher}
	 * will account for the offset to the centre of the template internally.
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	public static class NORM_SUM_SQUARED_DIFFERENCE extends PatchMatcher{
		@Override
		public float computeMatchScore(final float[][] img1, int x1, int y1, final float[][] img2, int x2, int y2, int w, int h){
			final int stopX1 = w + x1; 
			final int stopY1 = h + y1;
			final int stopX2 = w + x2; 
			final int stopY2 = h + y2;
			
			float s1 = 0;
			float s2 = 0;
			float score = 0;
			int xx1,xx2,yy1,yy2;
			for (yy1=y1, yy2=y2; yy1<stopY1 && yy2 < stopY2; yy1++, yy2++) {
				for (xx1=x1, xx2=x2; xx1<stopX1 && xx2 < stopX2; xx1++, xx2++) {
					float diff = (img1[yy1][xx1] - img2[yy2][xx2]);
					score += diff*diff;
					s1 += (img1[yy1][xx1] * img1[yy1][xx1]);
					s2 += (img2[yy2][xx2] * img2[yy2][xx2]);
				}
			}
			
			return (float) (score / Math.sqrt(s1*s2));
		}

		@Override
		public boolean scoresAscending() {
			return false; //smaller scores are better
		}
		
	}
	/**
	 * Compute the score at a point as the summed product between the image
	 * and the template with the top-left at the point given. The {@link TemplateMatcher}
	 * will account for the offset to the centre of the template internally.
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	public static class CORRELATION extends PatchMatcher{
		@Override
		public float computeMatchScore(final float[][] img1, int x1, int y1, final float[][] img2, int x2, int y2, int w, int h){
			float score = 0;
			
			final int stopX1 = w + x1;
			final int stopY1 = h + y1;
			final int stopX2 = w + x2;
			final int stopY2 = h + y2;
			
			int xx1,xx2,yy1,yy2;
			for (yy1=y1, yy2=y2; yy1<stopY1 && yy2 < stopY2; yy1++, yy2++) {
				for (xx1=x1, xx2=x2; xx1<stopX1 && xx2 < stopX2; xx1++, xx2++) {
					float prod = (img1[yy1][xx1] * img2[yy2][xx2]);
					score += prod;
				}
			}
			
			return score;
		}

		@Override
		public boolean scoresAscending() {
			return true; //bigger scores are better
		}
	}
	/**
	 * Compute the normalised score at a point as the summed product between the image
	 * and the template with the top-left at the point given. The {@link TemplateMatcher}
	 * will account for the offset to the centre of the template internally.
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	public static class NORM_CORRELATION extends PatchMatcher{
		@Override
		public float computeMatchScore(final float[][] img1, int x1, int y1, final float[][] img2, int x2, int y2, int w, int h){
//			float score = 0;
//			float si = 0;
//			final float st = (Float)workingSpace;
//			
//			final float[][] imageData = image.pixels;
//			final float[][] templateData = template.pixels;
//			
//			final int stopX = template.width + x;
//			final int stopY = template.height + y;
//			
//			for (int yy=y, j=0; yy<stopY; yy++, j++) {
//				for (int xx=x, i=0; xx<stopX; xx++, i++) {						
//					float prod = (imageData[yy][xx] * templateData[j][i]);
//					score += prod;
//					si += (imageData[yy][xx] * imageData[yy][xx]);
//				}
//			}
			float score = 0;
			float s1 = 0;
			float s2 = 0;
			final int stopX1 = w + x1;
			final int stopY1 = h + y1;
			final int stopX2 = w + x2;
			final int stopY2 = h + y2;
			
			int xx1,xx2,yy1,yy2;
			for (yy1=y1, yy2=y2; yy1<stopY1 && yy2 < stopY2; yy1++, yy2++) {
				for (xx1=x1, xx2=x2; xx1<stopX1 && xx2 < stopX2; xx1++, xx2++) {
					float prod = (img1[yy1][xx1] * img2[yy2][xx2]);
					s1 += (img1[yy1][xx1] * img1[yy1][xx1]);
					s2 += (img2[yy2][xx2] * img2[yy2][xx2]);
					score += prod;
				}
			}
			
			return (float) (score / Math.sqrt(s1*s2));
		}

		@Override
		public boolean scoresAscending() {
			return true; //bigger scores are better
		}
	}
	/**
	 * Compute the score at a point as the summed product between the mean-centered image patch
	 * and the mean-centered template with the top-left at the point given. The {@link TemplateMatcher}
	 * will account for the offset to the centre of the template internally.
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	public static class CORRELATION_COEFFICIENT extends PatchMatcher{
		
		@Override
		public float computeMatchScore(final float[][] img1, int x1, int y1, final float[][] img2, int x2, int y2, int w, int h){
			float mean1 = MeanCenter.patchMean(img1, x1,y1,w, h);
			float mean2 = MeanCenter.patchMean(img2, x2,y2,w, h);
			
			final int stopX1 = w + x1;
			final int stopY1 = h + y1;
			final int stopX2 = w + x2;
			final int stopY2 = h + y2;
			float score = 0;
			int xx1,xx2,yy1,yy2;
			for (yy1=y1, yy2=y2; yy1<stopY1 && yy2 < stopY2; yy1++, yy2++) {
				for (xx1=x1, xx2=x2; xx1<stopX1 && xx2 < stopX2; xx1++, xx2++) {
					float prod = ((img1[yy1][xx1]-mean1) * (img2[yy2][xx2]-mean2));
					score += prod;
				}
			}
			
			return score;
		}

		@Override
		public boolean scoresAscending() {
			return true; //bigger scores are better
		}
	}
	/**
	 * Compute the normalised score at a point as the summed product between the mean-centered image patch
	 * and the mean-centered template with the top-left at the point given. The {@link TemplateMatcher}
	 * will account for the offset to the centre of the template internally.
	 * 
	 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
	 */
	public static class NORM_CORRELATION_COEFFICIENT  extends PatchMatcher{
		
		@Override
		public float computeMatchScore(final float[][] img1, int x1, int y1, final float[][] img2, int x2, int y2, int w, int h){
			float mean1 = MeanCenter.patchMean(img1, x1,y1,w, h);
			float mean2 = MeanCenter.patchMean(img2, x2,y2,w, h);			
			final int stopX1 = w + x1;
			final int stopY1 = h + y1;
			final int stopX2 = w + x2;
			final int stopY2 = h + y2;
			float score = 0;
			float s1 = 0;
			float s2 = 0;
			int xx1,xx2,yy1,yy2;
			for (yy1=y1, yy2=y2; yy1<stopY1 && yy2 < stopY2; yy1++, yy2++) {
				for (xx1=x1, xx2=x2; xx1<stopX1 && xx2 < stopX2; xx1++, xx2++) {
					float prod = ((img1[yy1][xx1]-mean1) * (img2[yy2][xx2]-mean2));
					score += prod;
					s1 += (img1[yy1][xx1] * img1[yy1][xx1]);
					s2 += (img2[yy2][xx2] * img2[yy2][xx2]);
				}
			}
			
			return (float) (score / Math.sqrt(s1*s2));
		}

		@Override
		public boolean scoresAscending() {
			return true; //bigger scores are better
		}
	}
	
	/**
	 * Compute the matching score between two patches, with the top-left of the
	 * template at (x, y) in two images.
	 * @param image1 first image
	 * @param x1 first patch top left
	 * @param y1 first patch top left
	 * @param image2 second image
	 * @param x2 second patch top left
	 * @param y2 second patch top left
	 * @param width patch width
	 * @param height patch height 
	 * @return The match score. 
	 */
	public float computeMatchScore(final FImage image1, final int x1, final int y1, final FImage image2, final int x2, final int y2, int width, int height){
		return computeMatchScore(image1.pixels,x1,y1,image2.pixels,x2,y2,width,height);
	}
	
	/**
	 * Compute the matching score between two patches, with the top-left of the
	 * template at (x, y) in two images.
	 * @param image1 first image
	 * @param x1 first patch top left
	 * @param y1 first patch top left
	 * @param image2 second image
	 * @param x2 second patch top left
	 * @param y2 second patch top left
	 * @param width patch width
	 * @param height patch height 
	 * @return The match score. 
	 */
	public abstract float computeMatchScore(final float[][] image1, final int x1, final int y1, final float[][] image2, final int x2, final int y2, int width, int height);
	
	/**
	 * convenience function for {@link #computeMatchScore(FImage, int, int, FImage, int, int, int, int)} compares the
	 * template to a patch in an image
	 * @param image1
	 * @param x1
	 * @param y1
	 * @param template
	 * @return the match score
	 */
	public float computeMatchScore(final FImage image1, final int x1, final int y1, final FImage template){
		return computeMatchScore(image1, x1,y1,template,0,0,template.width,template.height);
	}
	
	/**
	 * Are the scores ascending (i.e. bigger is better) or descending (smaller is better)?
	 * @return true is bigger scores are better; false if smaller scores are better.
	 */
	public abstract boolean scoresAscending();
			
	
}

