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
package org.openimaj.math.geometry.shape.util.polygon;

import java.awt.geom.Point2D;

/** */
public class EdgeNode
{
	/**
	 * Piggy-backed contour vertex
	 * data
	 */
	public Point2D.Double vertex = new Point2D.Double();
	
	/** Edge lower (x, y) coordinate */
	public Point2D.Double bot = new Point2D.Double(); 

	/** Edge upper (x, y) coordinate */
	public Point2D.Double top = new Point2D.Double();

	/** Scanbeam bottom x coordinate */
	public double xb;

	/** Scanbeam top x coordinate */
	public double xt;
	
	/** Change in x for a unit y increase */
	public double dx;
	 
	/** PolygonUtils / subject edge flag */
	public int type;

	/** Bundle edge flags */
	public int[][] bundle = new int[2][2];

	/** Bundle left / right indicators */
	public int[] bside = new int[2];

	/** Edge bundle state */
	public BundleState[] bstate = new BundleState[2];

	/** Output polygon / tristrip pointer */
	public PolygonNode[] outp = new PolygonNode[2];

	/** Previous edge in the AET */
	public EdgeNode prev;

	/** Next edge in the AET */
	public EdgeNode next;

	// EdgeNode pred; /* Edge connected at the lower end */
	
	/** Edge connected at the upper end */
	public EdgeNode succ;

	/** Pointer to next bound in LMT */
	public EdgeNode next_bound;
}
