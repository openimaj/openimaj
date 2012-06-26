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
package org.openimaj.feature.local.matcher;

import java.util.List;
import java.util.Random;

import org.openimaj.image.FImage;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.util.pair.Pair;

import Jama.Matrix;

/**
 * Supports the generation of mild affine transforms. This is especially useful for the checking of localfeatures
 * which should show some resiliance to such transforms
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class KeypointCorrespondenceTestHelper{
	
	/**
	 * Generate an affine transform between some reasonable limits:
	 * - Rotate it between 0 and 360
	 * - transform it between 0 and 10 pixels
	 * - slant it (sheer in x and y) between 0 and 10 degrees
	 * - scale it between 0 and 2.0 times
	 * @param image the image on which to generate the transform
	 * @return a 3x3 transformation matrix
	 */
	public static Matrix generateMildTransform(FImage image){return generateMildTransform(image, new Random());}
	/**
	 * Generate an affine transform between some reasonable limits:
	 * - Rotate it between 0 and 360
	 * - transform it between 0 and 10 pixels
	 * - slant it (sheer in x and y) between 0 and 10 degrees
	 * - scale it between 0 and 2.0 times
	 * @param image the image on which to generate the transform
	 * @param seed the random seed against which to generate the random mild transforms
	 * @return a 3x3 transformation matrix
	 */
	public static Matrix generateMildTransform(FImage image,Random seed){
		double tenDegrees = Math.PI/2.0 * (1.0/(18.0));
//		double tenDegrees = 0;
		return generateRandomTransform(
				image.getWidth()/2.0,image.getHeight()/2.0,
				0.0,10.0,
				0,Math.PI*2,
				1.0,1.0,
				tenDegrees*3,tenDegrees*3,seed);
	}
	
	private static Matrix generateRandomTransform(
			double centerX, double centerY,
			double minTrans, double maxTrans,
			double minRot, double maxRot, 
			double minScale,double maxScale, 
			double maxSlantX, double maxSlantY,Random seed) {
		System.out.println("Generating random transform matrix");
		double transX = (seed.nextDouble() * (maxTrans - minTrans)) + minTrans;
		double transY = (seed.nextDouble() * (maxTrans - minTrans)) + minTrans;
		double rot = (seed.nextDouble() * (maxRot - minRot)) + minRot;
		double scale = (seed.nextDouble() * (maxScale - minScale)) + minScale;
		double slantX = (seed.nextDouble() * maxSlantX );
		double slantY = (seed.nextDouble() * maxSlantY );
		
		slantX = 1.0 - (slantX/(Math.PI/2));
		slantY = 1.0 - (slantY/(Math.PI/2));
		
		// The center coords take us to the origin
		// To place the rotated shape back to the center of the image we must find out it's "top left corner" after rotation
		// Find these points by rotating the unrotated extrema and then calculate the decenterX and Y
		double[][][] extrema = new double[][][]{
				{{0},{0},{1}},
				{{0},{centerY*2*scale*slantY},{1}},
				{{centerX*2*scale*slantX},{0},{1}},
				{{centerX*2*scale*slantX},{centerY*2*scale*slantY},{1}},
		};
		
		Matrix rotationMatrix = Matrix.constructWithCopy(new double[][]{
				{Math.cos(rot),-Math.sin(rot),0},
				{Math.sin(rot),Math.cos(rot),0},
				{0,0,1},
		});
		
		
		boolean unset = true;
		double minX=0,minY=0,maxX=0,maxY=0;
		for(double[][] ext : extrema){
			Matrix tmp = rotationMatrix.times(Matrix.constructWithCopy(ext));
			if(unset)
			{
				minX = maxX = tmp.get(0, 0);
				maxY = minY = tmp.get(1, 0);
				unset = false;
			}
			else{
				if(tmp.get(0, 0) > maxX) maxX = tmp.get(0, 0);
				if(tmp.get(1, 0) > maxY) maxY = tmp.get(1, 0);
				if(tmp.get(0, 0) < minX) minX = tmp.get(0, 0);
				if(tmp.get(1, 0) < minY) minY = tmp.get(1, 0);
			}
		}
		
		double deCenterX = (maxX - minX)/2.0;
		double deCenterY = (maxY - minY)/2.0;
		
		Matrix centerTranslationMatrix = Matrix.constructWithCopy(new double[][]{
				{1,0,-centerX*scale*slantX},
				{0,1,-centerY*scale*slantY},
				{0,0,1},
		});
		
		Matrix translationMatrix = Matrix.constructWithCopy(new double[][]{
				{1,0,transX*scale*slantX},
				{0,1,transY*scale*slantY},
				{0,0,1},
		});
		Matrix decenterTranslationMatrix = Matrix.constructWithCopy(new double[][]{
				{1,0,deCenterX},
				{0,1,deCenterY},
				{0,0,1},
		});
		Matrix scaleMatrix = Matrix.constructWithCopy(new double[][]{
				{scale,0,0},
				{0,scale,0},
				{0,0,1},
		});
		Matrix slantMatrix = Matrix.constructWithCopy(new double[][]{
				{slantX,0,0},
				{0,slantY,0},
				{0,0,1},
		});
		
		return Matrix.identity(3, 3)
				.times(translationMatrix)
				.times(decenterTranslationMatrix)
				.times(rotationMatrix)
				.times(centerTranslationMatrix)
				.times(slantMatrix)
				.times(scaleMatrix);
//		return centerTranslationMatrix.times(rotationMatrix).times(scaleMatrix).times(translationMatrix).times(decenterTranslationMatrix);
	}
	
	
	/**
	 * Provide simplistic check of correspondence between the keypoints extracted and a given transform. The euclidian
	 * distance between the expected position of a keypoint and it's actual position. Note this should not be used
	 * for consistency matching, please see the {@link LocalFeatureMatcher} for better implementations for that purpose.
	 * @param matches list of keypoint matches
	 * @param transform the supposed transform applied 
	 * @return lower numbers are lower euclidian distances
	 */
	public static float correspondance(List<Pair<Keypoint>> matches, Matrix transform){
		return correspondance(matches,transform,1.0f);
	}
	/**
	 * Provide simplistic check of correspondence between the keypoints extracted and a given transform. The euclidian
	 * distance between the expected position of a keypoint and it's actual position. Note this should not be used
	 * for consistency matching, please see the {@link LocalFeatureMatcher} for better implementations for that purpose.
	 * @param matches list of keypoint matches
	 * @param transform the supposed transform applied 
	 * @param scaleThreshold The allowable scale shift between two keypoints
	 * @return lower numbers are lower euclidian distances
	 */
	public static float correspondance(List<Pair<Keypoint>> matches, Matrix transform, float scaleThreshold){
		float total = 0.0f;
		for(Pair<Keypoint> pair : matches){
			
			Keypoint originalKeypoint = pair.firstObject();
			float currentScaleThreshold = scaleThreshold * originalKeypoint.scale;
			Keypoint transformedKeypoint = pair.secondObject();
			Matrix originalKPMatrix = keypointToMatrix(originalKeypoint);
			Matrix transformedKPMatrix = keypointToMatrix(transformedKeypoint );
			Matrix originalTransformed = transform.times(originalKPMatrix);
			
			
			if(distance(originalTransformed,transformedKPMatrix) < currentScaleThreshold){ total +=1f; }
		}
		return total;
	}
	private static Matrix keypointToMatrix(Keypoint kp) {
		return Matrix.constructWithCopy(
				new double[][]{
						{kp.x},
						{kp.y},
						{1.0}
			}
		);
	}
	private static float distance(Matrix a,Matrix b) {
		return (float) a.minus(b).norm2();
	}
}
