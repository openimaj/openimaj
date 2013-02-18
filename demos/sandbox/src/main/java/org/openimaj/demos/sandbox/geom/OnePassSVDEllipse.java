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
package org.openimaj.demos.sandbox.geom;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.matrix.MatrixUtils;

import Jama.Matrix;

public class OnePassSVDEllipse implements MouseListener, MouseMotionListener {
	private static final double DAMPENING = 0.98;
	Matrix init = new Matrix(2,10);
	Matrix point = new Matrix(2,1);
	double seen = 0;
	private Matrix mean;
	private IncrementalSVD svd;
	private MBFImage image;
	private JFrame disp;
	private boolean dragClick;
	private Ellipse prevEllipse;
	private boolean initMode = true;
	public OnePassSVDEllipse() {
		svd = new IncrementalSVD(2);
		svd.setDefaultWeighting(1.);
		image = new MBFImage(400,400,3);
		disp = DisplayUtilities.displaySimple(image);
		disp.getContentPane().getComponent(0).addMouseListener(this);
		disp.getContentPane().getComponent(0).addMouseMotionListener(this);
	}
	public static void main(String[] args) {
		new OnePassSVDEllipse();
	}

	@Override
	public void mouseClicked(MouseEvent e) {

		if(initMode){
			init.set(0, (int)seen, e.getX());
			init.set(1, (int)seen, e.getY());
			seen ++;
			if (seen == init.getColumnDimension()) {
				initMean();
				svd.update(MatrixUtils.minusCol(init, mean));
				redrawEllipses();
			}
		}
		else{
			point.set(0, 0, e.getX());
			point.set(1, 0, e.getY());
			updateMean(point);
			svd.update(point.minus(mean),DAMPENING);
			redrawEllipses();
		}
		drawPoint(e.getX(),e.getY());
		if(initMode  && seen >= init.getColumnDimension()){
			initMode = false;
		}
	}
	private void updateMean(Matrix point) {
		Matrix newMean = mean.times(DAMPENING);
		newMean.plusEquals(point.times(1 - DAMPENING));
		mean = newMean;
	}
	private void redrawEllipses() {
		Matrix US = svd.U.times(MatrixUtils.sqrt(svd.Sdiag));
		Ellipse e = EllipseUtilities.ellipseFromCovariance((float)mean.get(0, 0), (float)mean.get(1, 0), US.times(US.transpose()), 6f);
		if(prevEllipse!=null)
			image.drawShape(prevEllipse, RGBColour.BLUE);
		image.drawShape(e, RGBColour.RED);
		image.drawShape(new Circle((float)mean.get(0, 0),(float)mean.get(1, 0),10), RGBColour.GREEN);
		this.prevEllipse = e;
		DisplayUtilities.display(image,disp);
	}
	private void drawPoint(int x, int y) {
		image.drawShape(new Circle(x,y,10), RGBColour.RED);
		DisplayUtilities.display(image,disp);
	}
	private void initMean() {
		mean = MatrixUtils.sumRows(init);
		mean = mean.times(1.0/init.getColumnDimension());
	}
	@Override
	public void mousePressed(MouseEvent e) {
		image.fill(RGBColour.BLACK);
		mouseClicked(e);
		dragClick = true;
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		dragClick = false;
		image.fill(RGBColour.BLACK);
		redrawEllipses();
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}
	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}
	@Override
	public void mouseDragged(MouseEvent e) {
		mouseClicked(e);
	}
	@Override
	public void mouseMoved(MouseEvent e) {

	}
}
