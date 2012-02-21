package org.openimaj.math.geometry.shape.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.PointList;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

/**
 * Implementation of shape alignment using Generalised Procrustes Analysis.
 * 
 * The alignment process is iterative and stops after a given number
 * of iterations or when the Procrustes Distance between the current mean and previous
 * reference shape is less than a threshold (i.e. the rate of change is small).
 * 
 * All shapes are aligned inline. The reference shape is optionally normalised
 * to a standardised scale and translated to the origin.
 *  
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 */
public class GeneralisedProcrustesAnalysis {
	
	private static final int DEFAULT_MAX_ITERS = 10;
	private boolean normalise;
	private float threshold;
	private int maxIters;

	/**
	 * Construct the {@link GeneralisedProcrustesAnalysis} with the given
	 * parameters. The initial reference shape can be optionally normalised
	 * by setting the first parameter to true. The alignment process is 
	 * iterative and stops after a given number of iterations (last parameter)
	 * or when the Procrustes Distance between the current mean and previous
	 * reference shape is less than a threshold (i.e. the rate of change is small) (second parameter).
	 * 
	 * @param normalise if true, then the reference is normalised (changing the reference shape itself).
	 * @param threshold the threshold on the Procrustes Distance at which to stop iterating
	 * @param maxIters the maximum number of iterations.
	 */
	public GeneralisedProcrustesAnalysis(boolean normalise, float threshold, int maxIters) {
		this.normalise = normalise;
		this.threshold = threshold;
		this.maxIters = maxIters;
	}

	/**
	 * Construct the {@link GeneralisedProcrustesAnalysis} with the given
	 * parameters. The initial reference shape can be optionally normalised
	 * by setting the first parameter to true. The alignment process is 
	 * iterative and stops after a default number of iterations (10)
	 * or when the Procrustes Distance between the current mean and previous
	 * reference shape is less than a threshold (i.e. the rate of change is small) (second parameter).
	 * 
	 * @param normalise if true, then the reference is normalised (changing the reference shape itself).
	 * @param threshold the threshold on the Procrustes Distance at which to stop iterating
	 */
	public GeneralisedProcrustesAnalysis(boolean normalise, float threshold) {
		this(normalise, threshold, DEFAULT_MAX_ITERS);
	}
	
	/**
	 * Construct the {@link GeneralisedProcrustesAnalysis} with the given
	 * parameters. The alignment process is 
	 * iterative and stops after a default number of iterations (10)
	 * or when the Procrustes Distance between the current mean and previous
	 * reference shape is less than a threshold (i.e. the rate of change is small) (first parameter).
	 * 
	 * @param threshold the threshold on the Procrustes Distance at which to stop iterating
	 */
	public GeneralisedProcrustesAnalysis(float threshold) {
		this(false, threshold, DEFAULT_MAX_ITERS);
	}
	
	/**
	 * Align the input shapes to the "mean" shape. All shapes are aligned inline.
	 * The mean shape is returned. 
	 * 
	 * @param shapes The shapes to align 
	 * @return the mean shape.
	 */
	public PointList align(List<PointList> shapes) {
		return alignPoints(shapes, normalise, threshold, maxIters);
	}
	
	/**
	 * Align the input shapes to the "mean" shape using Generalised Procrustes Analysis.
	 * The alignment process is iterative and stops after a given number
	 * of iterations or when the Procrustes Distance between the current mean and previous
	 * reference shape is less than a threshold (i.e. the rate of change is small).
	 * 
	 * All shapes are aligned inline. The reference shape is optionally normalised
	 * to a standardised scale and translated to the origin. The mean shape is returned.
	 * 
	 * @param inputShapes The shapes to align 
	 * @param normaliseReference if true, then the reference is normalised (changing
	 * 		the reference shape itself).
	 * @param threshold the threshold on the Procrustes Distance at which to stop iterating
	 * @param maxIters the maximum number of iterations.
	 * @return the mean shape
	 */
	public static PointList alignPoints(List<PointList> inputShapes, boolean normaliseReference, float threshold, int maxIters) {
		PointList reference = inputShapes.get(0); 
		
		List<PointList> workingShapes = new ArrayList<PointList>(inputShapes);	
		workingShapes.remove(reference);
		
		//Use a ProcrustesAnalysis to get the reference scaling and cog (normalised if required)
		ProcrustesAnalysis referencePa = new ProcrustesAnalysis(reference, normaliseReference);
		double referenceScaling = referencePa.scaling;
		Point2d referenceCog = referencePa.referenceCog;
		
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
		Point2d cog = mean.getCOG();
		Matrix trans = TransformUtilities.translateToPointMatrix(cog, referenceCog);
		mean.translate((float)trans.get(0,2), (float)trans.get(1,2));
				
		//normalise scaling to reference
		double scale = ProcrustesAnalysis.computeScale(mean, referenceCog.getX(), referenceCog.getY());
		float sf = (float)(scale / referenceScaling);
		mean.scale(referenceCog, sf);
		
		return mean;
	}
	
}
