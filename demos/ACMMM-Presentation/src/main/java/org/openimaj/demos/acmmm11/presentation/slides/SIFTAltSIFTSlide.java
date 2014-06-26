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
package org.openimaj.demos.acmmm11.presentation.slides;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JPanel;

import org.openimaj.content.slideshow.Slide;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.ALTDoGSIFTEngine;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.transforms.AffineTransformModel;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.geometry.transforms.residuals.SingleImageTransferResidual2d;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.util.pair.Pair;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;

import Jama.Matrix;

/**
 * Slide illustrating two sift implementation
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class SIFTAltSIFTSlide implements Slide, VideoDisplayListener<MBFImage>, KeyListener {

	private MBFImage outFrame;
	private DoGSIFTEngine normalEngine;
	private DoGSIFTEngine altEngine;
	private FImage carpetGrey;
	private MBFImage carpet;
	private ConsistentLocalFeatureMatcher2d<Keypoint> normalmatcher;
	private ConsistentLocalFeatureMatcher2d<Keypoint> altmatcher;
	private ImageComponent ic;
	private SpinningImageVideo spinning;
	private VideoDisplay<MBFImage> display;

	@Override
	public Component getComponent(int width, int height) throws IOException {
		carpet = ImageUtilities.readMBF(SIFTAltSIFTSlide.class.getResource("rabbit.jpeg"));
		final double wh = Math.sqrt(carpet.getWidth() * carpet.getWidth() + carpet.getHeight() * carpet.getHeight());
		if (wh * 2 > Math.min(width, height)) {
			final float prop = (float) (Math.min(width, height) / (wh * 2));
			carpet.processInplace(new ResizeProcessor(prop));
		}

		this.carpetGrey = carpet.flatten();

		spinning = new SpinningImageVideo(carpet, -0.5f, 0.005f);

		outFrame = new MBFImage(spinning.getWidth() * 2, spinning.getHeight() * 2, 3);
		normalEngine = new DoGSIFTEngine();
		normalEngine.getOptions().setDoubleInitialImage(false);
		altEngine = new ALTDoGSIFTEngine();
		altEngine.getOptions().setDoubleInitialImage(false);

		final LocalFeatureList<Keypoint> carpetNormalKPTs = normalEngine.findFeatures(carpetGrey);
		normalmatcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
				new FastBasicKeypointMatcher<Keypoint>(8),
				new RANSAC<Point2d, Point2d, AffineTransformModel>(new AffineTransformModel(),
						new SingleImageTransferResidual2d<AffineTransformModel>(), 5.0, 100,
						new RANSAC.ProbabilisticMinInliersStoppingCondition(0.01), true)
				);
		normalmatcher.setModelFeatures(carpetNormalKPTs);

		final LocalFeatureList<Keypoint> carpetAltKPTs = altEngine.findFeatures(carpetGrey);
		altmatcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
				new FastBasicKeypointMatcher<Keypoint>(8),
				new RANSAC<Point2d, Point2d, AffineTransformModel>(new AffineTransformModel(),
						new SingleImageTransferResidual2d<AffineTransformModel>(), 5.0, 100,
						new RANSAC.ProbabilisticMinInliersStoppingCondition(0.01), true)
				);
		altmatcher.setModelFeatures(carpetAltKPTs);

		display = VideoDisplay.createOffscreenVideoDisplay(spinning);
		display.addVideoListener(this);

		final JPanel c = new JPanel();
		c.setOpaque(false);
		c.setPreferredSize(new Dimension(width, height));
		c.setLayout(new GridBagLayout());
		ic = new ImageComponent(true, false);
		c.add(ic);
		for (final Component cc : c.getComponents()) {
			if (cc instanceof JPanel) {
				((JPanel) cc).setOpaque(false);
			}
		}
		return c;
	}

	@Override
	public void close() {
		this.spinning.stop();
		this.display.close();
	}

	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		// do nothing
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		outFrame.fill(RGBColour.BLACK);
		final FImage fGrey = frame.flatten();
		final LocalFeatureList<Keypoint> normalKPTs = normalEngine.findFeatures(fGrey);
		final LocalFeatureList<Keypoint> altKPTs = altEngine.findFeatures(fGrey);

		final Matrix carpetOffset = TransformUtilities.translateMatrix(frame.getWidth() / 4, frame.getHeight() * 2 / 3);
		final Matrix normalOffset = TransformUtilities.translateMatrix(frame.getWidth(), 0);
		final Matrix altOffset = TransformUtilities.translateMatrix(frame.getWidth(), frame.getHeight());

		outFrame.drawImage(carpet, frame.getWidth() / 4, frame.getHeight() * 2 / 3);
		outFrame.drawImage(frame, frame.getWidth(), 0);
		outFrame.drawImage(frame, frame.getWidth(), frame.getHeight());

		if (normalmatcher.findMatches(normalKPTs))
		{
			final List<Pair<Keypoint>> normalMatches = normalmatcher.getMatches();
			for (final Pair<Keypoint> kps : normalMatches) {
				final Keypoint p1 = kps.firstObject().transform(normalOffset);
				final Keypoint p2 = kps.secondObject().transform(carpetOffset);

				outFrame.drawPoint(p1, RGBColour.RED, 3);
				outFrame.drawPoint(p2, RGBColour.RED, 3);

				outFrame.drawLine(new Line2d(p1, p2), 3, RGBColour.GREEN);
			}
		}

		if (altmatcher.findMatches(altKPTs)) {
			final List<Pair<Keypoint>> altMatches = altmatcher.getMatches();
			for (final Pair<Keypoint> kps : altMatches) {
				final Keypoint p1 = kps.firstObject().transform(altOffset);
				final Keypoint p2 = kps.secondObject().transform(carpetOffset);

				outFrame.drawPoint(p1, RGBColour.RED, 3);
				outFrame.drawPoint(p2, RGBColour.RED, 3);

				outFrame.drawLine(new Line2d(p1, p2), 3, RGBColour.BLUE);
			}
		}

		this.ic.setImage(ImageUtilities.createBufferedImageForDisplay(outFrame));
	}

	@Override
	public void keyPressed(KeyEvent key) {
		if (key.getKeyChar() == 'x') {
			this.spinning.adjustSpeed(0.005f);
		}
		else if (key.getKeyChar() == 'z') {
			this.spinning.adjustSpeed(-0.005f);
		}
		else if (key.getKeyCode() == KeyEvent.VK_SPACE) {
			this.display.togglePause();
			this.spinning.togglePause();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// do nothing
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// do nothing
	}

}
