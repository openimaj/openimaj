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
package org.openimaj.demos.sandbox.tld;

import java.util.Iterator;

import org.openimaj.math.geometry.shape.Rectangle;

/**
 * A spaced cell grid is given bounds, padding and a delta for x and y.
 * Using these values it provides an iterator which produces grid cells
 * that are within the bounds + padding and are spaced by deltax and deltay
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SpacedCellGrid extends Grid{
	
	private double dx;
	private double dy;
	public int ny;
	public int nx;
	private double startx;
	private double starty;
	/**
	 * between some bounds minus some padding create a set of cellwidth by cellheight cells which are dx apart
	 * in x and dy apart in y.
	 * @param bounds
	 * @param padding
	 * @param cellwidth
	 * @param cellheight
	 * @param dx
	 * @param dy
	 */
	public SpacedCellGrid(Rectangle bounds, int padding,double cellwidth, double cellheight,double dx, double dy) {
		this.cellwidth = cellwidth;
		this.cellheight = cellheight;
		this.dx = dx;
		this.dy = dy;
		
		double endx,endy;
		endx = bounds.getWidth()-1-padding-cellwidth;
		endy = bounds.getHeight()-1-padding-cellheight;
		startx = padding;
		starty = padding;
		
		nx = (int) Math.floor((endx - startx)/dx) + 1;
		ny = (int) Math.floor((endy - starty)/dy) + 1;
	}
	@Override
	public Iterator<double[]> iterator() {
		return new Iterator<double[]>(){
			double[] holder = new double[4];
			int i = 0;
			int j = 0;
			boolean done = false;
			@Override
			public boolean hasNext() {
				return !done;
			}

			@Override
			public double[] next() {
				holder[0] = Math.round(startx + i * dx);
				holder[1] = Math.round(starty + j * dy);
				holder[2] = holder[0] + cellwidth-1;
				holder[3] = holder[1] + cellheight-1;
				i++;
				if(i == nx){
					j++;
					if(j == ny) done = true;
					i = 0;
				}
				return holder;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	@Override
	public int size() {
		return this.nx *this.ny;
	}

}
