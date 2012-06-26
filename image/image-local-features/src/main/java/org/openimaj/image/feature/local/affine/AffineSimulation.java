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
package org.openimaj.image.feature.local.affine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.convolution.FImageConvolveSeparable;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.ScaleSpacePoint;
import org.openimaj.math.geometry.transforms.TransformUtilities;


/**
 * Simulate affine rotations and tilts
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <Q> Type of interest point list
 * @param <T> Type of interest point
 * @param <I> Concrete subclass of {@link Image}
 * @param <P> Pixel type
 *
 */
public abstract class AffineSimulation<
		Q extends List<T>, 
		T extends ScaleSpacePoint, 
		I extends 
			Image<P,I> & 
			SinglebandImageProcessor.Processable<Float, FImage, I>, 
		P
	> {
	protected static final float PI = 3.141592654f;
	
	protected static final float InitSigma_aa = 1.6f;
	protected float BorderFact=6*(float)Math.sqrt(2);

	protected Q keys_all;
	
	public Map<AffineParams, Q> keys_all_mapped;
	public List<AffineParams> simulationOrder;

	public AffineSimulation() {
	}

	protected abstract Q findKeypoints(I image);
	protected abstract Q newList();
	
	/**
	 * Transform the coordinates of the keypoints to the original space 
	 * @param <Q> Type of interest point list
	 * @param <T> Type of interest point
	 * @param <I> Type of {@link Image}
	 * @param keys
	 * @param original
	 * @param Rtheta
	 * @param t1
	 * @param t2
	 */
	public static <Q extends List<T>, T extends ScaleSpacePoint, I extends Image<?,I>> void transformToOriginal(Q keys, I original, float Rtheta, float t1, float t2) {
		List<T> keys_to_remove = new ArrayList<T>();
		float x_ori, y_ori;

		if ( Rtheta <= PI/2 )
		{
			x_ori = 0;
			y_ori = (float) ((original.getWidth()) * Math.sin(Rtheta) / t1);
		}
		else
		{
			x_ori = (float) (-(original.getWidth()) * Math.cos(Rtheta) / t2);
			y_ori = (float) (( (original.getWidth()) * Math.sin(Rtheta) + (original.getHeight()) * Math.sin(Rtheta-PI/2) ) / t1);
		}

		float sin_Rtheta = (float) Math.sin(Rtheta);
		float cos_Rtheta = (float) Math.cos(Rtheta);

		for (T k : keys) {
			/* project the coordinates of im1 to original image before tilt-rotation transform */
			/* Get the coordinates with respect to the 'origin' of the original image before transform */
			k.setX(k.getX() - x_ori);
			k.setY(k.getY() - y_ori);
			/* Invert tilt */
			k.setX(k.getX() * t2);
			k.setY(k.getY() * t1);
			/* Invert rotation (Note that the y direction (vertical) is inverse to the usual 
			 * concention. Hence Rtheta instead of -Rtheta to inverse the rotation.) */
			float tx = cos_Rtheta*k.getX() - sin_Rtheta*k.getY();
			float ty = sin_Rtheta*k.getX() + cos_Rtheta*k.getY();
			
			k.setX(tx);
			k.setY(ty);
			
			if(tx <= 0 || ty <= 0 || tx >= original.getWidth() || ty >= original.getHeight()) {
				keys_to_remove.add(k);
			}
		}
		keys.removeAll(keys_to_remove);	
	}

	public static Point2d transformToOriginal(Point2d pt, FImage original, float theta, float t) {
		if (t==1)
			return pt;
		
		return transformToOriginal(pt, original, theta, t, 1);
	}
	
	protected static Point2d transformToOriginal(Point2d pt, FImage original, float Rtheta, float t1, float t2) {
		float x_ori, y_ori;
		Rtheta = Rtheta*PI/180;

		if ( Rtheta <= PI/2 )
		{
			x_ori = 0;
			y_ori = (float) ((original.width) * Math.sin(Rtheta) / t1);
		}
		else
		{
			x_ori = (float) (-(original.width) * Math.cos(Rtheta) / t2);
			y_ori = (float) (( (original.width) * Math.sin(Rtheta) + (original.height) * Math.sin(Rtheta-PI/2) ) / t1);
		}

		float sin_Rtheta = (float) Math.sin(Rtheta);
		float cos_Rtheta = (float) Math.cos(Rtheta);

		Point2d ptout = pt.copy();
		
		/* project the coordinates of im1 to original image before tilt-rotation transform */
		/* Get the coordinates with respect to the 'origin' of the original image before transform */
		ptout.setX(pt.getX() - x_ori);
		ptout.setY(pt.getY() - y_ori);
		/* Invert tilt */
		ptout.setX(ptout.getX() * t2);
		ptout.setY(ptout.getY() * t1);
		/* Invert rotation (Note that the y direction (vertical) is inverse to the usual 
		 * concention. Hence Rtheta instead of -Rtheta to inverse the rotation.) */
		float tx = cos_Rtheta*ptout.getX() - sin_Rtheta*ptout.getY();
		float ty = sin_Rtheta*ptout.getX() + cos_Rtheta*ptout.getY();

		ptout.setX(tx);
		ptout.setY(ty);
		
		return ptout;
	}
	
	public Q getKeypoints() {
		return keys_all;
	}

	public void process(I image, int num_of_tilts) {
		I image_tmp1;
		float t_k;
		int num_rot1=0;
		int counter_sim=0;

		/* keypoints are given by position, scale, orientation + decriptor */
		keys_all = newList();
		keys_all_mapped = new HashMap<AffineParams, Q>();
		simulationOrder = new ArrayList<AffineParams>();
		
		int num_rot_t2 = 10;

		float t_min = 1;
		t_k = (float)Math.sqrt(2);

		if ( num_of_tilts < 1)
		{
			throw new RuntimeException("Number of tilts num_tilt should be equal or larger than 1.");
		}

		image_tmp1 = image.clone();

		/* Calculate the number of simulations */
		for (int tt = 1; tt <= num_of_tilts; tt++)
		{
			float t = t_min * (float)Math.pow(t_k, tt-1);

			if ( t == 1 )
			{
				counter_sim ++;
			}
			else
			{
				num_rot1 = Math.round(num_rot_t2*t/2);        
				if ( num_rot1%2 == 1 )
				{
					num_rot1 = num_rot1 + 1;
				}
				num_rot1 = num_rot1 / 2;

				counter_sim +=  num_rot1;
			}         
		}

		//System.out.format("%d affine simulations will be performed. \n", num_sim);

		counter_sim = 0;		

		/* Affine simulation  */
		for (int tt = 1; tt <= num_of_tilts; tt++) {
			float t = t_min * (float)Math.pow(t_k, tt-1);
			AffineParams addedParams = null;
			if ( t == 1 ) {
				addedParams = new AffineParams(0, t);
				//lowe_sift_yu_nodouble_v1(image_tmp1,keypoints,display,verb);
				Q keypoints = findKeypoints(image_tmp1);				

				/* Store the number of keypoints */
				keys_all_mapped.put(addedParams, keypoints);
				
				/* Store the keypoints */
				keys_all.addAll(keypoints);
				simulationOrder.add(addedParams);
			}
			else
			{
				num_rot1 = Math.round(num_rot_t2*t/2);

				if ( num_rot1%2 == 1 )
				{
					num_rot1 = num_rot1 + 1;
				}
				num_rot1 = num_rot1 / 2;

				float delta_theta = PI / num_rot1;

				for (int rr = 1; rr <= num_rot1; rr++ ) 
				{
					float theta = delta_theta * (rr-1);

					image_tmp1 = transformImage(image, theta, t);
					
					//System.out.format("Rotation theta = %.2f, Tilt t = %.2f. \n", theta, t);
					
					Q keypoints = findKeypoints(image_tmp1);
					filterEdgesTransformed(keypoints, theta, image, 1.0f/t);
//					DisplayUtilities.display(new KeypointVisualizer(image_tmp1, keypoints).drawPatches(null,1.0f));

					transformToOriginal(keypoints, image, theta, t, 1);
//					DisplayUtilities.display(new KeypointVisualizer(image.clone(), keypoints).drawPatches(null,1.0f));
					
					addedParams = new AffineParams(theta, t);
					keys_all_mapped.put(addedParams, keypoints);					
					keys_all.addAll(keypoints);
					simulationOrder.add(addedParams);
				}
			}
		}
		//System.out.format("%d LR-ASIFT keypoints are detected. \n", keys_all.size());
	}
	
	public Q process(I image, AffineParams params) {
		return process(image, params.theta, params.tilt);
	}
	
	public Q process(I image, float theta, float tilt) {
		I image_tmp1 = transformImage(image, theta, tilt);
		
		//System.out.format("Rotation theta = %.2f, Tilt t = %.2f. \n", theta, t);
		
		Q keypoints = findKeypoints(image_tmp1);
		filterEdgesTransformed(keypoints, theta, image, 1.0f/tilt);
		// DisplayUtilities.display(new KeypointVisualizer<Float>(image_tmp1,(List<Keypoint>) keypoints).drawPatches(null,1.0f));

		transformToOriginal(keypoints, image, theta, tilt, 1);
		
		return keypoints;
	}
	
	public I transformImage(I image, float theta, float t) {
		float t1 = 1;
		float t2 = 1/t;
		
		/* Tilt */
		
		I image_rotated = ProjectionProcessor.project(image, TransformUtilities.rotationMatrix(-theta));
		
		/* Anti-aliasing filtering along vertical direction */
		float sigma_aa = InitSigma_aa * t / 2;
		image_rotated.processInplace(new FImageConvolveSeparable(null, FGaussianConvolve.makeKernel(sigma_aa)));
		
		/* Squash the image in the x and y direction by t1 and t2*/
		I image_tmp1 = ProjectionProcessor.project(image_rotated,TransformUtilities.scaleMatrix(t1,t2));
		return image_tmp1;
	}

	protected void filterEdgesTransformed(Q keypoints, float theta, I image, float t2) {
		float x1, y1, x2, y2, x3, y3, x4, y4;
		List<T> keys_to_remove = new ArrayList<T>(); 

		/* Store the keypoints */
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();
		for (int cc = 0; cc < keypoints.size(); cc++ )
		{		      
			/* check if the keypoint is located on the boundary of the parallelogram */
			/* coordinates of the keypoint */
			float x0 = keypoints.get(cc).getX();
			float y0 = keypoints.get(cc).getY();
			float scale1 = keypoints.get(cc).getScale();

			float sin_theta = (float) Math.sin(theta);
			float cos_theta1 = (float) Math.cos(theta);

			/* the coordinates of the 4 corners of the parallelogram */
			if ( theta <= PI/2.0 ) {
				/*			   theta1 = theta * PI / 180;*/
				x1 = imageHeight * sin_theta;
				y1 = 0;			 
				y2 = imageWidth * sin_theta;
				x3 = imageWidth * cos_theta1;
				x4 = 0;
				y4 = imageHeight * cos_theta1;
				x2 = x1 + x3;
				y3 = y2 + y4;

				/* Note that the vertical direction goes from top to bottom!!! 
				 * The calculation above assumes that the vertical direction goes 
				 * from the bottom to top. Thus the vertical coordinates need to 
				 * be reversed!!! */
				y1 = y3 - y1;
				y2 = y3 - y2;
				y4 = y3 - y4;
				y3 = 0;
			}
			else
			{
				/*   theta1 = theta * PI / 180;*/
				y1 = -imageHeight*cos_theta1;
				x2 = imageHeight*sin_theta;
				x3 = 0;
				y3 = imageWidth*sin_theta;
				x4 = -imageWidth*cos_theta1;
				y4 = 0;
				x1 = x2 + x4;
				y2 = y1 + y3;

				/* Note that the vertical direction goes from top to bottom!!! 
				 * The calculation above assumes that the vertical direction goes 
				 * from the bottom to top. Thus the vertical coordinates need to 
				 * be reversed!!! */
				y1 = y2 - y1;
				y3 = y2 - y3;
				y4 = y2 - y4;
				y2 = 0;
			}		       

			y1 = y1 * t2;
			y2 = y2 * t2;
			y3 = y3 * t2;
			y4 = y4 * t2;

			/* the distances from the keypoint to the 4 sides of the parallelogram */
			float d1 = (float) (Math.abs((x2-x1)*(y1-y0)-(x1-x0)*(y2-y1)) / Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1)));
			float d2 = (float) (Math.abs((x3-x2)*(y2-y0)-(x2-x0)*(y3-y2)) / Math.sqrt((x3-x2)*(x3-x2)+(y3-y2)*(y3-y2)));
			float d3 = (float) (Math.abs((x4-x3)*(y3-y0)-(x3-x0)*(y4-y3)) / Math.sqrt((x4-x3)*(x4-x3)+(y4-y3)*(y4-y3)));
			float d4 = (float) (Math.abs((x1-x4)*(y4-y0)-(x4-x0)*(y1-y4)) / Math.sqrt((x1-x4)*(x1-x4)+(y1-y4)*(y1-y4)));

			float BorderTh = BorderFact*scale1;
			if ((d1<BorderTh) || (d2<BorderTh) || (d3<BorderTh) || (d4<BorderTh) ) {
				keys_to_remove.add(keypoints.get(cc));
			}
		}
		keypoints.removeAll(keys_to_remove);
	}
		
	public Map<AffineParams, Q> getKeypointsMap() {
		return keys_all_mapped;
	}
}
