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

import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.util.QuadraticEquation;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * An elliptical shape
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
//FIXME: sanitise ellipse implementation
public class Ellipse extends Polygon {
	private static final long serialVersionUID = 1L;
	
	protected static float fmacheps;
	static {
		float tmp = 0.5F;
		while (1+tmp > 1) tmp /= 2;
		fmacheps = tmp;
	}

	/**
	 * Construct an ellipse from the parameterisation used by the tools
	 * developed at Oxford: a(x-u)(x-u)+2b(x-u)(y-v)+c(y-v)(y-v)=1
	 * 
	 * @param u x-centroid
	 * @param v y-centroid
	 * @param a a 
	 * @param b b
	 * @param c c
	 * @return the ellipse generated from the parameters
	 */
	public static Ellipse ellipseFromOxford(float u, float v, float a, float b, float c) {
		return ellipseFromOxford(u, v, a, b, c, 1f);
	}

	/**
	 * Construct an ellipse from the parameterisation used by the tools
	 * developed at Oxford: a(x-u)(x-u)+2b(x-u)(y-v)+c(y-v)(y-v)=1
	 * 
	 * @param u x-centroid
	 * @param v y-centroid
	 * @param a a 
	 * @param b b
	 * @param c c
	 * @param scalefactor scale to increase/decrease size
	 * @return the ellipse generated from the parameters
	 */
	public static Ellipse ellipseFromOxford(float u, float v, float a, float b, float c, float scalefactor) {
		//trace of [a b, b c]
		float trace = a + c;
		//det of [a b, b c]
		float det = (a*c) - (b*b);
		//eigenvalues of [a b, b c]
		float [] eigval = QuadraticEquation.solveGeneralQuadratic(1, -1*trace, det);

		//eigenvectors of [a b, b c]
		float eigv1 [] = new float[2];
		float eigv2 [] = new float[2];
		if (Math.abs(b) > fmacheps) {
			if (b > 0) {
				eigv1[0] = eigval[0] - c; eigv1[1] = b;
				eigv2[0] = eigval[1] - c; eigv2[1] = b;
			} else {
				eigv1[0] = b; eigv1[1] = eigval[0] - a;
				eigv2[0] = b; eigv2[1] = eigval[1] - a;
			}
		} else {
			eigv1[0] = 1; eigv1[1] = 0;
			eigv2[0] = 0; eigv2[1] = 1;
		}
		double v1 =  Math.sqrt((eigv1[0]*eigv1[0]) + (eigv1[1]*eigv1[1]));
		eigv1[0] /= v1;
		eigv1[1] /= v1;

		double v2 = Math.sqrt((eigv2[0]*eigv2[0]) + (eigv2[1]*eigv2[1]));
		eigv2[0] /= v2;
		eigv2[1] /= v2;

		double l2 = Math.sqrt(eigval[0]) * scalefactor;
		double l1 = Math.sqrt(eigval[1]) * scalefactor;

		double alpha = Math.atan2(eigv2[1], eigv2[0]);
		double cosa = Math.cos(alpha);
		double sina = Math.sin(alpha);

		Ellipse e = new Ellipse();
		for (double t=0; t<2*Math.PI; t+=Math.PI/5000) {
			double y =  l2 * Math.sin(t);
			double x = l1 * Math.cos(t);

			//int xbar = (int)Math.round((x*cosa + y*sina) + u);
			//int ybar = (int)Math.round((y*cosa - x*sina) + v);
			int ybar = (int)Math.round((x*cosa + y*sina) + v);
			int xbar = (int)Math.round((y*cosa - x*sina) + u);

			e.vertices.add(new Point2dImpl(xbar, ybar));
		}

		return e;
	}
	
	/**
	 * Construct an ellipse from the parameterisation used by vlfeat:
	 * [u00 u11 s00 s10 s11]
	 * 
	 * @param S the parameters [u00 u11 s00 s10 s11]
	 * @return the ellipse generated from the parameters
	 */
	public static Ellipse ellipseFromVLFeat(float [] S) {
		return ellipseFromVLFeat(S, 1);
	}

	/**
	 * Construct an ellipse from the parameterisation used by vlfeat:
	 * [u00 u11 s00 s10 s11]
	 * 
	 * @param S the parameters [u00 u11 s00 s10 s11]
	 * @param sf scale to increase/decrease the size of the ellipse
	 * @return the ellipse generated from the parameters
	 */
	public static Ellipse ellipseFromVLFeat(float [] S, float sf) {
		// number of vertices drawn for each frame
		int np = 400;

		float [] thr = linspace(0, (float) (2*Math.PI), np);

		// vertices around a unit circle
		float [][] Xp = new float[np][2];
		for (int i=0; i<np; i++) {
			Xp[i][0] = (float) Math.cos(thr[i]);
			Xp[i][1] = (float) Math.sin(thr[i]);
		}

		// frame center
		float xc = S[0];
		float yc = S[1];

		// frame matrix
		float [] A = mapFromS(S);

		Ellipse e = new Ellipse();
		// vertices along the boundary
		for (int i=0; i<np; i++) {
			float x = sf*A[0]*Xp[i][0] + sf*A[2]*Xp[i][1] + xc;
			float y = sf*A[1]*Xp[i][0] + sf*A[3]*Xp[i][1] + yc;

			e.vertices.add(new Point2dImpl(x,y));
		}

		return e;
	}
	
	/**
	 * Ellipse from affine code
	 * 
	 * @param S0 S0
	 * @param S1 S1
	 * @param S2 S2
	 * @param S3 S3
	 * @param S4 S4
	 * @param sf scale to increase/decrease the size of the ellipse
	 * @return the ellipse generated from the parameters
	 */
	public static Ellipse ellipseFromHA(float S0, float S1, float S2, float S3, float S4, float sf) {
		return ellipseFromHA(new float[] {S0, S1, S2, S3, S4}, sf);
	}
	
	/**
	 * Ellipse from affine code
	 * 
	 * @param S parameters
	 * @param sf scale to increase/decrease the size of the ellipse
	 * @return the ellipse generated from the parameters
	 */
	public static Ellipse ellipseFromHA(float [] S, float sf) {
		// number of vertices drawn for each frame
		int np = 400;

		float [] thr = linspace(0, (float) (2*Math.PI), np);

		// vertices around a unit circle
		float [][] Xp = new float[np][2];
		for (int i=0; i<np; i++) {
			Xp[i][0] = (float) Math.cos(thr[i]);
			Xp[i][1] = (float) Math.sin(thr[i]);
		}

		// frame center
		float xc = S[0];
		float yc = S[1];

		// frame matrix
		float [] A = new float[] {S[2], S[3], S[3], S[4]};

		Ellipse e = new Ellipse();
		// vertices along the boundary
		for (int i=0; i<np; i++) {
			float x = sf*A[0]*Xp[i][0] + sf*A[1]*Xp[i][1] + xc;
			float y = sf*A[2]*Xp[i][0] + sf*A[3]*Xp[i][1] + yc;

			e.vertices.add(new Point2dImpl(x,y));
		}

		return e;
	}

	private static float[] linspace(float d1, float d2, int n) {
		float [] arr = new float[n];
		float incr = (d2-d1) / (n-1f);

		for (int j=0; j<n-1; j++) {
			arr[j] = d1 + j*incr;
		}
		arr[n-1] = d2;

		return arr;
	}

	/**
	 * Convert the vlfeat covar params into the mapping of the unit circle
	 * x' inv(S) x = 1
	 * 
	 * @param S covariance params
	 * @return mapping
	 */
	protected static float [] mapFromS(float [] S) {
		float [] A = new float[4];
		float tmp = (float) (Math.sqrt(S[4]) + fmacheps);

		A[0] = (float) Math.sqrt(S[2]*S[4] - (S[3]*S[3])) / tmp;
		A[1] = 0;
		A[2] = S[3] / tmp ;
		A[3] = tmp ;

		return A;
	}

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
		Ellipse e = new Ellipse();
		
		double cosrot = Math.cos(rotation);
		double sinrot = Math.sin(rotation);
		
		for(double t = 0; t < Math.PI * 2; t+=Math.PI/360){
//			double yt = centerY + major * Math.cos(t) * cosrot + minor * Math.sin(t) * sinrot;
//			double xt = centerX + minor * Math.cos(t) * cosrot - major * Math.sin(t) * sinrot;
			double xt = centerX + (major * Math.cos(t) * cosrot - minor * Math.sin(t) * sinrot);
			double yt = centerY + (major * Math.cos(t) * sinrot + minor * Math.sin(t) * cosrot);
			
			
			e.vertices.add(new Point2dImpl((float)xt,(float)yt));
		}
		return e;
	}

	public static Ellipse ellipseFromSecondMoments(float x, float y,Matrix secondMoments,float scale) {
		double divFactor = 1/Math.sqrt(secondMoments.det());
		double scaleFctor = 4 * scale;
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
		
		double scaleCorrectedD1 = d1 * scaleFctor;
		double scaleCorrectedD2 = d2 * scaleFctor;
		
		Matrix eigenMatrix = rdr.getV();
		
		double rotation = Math.atan2(eigenMatrix.get(0,0),eigenMatrix.get(0,1));
		return ellipseFromEquation(x,y,scaleCorrectedD1,scaleCorrectedD2,rotation);
	}	
}
