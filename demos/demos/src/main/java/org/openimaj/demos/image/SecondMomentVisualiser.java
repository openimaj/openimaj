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
package org.openimaj.demos.image;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.interest.HarrisIPD;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processing.transform.ProjectionProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Ellipse;
import org.openimaj.math.geometry.shape.EllipseUtilities;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.shape.Shape;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.Pair;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * Demonstrate the second moments
 * 
 * @author Sina Samangeooi (ss@ecs.soton.ac.uk)
 * 
 */
@Demo(
		author = "Sina Samangeooi",
		description = "Demonstrates the second moment extractor in an interactive"
				+ " way. Move the mouse over the edges of the box in the first image "
				+ "and the moments are displayed in the other images.",
		keywords = {
				"image", "moments" },
		title = "Second Moment Visualiser",
		icon = "/org/openimaj/demos/icons/image/moment-icon.png")
public class SecondMomentVisualiser implements MouseListener, MouseMotionListener {
	private MBFImage image;
	private HarrisIPD ipd;
	private Point2d drawPoint = null;
	private double derivscale;
	private JFrame projectFrame;
	private ResizeProcessor resizeProject;
	private List<Ellipse> ellipses;
	private List<Pair<Line2d>> lines;
	private Matrix transformMatrix;
	private JFrame mouseFrame;
	private int windowSize;
	private int featureWindowSize;
	private JFrame featureFrame;
	private double visFactor = 4;

	/**
	 * Construct the demo
	 * 
	 * @throws IOException
	 */
	public SecondMomentVisualiser() throws IOException {
		// image = ImageUtilities.readMBF(
		// SecondMomentVisualiser.class.getResourceAsStream("/org/openimaj/image/data/square_rot.png")
		// );
		image = new MBFImage(400, 400, ColourSpace.RGB);
		image.fill(RGBColour.WHITE);
		final Shape shapeToDraw = new Rectangle(100, 100, 200, 200)
				.transform(TransformUtilities.rotationMatrixAboutPoint(
						Math.PI / 4, 200, 200));
		// Shape shapeToDraw = new Rectangle(100,100,200,200);
		// Shape shapeToDraw = new Circle(200f,200f,100f);
		image.createRenderer().drawShapeFilled(shapeToDraw, RGBColour.BLACK);
		derivscale = 5;
		ipd = new HarrisIPD((float) derivscale, (float) derivscale * 2);
		ipd.findInterestPoints(Transforms.calculateIntensityNTSC(image));

		class Updater implements Runnable {

			private SecondMomentVisualiser frame;

			Updater(SecondMomentVisualiser frame) {
				this.frame = frame;
			}

			@Override
			public void run() {
				while (true) {
					frame.draw();
					try {
						Thread.sleep(1000 / 30);
					} catch (final InterruptedException e) {
					}
				}
			}
		}
		image = image.process(new FGaussianConvolve(5));

		this.mouseFrame = DisplayUtilities.displaySimple(image.clone());

		this.mouseFrame.getContentPane().addMouseListener(this);
		this.mouseFrame.getContentPane().addMouseMotionListener(this);

		projectFrame = DisplayUtilities.display(image.clone());
		projectFrame.setBounds(image.getWidth(), 0, image.getWidth(),
				image.getHeight());
		featureFrame = DisplayUtilities.display(image.clone());
		featureFrame.setBounds(image.getWidth() * 2, 0, image.getWidth(),
				image.getHeight());
		ellipses = new ArrayList<Ellipse>();
		lines = new ArrayList<Pair<Line2d>>();
		resizeProject = new ResizeProcessor(256, 256);
		final Thread t = new Thread(new Updater(this));
		t.start();

	}

	/**
	 * Draw
	 */
	public synchronized void draw() {
		final MBFImage toDraw = image.clone();
		final MBFImageRenderer renderer = toDraw.createRenderer();

		if (this.drawPoint != null)
			renderer.drawPoint(this.drawPoint, RGBColour.RED, 3);

		for (final Ellipse ellipse : ellipses) {
			renderer.drawShape(ellipse, 1, RGBColour.GREEN);
		}
		for (final Pair<Line2d> line : lines) {
			renderer.drawLine(line.firstObject(), 3, RGBColour.BLUE);
			renderer.drawLine(line.secondObject(), 3, RGBColour.RED);
		}
		if (this.transformMatrix != null) {
			try {

				final ProjectionProcessor<Float[], MBFImage> pp = new ProjectionProcessor<Float[], MBFImage>();
				pp.setMatrix(this.transformMatrix);
				this.image.accumulateWith(pp);
				final MBFImage patch = pp.performProjection(-windowSize,
						windowSize, -windowSize, windowSize,
						RGBColour.RED);
				if (patch.getWidth() > 0 && patch.getHeight() > 0) {
					DisplayUtilities.display(patch.process(this.resizeProject),
							this.projectFrame);
					DisplayUtilities.display(
							patch.extractCenter(this.featureWindowSize,
									this.featureWindowSize).process(
									this.resizeProject), this.featureFrame);

				}
			} catch (final Exception e) {
				e.printStackTrace();
			}

		}

		DisplayUtilities.display(toDraw.clone(), this.mouseFrame);
	}

	private synchronized void setEBowl() {
		final Matrix secondMoments = ipd.getSecondMomentsAt(
				(int) this.drawPoint.getX(), (int) this.drawPoint.getY());
		// System.out.println(secondMoments.det());
		// secondMoments = secondMoments.times(1/secondMoments.det());
		// System.out.println(secondMoments.det());
		this.ellipses.clear();
		this.lines.clear();
		try {
			getBowlEllipse(secondMoments);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void getBowlEllipse(Matrix secondMoments) {
		double rotation = 0;
		double d1 = 0, d2 = 0;
		if (secondMoments.det() == 0)
			return;
		// If [u v] M [u v]' = E(u,v)
		// THEN
		// [u v] (M / E(u,v)) [u v]' = 1
		// THEN you can go ahead and do the eigen decomp s.t.
		// (M / E(u,v)) = R' D R
		// where R is the rotation and D is the size of the ellipse
		// double divFactor = 1/E;
		final Matrix noblur = new Matrix(new double[][] {
				{
						ipd.lxmxblur.getPixel((int) this.drawPoint.getX(),
								(int) this.drawPoint.getY()),
						ipd.lxmyblur.getPixel((int) this.drawPoint.getX(),
								(int) this.drawPoint.getY()) },
				{
						ipd.lxmyblur.getPixel((int) this.drawPoint.getX(),
								(int) this.drawPoint.getY()),
						ipd.lxmxblur.getPixel((int) this.drawPoint.getX(),
								(int) this.drawPoint.getY()) } });
		System.out.println("NO BLUR SECOND MOMENTS MATRIX");
		noblur.print(5, 5);
		System.out.println("det is: " + noblur.det());
		if (noblur.det() < 0.00001)
			return;

		final double divFactor = 1 / Math.sqrt(secondMoments.det());
		final double scaleFctor = derivscale;
		final EigenvalueDecomposition rdr = secondMoments.times(divFactor).eig();
		secondMoments.times(divFactor).print(5, 5);

		System.out.println("D1(before)= " + rdr.getD().get(0, 0));
		System.out.println("D2(before) = " + rdr.getD().get(1, 1));

		if (rdr.getD().get(0, 0) == 0)
			d1 = 0;
		else
			d1 = 1.0 / Math.sqrt(rdr.getD().get(0, 0));
		// d1 = Math.sqrt(rdr.getD().get(0,0));
		if (rdr.getD().get(1, 1) == 0)
			d2 = 0;
		else
			d2 = 1.0 / Math.sqrt(rdr.getD().get(1, 1));
		// d2 = Math.sqrt(rdr.getD().get(1,1));

		final double scaleCorrectedD1 = d1 * scaleFctor * visFactor;
		final double scaleCorrectedD2 = d2 * scaleFctor * visFactor;

		final Matrix eigenMatrix = rdr.getV();
		System.out.println("D1 = " + d1);
		System.out.println("D2 = " + d2);
		eigenMatrix.print(5, 5);

		rotation = Math.atan2(eigenMatrix.get(1, 0), eigenMatrix.get(0, 0));
		final Ellipse ellipseToAdd = EllipseUtilities.ellipseFromEquation(
				this.drawPoint.getX(), // center x
				this.drawPoint.getY(), // center y
				scaleCorrectedD1, // semi-major axis
				scaleCorrectedD2, // semi-minor axis
				rotation// rotation
				);
		ellipses.add(ellipseToAdd);

		if (d1 != 0 && d2 != 0) {
			this.windowSize = (int) (scaleFctor * d1 / d2) / 2;
			this.featureWindowSize = (int) scaleFctor;
			if (this.windowSize > 256)
				this.windowSize = 256;
			// this.transformMatrix = affineIPDTransformMatrix(secondMoments);
			// this.transformMatrix =
			// secondMomentsTransformMatrix(secondMoments);
			// this.transformMatrix =
			// usingEllipseTransformMatrix(d1,d2,rotation);
			this.transformMatrix = ellipseToAdd
					.transformMatrix()
					.times(TransformUtilities.scaleMatrix(1 / scaleFctor,
							1 / scaleFctor)).inverse();
			for (final double d : transformMatrix.getRowPackedCopy())
				if (d == Double.NaN) {
					this.transformMatrix = null;
					break;
				}
		} else {
			transformMatrix = null;
		}
		if (transformMatrix != null) {
			System.out.println("Transform matrix:");
			transformMatrix.print(5, 5);
		}

		final Line2d major = Line2d.lineFromRotation((int) this.drawPoint.getX(),
				(int) this.drawPoint.getY(), (float) rotation,
				(int) scaleCorrectedD1);
		final Line2d minor = Line2d.lineFromRotation((int) this.drawPoint.getX(),
				(int) this.drawPoint.getY(), (float) (rotation + Math.PI / 2),
				(int) scaleCorrectedD2);
		lines.add(new Pair<Line2d>(major, minor));
	}

	// private Matrix usingEllipseTransformMatrix(double major, double minor,
	// double rotation){
	// Matrix rotate = TransformUtilities.rotationMatrix(rotation);
	// Matrix scale = TransformUtilities.scaleMatrix(major, minor);
	// Matrix translation =
	// TransformUtilities.translateMatrix(this.drawPoint.getX(),
	// this.drawPoint.getY());
	// // Matrix transformMatrix = scale.times(translation).times(rotation);
	// Matrix transformMatrix = translation.times(rotate.times(scale));
	// return transformMatrix.inverse();
	// }
	//
	// private Matrix secondMomentsTransformMatrix(Matrix secondMoments) {
	// secondMoments = secondMoments.times(1/Math.sqrt(secondMoments.det()));
	// Matrix eigenMatrix = MatrixUtils.sqrt(secondMoments).inverse(); // This
	// is simply the rotation (eig vectors) and the scaling by the semi major
	// and semi minor axis (eig values)
	// // eigenMatrix = eigenMatrix.inverse();
	// Matrix transformMatrix = new Matrix(new double[][]{
	// {eigenMatrix.get(0, 0),eigenMatrix.get(0, 1),this.drawPoint.getX()},
	// {eigenMatrix.get(1, 0),eigenMatrix.get(1, 1),this.drawPoint.getY()},
	// {0,0,1},
	// });
	// return transformMatrix.inverse();
	// }
	//
	//
	// private Matrix affineIPDTransformMatrix(Matrix secondMoments) {
	// Matrix covar =
	// AbstractIPD.InterestPointData.getCovarianceMatrix(secondMoments);
	// Matrix sqrt = MatrixUtils.sqrt(covar);
	// Matrix transform = new Matrix(new double[][]{
	// {sqrt.get(0, 0),sqrt.get(0,1),this.drawPoint.getX()},
	// {sqrt.get(1, 0),sqrt.get(1,1),this.drawPoint.getY()},
	// {0,0,1},
	// });
	// return transform.inverse();
	// }

	@Override
	public void mouseClicked(MouseEvent event) {
		drawPoint = new Point2dImpl(event.getX(), event.getY());
		if (this.drawPoint != null) {
			setEBowl();
		}
	}

	@Override
	public void mouseEntered(MouseEvent event) {

	}

	@Override
	public void mouseExited(MouseEvent event) {

	}

	@Override
	public void mousePressed(MouseEvent event) {

	}

	@Override
	public void mouseReleased(MouseEvent event) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		drawPoint = new Point2dImpl(e.getX(), e.getY());
		if (this.drawPoint != null) {
			setEBowl();
		}
	}

	/**
	 * The main method
	 * 
	 * @param args
	 *            ignored
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException {
		new SecondMomentVisualiser();
	}
}
