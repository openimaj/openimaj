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
package org.openimaj.math.geometry.line;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openimaj.math.geometry.line.Line2d.IntersectionResult;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;

/**
 * Test 2d lines
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author David Dupplaw (dpd@ecs.soton.ac.uk)
 *
 */
public class Line2dTest {
	private static final double RAD2DEG = 57.2957795;

	/**
	 * Test the intersection functionality
	 */
	@Test
	public void testIntersection() {
		Line2d line = new Line2d( new Point2dImpl(-2,0), new Point2dImpl(4,12) );
		
		IntersectionResult result = line.getIntersection( new Line2d( new Point2dImpl(-4,8), new Point2dImpl(4,0) ) );
		
		assertEquals(0, result.intersectionPoint.getX(), 0.001);
		assertEquals(4, result.intersectionPoint.getY(), 0.001);
	}
	
	/**
	 * Test the reflection functionality
	 */
	@Test
	public void testReflection() {
		Line2d line = new Line2d( new Point2dImpl(0,0), new Point2dImpl(8,4) );
		
		Point2d reflection = line.reflectAroundLine( new Point2dImpl( 1,2 ) );
		
		assertEquals(2.2, reflection.getX(), 0.001); 
		assertEquals(-0.4, reflection.getY(), 0.001);
	}
	
	/**
	 * Test angles between lines
	 */
	@Test
	public void testAngle() {
		Line2d line1 = new Line2d( new Point2dImpl(0,0), new Point2dImpl(4,4) );
		Line2d line2 = new Line2d( new Point2dImpl(0,0), new Point2dImpl(4,0) );
		Line2d line3 = new Line2d( new Point2dImpl(0,0), new Point2dImpl(0,4) );
		
		// Check the angle is accurately calculated within a degree
		assertEquals( 45, line1.calculateHorizontalAngle()*RAD2DEG, 1d );
		assertEquals(  0, line2.calculateHorizontalAngle()*RAD2DEG, 1d );
		assertEquals( 90, line3.calculateHorizontalAngle()*RAD2DEG, 1d );
	}
	
	/**
	 * Test point on line
	 */
	@Test
	public void testOnLine()
	{
		Line2d line1 = new Line2d( new Point2dImpl(0,0), new Point2dImpl(4,4) );
		assertTrue(  line1.isOnLine( new Point2dImpl(3,3), 0.1f ) );
		assertTrue(  line1.isInLine( new Point2dImpl(3,3), 0.1f ) );
		assertTrue(  line1.isOnLine( new Point2dImpl(8,8), 0.1f ) );
		assertTrue( !line1.isInLine( new Point2dImpl(8,8), 0.1f ) );
	}
	
	/**
	 * Test getting normal
	 */
	@Test
	public void testNormal()
	{
		// Horizontal Line
		Line2d line1 = new Line2d( new Point2dImpl(0,0), new Point2dImpl(10,0) );
		Line2d line1Norm = line1.getNormal();
		
		// Normal should be at 90 degrees to original line
		assertEquals( 90, Math.abs( line1.calculateHorizontalAngle() - 
				line1Norm.calculateHorizontalAngle() )*RAD2DEG, 1d );
		
		// Get the normal of the first line but going through a specific point
		Line2d line1Norm2 = line1.getNormal( new Point2dImpl( 10, 10 ) );
		System.out.println( line1Norm2 );
		
		// The normal should be at 90 degrees...
		assertEquals( 90, Math.abs( line1.calculateHorizontalAngle() - 
				line1Norm2.calculateHorizontalAngle() )*RAD2DEG, 1d );
		// .. and go through that given point
		assertTrue( line1Norm2.isOnLine( new Point2dImpl(10,10), 0.1f ) );
		
		// Line at some random angle
		Line2d line2 = new Line2d( new Point2dImpl(0,0), new Point2dImpl( 50,72 ) );
		Line2d line2Norm = line2.getNormal();
		
		// Normal should still be at 90 degrees to original line
		assertEquals( 90, Math.abs( line2.calculateHorizontalAngle() - 
				line2Norm.calculateHorizontalAngle() )*RAD2DEG, 1d );
		
	}
}

