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

import org.openimaj.math.geometry.shape.Rectangle;

/**
 * A {@link SpacedCellGrid} with a notion of the scale which generated it 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ScaledSpacedCellGrid extends SpacedCellGrid{

	private double scale;

	/**
	 * Calls {@link SpacedCellGrid#SpacedCellGrid(Rectangle, int, double, double, double, double)} and saves the scale also
	 * @param bounds
	 * @param padding
	 * @param cellwidth
	 * @param cellheight
	 * @param dx
	 * @param dy
	 * @param scale
	 */
	public ScaledSpacedCellGrid(Rectangle bounds, int padding,double cellwidth, double cellheight, double dx, double dy, double scale) {
		super(bounds, padding, cellwidth, cellheight, dx, dy);
		this.setScale(scale);
	}

	/**
	 * @param scale
	 */
	public void setScale(double scale) {
		this.scale = scale;
	}

	/**
	 * @return the scale
	 */
	public double getScale() {
		return scale;
	}

}
