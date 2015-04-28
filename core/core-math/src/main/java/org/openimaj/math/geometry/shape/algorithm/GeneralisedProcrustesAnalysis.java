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
package org.openimaj.math.geometry.shape.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.PointList;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

/**
 * Implementation of shape alignment using Generalised Procrustes Analysis.
 * 
 * The alignment process is iterative and stops after a given number
 * of iterations or when the Procrustes Distance between the current mean and previous
 * reference shape is less than a threshold (i.e. the rate of change is small).
 * 
 * All shapes are aligned inplace. The reference shape is optionally normalised
 * to a standardised scale and translated to the origin.
 *  
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class GeneralisedProcrustesAnalysis {
	
	private static final int DEFAULT_MAX_ITERS = 10;
	private float threshold;
	private int maxIters;

	/**
	 * Construct the {@link GeneralisedProcrustesAnalysis} with the given
	 * parameters. The alignment process is 
	 * iterative and stops after a given number of iterations (last parameter)
	 * or when the Procrustes Distance between the current mean and previous
	 * reference shape is less than a threshold (i.e. the rate of change is small) (first parameter).
	 * 
	 * @param threshold the threshold on the Procrustes Distance at which to stop iterating
	 * @param maxIters the maximum number of iterations.
	 */
	public GeneralisedProcrustesAnalysis(float threshold, int maxIters) {
		this.threshold = threshold;
		this.maxIters = maxIters;
	}
	
	/**
	 * Construct the {@link GeneralisedProcrustesAnalysis} with the given
	 * parameters. The alignment process is 
	 * iterative and stops after a default number of iterations (10)
	 * or when the Procrustes Distance between the current mean and previous
	 * reference shape is less than a threshold (i.e. the rate of change is small)
	 * 
	 * @param threshold the threshold on the Procrustes Distance at which to stop iterating
	 */
	public GeneralisedProcrustesAnalysis(float threshold) {
		this(threshold, DEFAULT_MAX_ITERS);
	}
	
	/**
	 * Align the input shapes to the "mean" shape. All shapes are aligned inplace.
	 * The mean shape is returned. 
	 * 
	 * @param shapes The shapes to align 
	 * @return the mean shape.
	 */
	public PointList align(List<PointList> shapes) {
		return alignPoints(shapes, threshold, maxIters);
	}
	
	/**
	 * Align the input shapes to the "mean" shape using Generalised Procrustes Analysis.
	 * The alignment process is iterative and stops after a given number
	 * of iterations or when the Procrustes Distance between the current mean and previous
	 * reference shape is less than a threshold (i.e. the rate of change is small).
	 * 
	 * All shapes are aligned inplace. The reference shape is initially normalised
	 * to a standardised scale and translated to the origin. The mean shape is returned.
	 * 
	 * @param inputShapes The shapes to align
	 * @param threshold the threshold on the Procrustes Distance at which to stop iterating
	 * @param maxIters the maximum number of iterations.
	 * @return the mean shape
	 */
	public static PointList alignPoints(List<PointList> inputShapes, float threshold, int maxIters) {
		PointList reference = inputShapes.get(0); 
		
		List<PointList> workingShapes = new ArrayList<PointList>(inputShapes);	
		workingShapes.remove(reference);
		
		//Use a ProcrustesAnalysis to get the reference scaling and cog (normalised if required)
		ProcrustesAnalysis referencePa = new ProcrustesAnalysis(reference, true);
		double referenceScaling = referencePa.scaling;
		Point2d referenceCog = referencePa.referenceCog;
	
		if (workingShapes.size() == 0)
			return reference;
		
		PointList mean = alignPointsAndAverage(workingShapes, reference, referenceScaling, referenceCog);
		
		while (ProcrustesAnalysis.computeProcrustesDistance(reference, mean) > threshold && maxIters-- >= 0) {
			reference = mean;
			mean = alignPointsAndAverage(inputShapes, reference, referenceScaling, referenceCog);
		}
		
		return mean;
	}
	
	protected static PointList alignPointsAndAverage(List<PointList> shapes, PointList reference, double referenceScaling, Point2d referenceCog) {
		ProcrustesAnalysis pa = new ProcrustesAnalysis(reference);
		
		for (PointList shape : shapes) {
			pa.align(shape);
		}
		
		PointList mean = PointList.computeMean(shapes);
		
		//normalise translation to reference
		Point2d cog = mean.calculateCentroid();
		Matrix trans = TransformUtilities.translateToPointMatrix(cog, referenceCog);
		mean.translate((float)trans.get(0,2), (float)trans.get(1,2));
				
		//normalise scaling to reference
		double scale = ProcrustesAnalysis.computeScale(mean, referenceCog.getX(), referenceCog.getY());
		float sf = (float)(scale / referenceScaling);
		mean.scale(referenceCog, sf);
		
		return mean;
	}
	
}
