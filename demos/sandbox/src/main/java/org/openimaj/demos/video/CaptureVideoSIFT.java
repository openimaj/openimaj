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
package org.openimaj.demos.video;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.SwingUtilities;

import org.openimaj.demos.video.utils.PolygonDrawingListener;
import org.openimaj.demos.video.utils.PolygonExtractionProcessor;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.FImage;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.transforms.HomographyRefinement;
import org.openimaj.math.geometry.transforms.MatrixTransformProvider;
import org.openimaj.math.geometry.transforms.estimation.RobustHomographyEstimator;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

public class CaptureVideoSIFT implements KeyListener, VideoDisplayListener<MBFImage> {

	private VideoWithinVideo vwv;
	private PolygonDrawingListener polygonListener;
	private DoGSIFTEngine engine;
	private VideoDisplay<MBFImage> videoFrame;
	private MBFImage modelImage;
	private ConsistentLocalFeatureMatcher2d<Keypoint> matcher;
	private boolean ransacReader = false;

	public CaptureVideoSIFT(VideoWithinVideo videoWithinVideo) {
		this.vwv = videoWithinVideo;
		polygonListener = new PolygonDrawingListener();
		this.vwv.display.getScreen().addMouseListener(polygonListener);
		SwingUtilities.getRoot(this.vwv.display.getScreen()).addKeyListener(this);
		engine = new DoGSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);

		this.videoFrame = VideoDisplay.createOffscreenVideoDisplay(vwv.capture);
		this.videoFrame.addVideoListener(this);
	}

	@Override
	public void keyPressed(KeyEvent key) {
		if (key.getKeyCode() == KeyEvent.VK_SPACE) {
			this.videoFrame.togglePause();
		}
		else if (key.getKeyChar() == 'r') {
			vwv.display.seek(0);
		}
		else if (key.getKeyChar() == 'c' && this.polygonListener.getPolygon().getVertices().size() > 2) {
			try {
				ransacReader = false;
				final Polygon p = this.polygonListener.getPolygon().clone();
				this.polygonListener.reset();
				modelImage = this.vwv.capture.getCurrentFrame().process(
						new PolygonExtractionProcessor<Float[], MBFImage>(p, RGBColour.BLACK));

				// configure the matcher
				matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(new FastBasicKeypointMatcher<Keypoint>(8));
				matcher.setFittingModel(new RobustHomographyEstimator(3.0, 1500,
						new RANSAC.PercentageInliersStoppingCondition(0.01), HomographyRefinement.NONE));

				final DoGSIFTEngine engine = new DoGSIFTEngine();
				engine.getOptions().setDoubleInitialImage(false);

				final FImage modelF = Transforms.calculateIntensityNTSC(modelImage);
				matcher.setModelFeatures(engine.findFeatures(modelF));
				vwv.display.seek(0);
				ransacReader = true;

			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		if (ransacReader && matcher != null && !videoFrame.isPaused()) {
			final MBFImage capImg = videoFrame.getVideo().getCurrentFrame();
			final LocalFeatureList<Keypoint> kpl = engine.findFeatures(Transforms.calculateIntensityNTSC(capImg));
			if (matcher.findMatches(kpl)) {
				try {
					final Polygon poly = modelImage.getBounds()
							.transform(((MatrixTransformProvider) matcher.getModel()).getTransform().inverse())
							.asPolygon();

					this.vwv.targetArea = poly;
				} catch (final RuntimeException e) {
				}

			} else {
				this.vwv.targetArea = null;
			}
		}
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		final MBFImage frameWrite = frame;
		this.polygonListener.drawPoints(frameWrite);
		this.vwv.copyToCaptureFrame(frameWrite);

	}

}
