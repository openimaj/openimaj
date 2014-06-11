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

import static org.junit.Assert.*;

import org.junit.Test;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

/**
 * Tests for {@link Ellipse}
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class EllipseTest {
	/**
	 * Test covariance matrices
	 */
	@Test
	public void testCovariance(){
		Ellipse a = new Ellipse(1,1,20,10,Math.PI/3.5);
		Matrix covar = EllipseUtilities.ellipseToCovariance(a);
		Ellipse b = EllipseUtilities.ellipseFromCovariance(1, 1, covar, 1.0f);
		
		assertEquals(a.calculateCentroid().getX(),b.calculateCentroid().getX(),0.01f);
		assertEquals(a.calculateCentroid().getY(),b.calculateCentroid().getY(),0.01f);
		
		assertEquals(a.getMajor(),b.getMajor(),0.01f);
		assertEquals(a.getMinor(),b.getMinor(),0.01f);
		
		assertEquals(Math.sin(a.getRotation()),Math.sin(b.getRotation()),0.01f);
	}
	
	/**
	 * Test affine transforms
	 */
	@Test
	public void testAffineTransform(){
		Ellipse a = new Ellipse(0,0,20,10,Math.PI/2);
		Matrix dble = TransformUtilities.scaleMatrix(2, 2);
		Ellipse b = a.transformAffine(dble);
		
		assertEquals(b.getMajor(),a.getMajor()*2,0.01f);
		assertEquals(b.getMinor(),a.getMinor()*2,0.01f);
		
		Matrix rotate = TransformUtilities.rotationMatrix(Math.PI/4);
		Ellipse c = a.transformAffine(rotate);
		
		assertEquals(c.getRotation() + ( Math.PI),a.getRotation()+Math.PI/4,0.01f);
//		assertEquals(c.getMinor(),a.getMinor()*2,0.01f);
	}
}
