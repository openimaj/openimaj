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
	 * @return a point on the plane
	 */
	public static Vector getPlanePoint(List<double[]> supports, List<Double> weights, double bias) {
		if(supports.size() == 0) throw new RuntimeException("Can't estimate plane point without supports");
		
		double[] w = new DenseVector(getPlaneDirections(supports, weights)[0]).getData();
		System.out.println(Arrays.toString(w));
		double[] x = new double[w.length];
		int index = 0;
		while(w[index] == 0)index++;
		x[index] = bias / w[index];
		return new DenseVector(x,false);
	}

	/**
	 * @param supports
	 * @param weights 
	 * @return the vectors defining the plane
	 */
	public static Vector[] getPlaneDirections(List<double[]> supports, List<Double> weights) {
		double[] dir = getDirection(supports, weights);
		int ind = 0;
		for (int i = 0; i < weights.size(); i++) {
			double[] ds = supports.get(i);
			System.out.println("Support " + ind++ + ": " + weights.get(i) + " * " +Arrays.toString(ds) );
		}
		System.out.println("Number of supports: " + supports.size() + " direction: " + Arrays.toString(dir));
		Vector[] all = GramSchmidtProcess.perform(dir);
		Vector[] ret = new Vector[all.length-1];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = all[i+1];
		}
		return ret;
	}
	

	private static double[] getDirection(List<double[]> supports, List<Double> weights) {
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
