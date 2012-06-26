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
package org.openimaj.math.model;

import java.util.List;

import org.openimaj.util.pair.IndependentPair;

/**
 * Model of mapping between pairs of integers learned from
 * a least-squares regression.
 * 
 * Basically this class learns the parameters m and c in
 * y = mx + c.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
public class LeastSquaresLinearModel implements Model<Integer, Integer> {
	private double c;
	private double m;
	private double tol;
	private int nEstimates = 10;
	
	/**
	 * Construct model with given tolerance
	 * @param tol tolerance
	 */
	public LeastSquaresLinearModel(double tol) {
		this.tol = tol;
		this.nEstimates = 2;
	}
	
	/**
	 * Construct model with given tolerance
	 * @param tol tolerance
	 * @param nEstimates minimum number of samples required for estimating model when fitting
	 */
	public LeastSquaresLinearModel(double tol, int nEstimates) {
		this.tol = tol;
		if (nEstimates < 2) nEstimates = 2;
		else this.nEstimates = nEstimates;
	}

	/***
	 * Using standard vertical linear regression as outlined here:
	 * http://mathworld.wolfram.com/LeastSquaresFitting.html
	 * 
	 * calculate the m and c of a line of best fit given the data.
	 * 
	 * @param data Observed data
	 * 
	 */
	@Override
	public void estimate(List<? extends IndependentPair<Integer, Integer>> data) {
		double sumXi = 0;
		double sumYi = 0;
		double sumXiXi = 0;
		double sumXiYi = 0;
		int n = 0;
		
		for(IndependentPair<Integer, Integer> pair : data){
			int xi = pair.firstObject();
			int yi = pair.secondObject();
			
			sumXi += xi;
			sumYi += yi;
			sumXiXi += (xi * xi);
			sumXiYi += xi * yi;
			
			n++;
		}
		
		c = (sumYi * sumXiXi - sumXi * sumXiYi) / (n * sumXiXi - sumXi * sumXi);
		m = (n * sumXiYi - sumXi * sumYi) / (n * sumXiXi - sumXi * sumXi);
	}

	@Override
	public boolean validate(IndependentPair<Integer, Integer> data) {
		double y = (m * data.firstObject()) + c;
		
		if (Math.abs(y-data.secondObject()) < tol) return true;
		return false;
	}

	@Override
	public Integer predict(Integer data) {
		return (int)Math.round((m * data) + c);
	}

	@Override
	public int numItemsToEstimate() {
		return nEstimates ;
	}

	@Override
	public double calculateError(List<? extends IndependentPair<Integer, Integer>> alldata) {
		double error=0;
		
		for (IndependentPair<Integer, Integer> data : alldata) {
			double y = (m * data.firstObject()) + c;
			
			error += (y-data.secondObject())*(y-data.secondObject());
		}
		
		return error;
	}
	
	@Override
	public LeastSquaresLinearModel clone() {
		LeastSquaresLinearModel model = new LeastSquaresLinearModel(tol,nEstimates);
		model.c = c;
		model.m = m;
		return model;
	}
	
	@Override
	public String toString(){
		return "Least Squares Fit: (m,c) = (" + m + "," + c + ")";
	}

	/**
	 * Get the gradient (m in y=mx+c)
	 * @return the gradient
	 */
	public double getM() {
		return m;
	}

	/**
	 * Get the offset (c in y=mx+c)
	 * @return the offset
	 */
	public double getC() {
		return c;
	}
}
