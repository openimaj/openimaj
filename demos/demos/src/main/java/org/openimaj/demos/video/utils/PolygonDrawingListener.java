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
package org.openimaj.demos.video.utils;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Polygon;

/**
 * {@link MouseListener} that allows users to click
 * a series of points in an image representing the vertices
 * of a {@link Polygon}.
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class PolygonDrawingListener implements MouseListener {
	private Polygon polygon;

	/**
	 * Default constructor
	 */
	public PolygonDrawingListener() {
		this.polygon = new Polygon();
	}

	/**
	 * Reset the polygon.
	 */
	public void reset() {
		this.polygon = new Polygon();
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {
		this.polygon.getVertices().add(new Point2dImpl(arg0.getX(),arg0.getY()));
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {}

	/**
	 * @return the polygon created by the user
	 */
	public Polygon getPolygon() {
		return this.polygon;
	}

	/**
	 * Draw the polygon onto an image.
	 * @param image the image to draw on.
	 */
	public void drawPoints(MBFImage image) {
		Polygon p = getPolygon();
		MBFImageRenderer renderer = image.createRenderer();

		if(p.getVertices().size() > 2) {
			renderer.drawPolygon(p, 3,RGBColour.RED);
		}

		for(Point2d point : p.getVertices()) {
			renderer.drawPoint(point, RGBColour.BLUE, 5);
		}
	}
}
