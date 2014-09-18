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
package org.openimaj.demos.sandbox.c3d;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.camera.CameraIntrinsics;
import org.openimaj.image.colour.Transforms;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.FundamentalRefinement;
import org.openimaj.math.geometry.transforms.estimation.RobustFundamentalEstimator;
import org.openimaj.util.pair.Pair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.tracking.klt.Feature;
import org.openimaj.video.tracking.klt.FeatureList;
import org.openimaj.video.tracking.klt.KLTTracker;
import org.openimaj.video.tracking.klt.TrackingContext;

import Jama.Matrix;

public class Main implements VideoDisplayListener<MBFImage>, KeyListener {
	private VideoCapture capture;
	private VideoDisplay<MBFImage> videoFrame;
	private KLTTracker tracker;
	private FeatureList fl1;
	private FeatureList fl2;
	private FImage oldFrame;

	boolean firstFrame = true;
	private int nFeatures = 150;
	private CameraIntrinsics cam;

	public Main() throws Exception {
		capture = new VideoCapture(640, 480);
		videoFrame = VideoDisplay.createVideoDisplay(capture);
		videoFrame.addVideoListener(this);
		SwingUtilities.getRoot(videoFrame.getScreen()).addKeyListener(this);
		// videoFrame.getScreen().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final TrackingContext tc = new TrackingContext();
		fl1 = new FeatureList(nFeatures);
		tracker = new KLTTracker(tc, fl1);

		tc.setSequentialMode(true);
		tc.setWriteInternalImages(false);
		tc.setAffineConsistencyCheck(-1);

		final Matrix m = new Matrix(3, 3);
		this.cam = new CameraIntrinsics(m, 640, 480);
		cam.setFocalLengthX(640);
		cam.setFocalLengthX(480);
		cam.setPrincipalPointX(640 / 2);
		cam.setPrincipalPointY(320 / 2);
	}

	public boolean needsReset() {
		return this.firstFrame;
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void beforeUpdate(MBFImage image) {
		final FImage greyFrame = Transforms.calculateIntensityNTSC(image);
		if (needsReset()) {
			tracker.selectGoodFeatures(greyFrame);
		}
		else {
			fl2 = fl1.clone();
			tracker.trackFeatures(oldFrame, greyFrame);
			tracker.replaceLostFeatures(greyFrame);

			final List<Pair<Point2d>> corres = new ArrayList<Pair<Point2d>>();
			for (int i = 0; i < fl1.features.length; i++) {
				final Feature p = fl2.features[i];
				final Feature c = fl1.features[i];

				if (c.val == 0) {
					corres.add(new Pair<Point2d>(p, c));
				}
			}

			try {
				final RobustFundamentalEstimator rfe = new RobustFundamentalEstimator(0.35, FundamentalRefinement.SAMPSON);
				rfe.fitData(corres);

				rfe.getModel().getF().print(5, 5);
			} catch (final Exception e) {

			}
		}
		fl1.drawFeatures(image);

		this.oldFrame = greyFrame;
		this.firstFrame = false;
	}

	static Matrix computeEssentialMatrix(CameraIntrinsics ci, Matrix F) {
		return ci.calibrationMatrix.transpose().times(F).times(ci.calibrationMatrix);
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == 'r') {
			this.firstFrame = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	public static void main(String args[]) throws Exception {
		new Main();
	}
}
