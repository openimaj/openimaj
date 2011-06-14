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
package org.openimaj.math.geometry.shape;

import org.openimaj.math.util.QuadraticEquation;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * An elliptical shape
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
public class EllipseUtilities {
	private static final long serialVersionUID = 1L;
	
	

	/***
	 * Construct an ellipse using a parametrics ellipse equation, namely:
	 * 
	 * X(t) = centerX + major * cos(t) * cos(rotation) - minor * sin(t) * sin(rotation)
	 * Y(t) = centerY + major * cos(t) * cos(rotation) + minor * sin(t) * sin(rotation)  
	 * 
	 * @param centerX
	 * @param centerY
	 * @param semi-major
	 * @param semi-minor
	 * @param rotation
	 */
	public static Ellipse ellipseFromEquation(double centerX, double centerY, double major, double minor, double rotation) {
		return new Ellipse(centerX,centerY,major,minor,rotation);
	}
	
	public static Ellipse ellipseFromSecondMoments(float x, float y,Matrix secondMoments) {
		return EllipseUtilities.ellipseFromSecondMoments(x, y, secondMoments,  1);
	}
	public static Ellipse ellipseFromSecondMoments(float x, float y,Matrix secondMoments,double scaleFactor) {
		double divFactor = 1/Math.sqrt(secondMoments.det());
		EigenvalueDecomposition rdr = secondMoments.times(divFactor).eig();		
		double d1,d2;
		if(rdr.getD().get(0,0) == 0)
			d1 = 0;
		else
			d1 = 1.0/Math.sqrt(rdr.getD().get(0,0));
//			d1 = Math.sqrt(rdr.getD().get(0,0));
		if(rdr.getD().get(1,1) == 0)
			d2 = 0;
		else
			d2 = 1.0/Math.sqrt(rdr.getD().get(1,1));
//			d2 = Math.sqrt(rdr.getD().get(1,1));
		
		double scaleCorrectedD1 = d1 * scaleFactor;
		double scaleCorrectedD2 = d2 * scaleFactor;
		
		Matrix eigenMatrix = rdr.getV();
		
		double rotation = Math.atan2(eigenMatrix.get(1,0),eigenMatrix.get(0,0));
		return ellipseFromEquation(x,y,scaleCorrectedD1,scaleCorrectedD2,rotation);
	}

	public static Ellipse ellipseFromCovariance(float x, float y, Matrix sm, float sf) {
		double xy = sm.get(1, 0);
		double xx = sm.get(0, 0);
		double yy = sm.get(1, 1);
		double theta = 0.5 * Math.atan2(2*xy, xx-yy);

		double trace = xx + yy;
		double det = (xx*yy) - (xy*xy);
		double [] eigval = QuadraticEquation.solveGeneralQuadratic(1, -trace, det);

		double a = Math.sqrt(eigval[1]) * sf * 2;
		double b = Math.sqrt(eigval[0]) * sf * 2;
		return ellipseFromEquation(x,y,a,b,theta);
	}	
}
