package org.openimaj.ml.linear.kernel;

import java.util.List;

import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Vector;

import org.openimaj.math.matrix.GramSchmidtProcess;
import org.openimaj.util.pair.IndependentPair;

import cern.colt.Arrays;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class LinearVectorKernel implements VectorKernel{

	@Override
	public Double apply(IndependentPair<double[], double[]> in) {
		double[] first = in.firstObject();
		double[] second = in.secondObject();
		return new DenseVector(first,false).dot(new DenseVector(second,false));
	}

	/**
	 * On the plane
	 * @param supports
	 * @param weights 
	 * @param bias
	 * @param setValues the dimentions of the point set to 0
	 * @return a point on the plane
	 */
	public static double[] getPlanePoint(List<double[]> supports, List<Double> weights, double bias, double ... setValues) {
		if(supports.size() == 0) throw new RuntimeException("Can't estimate plane point without supports");
		
		double[] w = getDirection(supports, weights);
		double[] x = new double[w.length];
		double resid = 0;
		int index = 0;
		for (int i = 0; i < w.length; i++) {
			
			if(Double.isNaN(setValues[i])){
				index = i;
			} else {
				resid += (setValues[i] * w[i]);
				x[i] = setValues[i];
			}
		}
		if(w[index] == 0) return new double[w.length];
		x[index] = (bias + resid)/ -w[index];
		return x;
	}

	/**
	 * @param supports
	 * @param weights 
	 * @return the vectors defining the plane
	 */
	public static Vector[] getPlaneDirections(List<double[]> supports, List<Double> weights) {
		double[] dir = getDirection(supports, weights);
		int ind = 0;
//		for (int i = 0; i < weights.size(); i++) {
//			double[] ds = supports.get(i);
//			System.out.println("Support " + ind++ + ": " + weights.get(i) + " * " +Arrays.toString(ds) );
//		}
//		System.out.println("Number of supports: " + supports.size() + " direction: " + Arrays.toString(dir));
		Vector[] all = GramSchmidtProcess.perform(dir);
		Vector[] ret = new Vector[all.length-1];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = all[i+1];
		}
		return ret;
	}
	

	public static double[] getDirection(List<double[]> supports, List<Double> weights) {
		Vector ret = null;
		for (int i = 0; i < supports.size(); i++) {
			double[] sup = supports.get(i);
			double weight = weights.get(i);
			DenseVector scale = new DenseVector(sup).scale(weight);
			if(ret == null){
				ret = scale;
			} else{
				ret.add(scale);
			}
		}
		double[] retdata = new DenseVector(ret).getData();
		return retdata;
	}

}
