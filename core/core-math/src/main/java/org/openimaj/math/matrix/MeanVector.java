package org.openimaj.math.matrix;

import no.uib.cipr.matrix.Vector;

/**
 * holds an updatable mean vector
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class MeanVector {
	
	private double[] mean;
	private int n;

	/**
	 * 
	 */
	public MeanVector() {
		this.mean = null;
	}
	
	public double[] update(double[] data){
		return update(data,true);
	}
	
	public double[] update(double[] data,boolean copy){
		data = data.clone();
		if(mean == null){
			mean = data;
			this.n = 1;
			return mean;
		}
		
		if(data.length != mean.length){
			throw new RuntimeException("Cannot update mean");
		}
		
		for (int i = 0; i < data.length; i++) {
			mean[i] = mean[i] + ((data[i] - mean[i])/(n+1));
		}
		this.n++;
		return mean;
	}

	public double[] vec() {
		return this.mean;
	}

}
