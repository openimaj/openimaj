package org.openimaj.math.geometry.shape;

import static org.junit.Assert.*;

import org.junit.Test;
import org.openimaj.math.geometry.transforms.TransformUtilities;

import Jama.Matrix;

public class EllipseTest {
	@Test
	public void testCovariance(){
		Ellipse a = new Ellipse(1,1,20,10,Math.PI/2);
		Matrix covar = EllipseUtilities.ellipseToCovariance(a);
		Ellipse b = EllipseUtilities.ellipseFromCovariance(1, 1, covar, 1.0f);
		
		assertEquals(a.getCOG().getX(),b.getCOG().getX(),0.01f);
		assertEquals(a.getCOG().getY(),b.getCOG().getY(),0.01f);
		
		assertEquals(a.getMajor(),b.getMajor(),0.01f);
		assertEquals(a.getMinor(),b.getMinor(),0.01f);
		
		assertEquals(Math.sin(a.getRotation()),Math.sin(b.getRotation()),0.01f);
	}
	
	@Test
	public void testAffineTransform(){
		Ellipse a = new Ellipse(0,0,20,10,Math.PI/2);
		Matrix dble = TransformUtilities.scaleMatrix(2, 2);
		Ellipse b = a.transformAffine(dble);
		
		assertEquals(b.getMajor(),a.getMajor()*2,0.01f);
		assertEquals(b.getMinor(),a.getMinor()*2,0.01f);
	}
}
