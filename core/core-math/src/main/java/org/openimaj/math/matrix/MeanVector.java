package org.openimaj.math.matrix;


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
	
	/**
	 * Update the mean. If no mean currently exists clone the
	 * data as the starting mean
	 * 
	 * @param data
	 * @return update 
	 */
	public double[] update(double[] data){
		return update(data,true);
	}
	
	/**
	 * Update the mean. If no mean currently exists copy controls
	 * where the data should be copied or not to initialise mean
	 * 
	 * @param data
	 * @param copy
	 * @return the new mean
	 */
	public double[] update(double[] data,boolean copy){
		if(copy) data = data.clone();
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

	/**
	 * @return current mean, may be null
	 */
	public double[] vec() {
		return this.mean;
	}

}
