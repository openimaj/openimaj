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
 * Example model that models the linear relationship between a set of integer ordinates.
 * Basically it calculates a straight line between a set of data points. This is
 * NOT the line of best fit however (i.e. we don't do least squares)... Only the first
 * two datapoint pairs are used to estimate the model.   
 * 
 * @author Jonathon Hare
 *
 */
public class SampleLinearModel implements Model<Integer, Integer> {
	float m, c;
	float tol;
	
	SampleLinearModel(float tolerence)
	{
		tol = tolerence;
	}
	
	@Override
	public SampleLinearModel clone() {
		SampleLinearModel slm = new SampleLinearModel(tol);
		slm.m = m;
		slm.c = c;
		return slm;
	}

	@Override
	public void estimate(List<? extends IndependentPair<Integer, Integer>> data) {
		float dy, dx;
		
		dy = data.get(0).secondObject() - data.get(1).secondObject();
		dx = data.get(0).firstObject() - data.get(1).firstObject();
			
		m = dy / dx;
		c = data.get(0).secondObject() - (m * data.get(0).firstObject());
	}

	@Override
	public int numItemsToEstimate() {
		return 2;
	}

	@Override
	public boolean validate(IndependentPair<Integer, Integer> data) {
		float y = (m * data.firstObject()) + c;
		
		if (Math.abs(y-data.secondObject()) < tol) return true;
		return false;
	}

	@Override
	public double calculateError(List<? extends IndependentPair<Integer, Integer>> alldata)
	{
		double error=0;
		
		for (IndependentPair<Integer, Integer> data : alldata) {
			double y = (m * data.firstObject()) + c;
			
			error += (y-data.secondObject())*(y-data.secondObject());
		}
		
		return error;
	}

	@Override
	public Integer predict(Integer data) {
		return (int)Math.round((m * data) + c);
	}
}
