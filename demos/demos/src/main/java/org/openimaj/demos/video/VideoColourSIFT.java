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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.openimaj.demos.Demo;
import org.openimaj.demos.video.utils.PolygonDrawingListener;
import org.openimaj.demos.video.utils.PolygonExtractionProcessor;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGColourSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processing.transform.MBFProjectionProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.shape.Polygon;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.HomographyModel;
import org.openimaj.math.geometry.transforms.MatrixTransformProvider;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.math.geometry.transforms.residuals.SingleImageTransferResidual2d;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.xuggle.XuggleVideo;

import Jama.Matrix;

/**
 * OpenIMAJ Real-time (ish) SIFT tracking and matching demo
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
@Demo(
		author = "Jonathon Hare and Sina Samangooei",
		description = "Realtime-ish SIFT-based tracking demonstration." +
				"Hold an object in front of the camera, and press space. Select" +
				"the outline of the object by clicking points on the frozen video " +
				"image, and press C when you're done. Press space to start the video " +
				"again, and the object should be tracked. This demo uses a homography " +
				"to constrain the matches.",
		keywords = { "video", "sift", "object tracking" },
		title = "VideoColourSIFT")
public class VideoColourSIFT implements KeyListener, VideoDisplayListener<MBFImage> {
	enum RenderMode {
		SQUARE {
			@Override
			public void render(final MBFImageRenderer renderer, final Matrix transform, final Rectangle rectangle) {
				renderer.drawShape(rectangle.transform(transform), 3, RGBColour.BLUE);
			}
		},
		PICTURE {
			MBFImage toRender = null;
			private Matrix renderToBounds;

			@Override
			public void render(final MBFImageRenderer renderer, final Matrix transform, final Rectangle rectangle) {
				if (this.toRender == null) {
					try {
						this.toRender = ImageUtilities.readMBF(VideoColourSIFT.class
								.getResource("/org/openimaj/demos/OpenIMAJ.png"));
					} catch (final IOException e) {
						System.err.println("Can't load image to render");
					}
					this.renderToBounds = TransformUtilities.makeTransform(this.toRender.getBounds(), rectangle);
				}

				final MBFProjectionProcessor mbfPP = new MBFProjectionProcessor();
				mbfPP.setMatrix(transform.times(this.renderToBounds));
				mbfPP.accumulate(this.toRender);
				mbfPP.performProjection(0, 0, renderer.getImage());

			}
		},
		VIDEO {
			private XuggleVideo toRender;
			private Matrix renderToBounds;

			@Override
			public void render(final MBFImageRenderer renderer, final Matrix transform, final Rectangle rectangle) {
				if (this.toRender == null) {
					this.toRender = new XuggleVideo(
							VideoColourSIFT.class.getResource("/org/openimaj/demos/video/keyboardcat.flv"), true);
					this.renderToBounds = TransformUtilities.makeTransform(new Rectangle(0, 0, this.toRender.getWidth(),
							this.toRender.getHeight()), rectangle);
				}

				final MBFProjectionProcessor mbfPP = new MBFProjectionProcessor();
				mbfPP.setMatrix(transform.times(this.renderToBounds));
				mbfPP.accumulate(this.toRender.getNextFrame());
				mbfPP.performProjection(0, 0, renderer.getImage());
			}
		};
		public abstract void render(MBFImageRenderer renderer, Matrix transform, Rectangle rectangle);
	}

	private final VideoCapture capture;
	private final VideoDisplay<MBFImage> videoFrame;
	private final ImageComponent modelFrame;
	private final ImageComponent matchFrame;

	private MBFImage modelImage;

	private ConsistentLocalFeatureMatcher2d<Keypoint> matcher;
	private final DoGColourSIFTEngine engine;
	private final PolygonDrawingListener polygonListener;
	private final JPanel vidPanel;
	private final JPanel modelPanel;
	private final JPanel matchPanel;
	private RenderMode renderMode = RenderMode.SQUARE;

	/**
	 * Construct the demo
	 * 
	 * @param window
	 * @throws Exception
	 */
	public VideoColourSIFT(final JComponent window) throws Exception {
		this(window, new VideoCapture(320, 240));
	}

	/**
	 * Construct the demo
	 * 
	 * @param window
	 * @param capture
	 * @throws Exception
	 */
	public VideoColourSIFT(final JComponent window, final VideoCapture capture) throws Exception {
		final int width = capture.getWidth();
		final int height = capture.getHeight();
		this.capture = capture;
		this.polygonListener = new PolygonDrawingListener();

		GridBagConstraints gbc = new GridBagConstraints();

		final JLabel label = new JLabel(
				"<html><body><p>Hold an object in front of the camera, and press space. Select<br/>" +
						"the outline of the object by clicking points on the frozen video<br/>" +
						"image, and press C when you're done. Press space to start the video<br/>" +
						"again, and the object should be tracked.</p></body></html>");
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(8, 8, 8, 8);
		window.add(label, gbc);

		this.vidPanel = new JPanel(new GridBagLayout());
		this.vidPanel.setBorder(BorderFactory.createTitledBorder("Live Video"));
		this.videoFrame = VideoDisplay.createVideoDisplay(capture, this.vidPanel);
		gbc = new GridBagConstraints();
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.gridwidth = 1;
		window.add(this.vidPanel, gbc);

		this.modelPanel = new JPanel(new GridBagLayout());
		this.modelPanel.setBorder(BorderFactory.createTitledBorder("Model"));
		this.modelFrame = new ImageComponent(true, false);
		this.modelFrame.setSize(width, height);
		this.modelFrame.setPreferredSize(new Dimension(width, height));
		this.modelPanel.add(this.modelFrame);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.PAGE_START;
		gbc.gridy = 1;
		gbc.gridx = 1;
		window.add(this.modelPanel, gbc);

		this.matchPanel = new JPanel(new GridBagLayout());
		this.matchPanel.setBorder(BorderFactory.createTitledBorder("Matches"));
		this.matchFrame = new ImageComponent(true, false);
		this.matchFrame.setSize(width * 2, height);
		this.matchFrame.setPreferredSize(new Dimension(width * 2, height));
		this.matchPanel.add(this.matchFrame);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.PAGE_END;
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		window.add(this.matchPanel, gbc);

		this.videoFrame.getScreen().addMouseListener(this.polygonListener);

		this.videoFrame.addVideoListener(this);
		this.engine = new DoGColourSIFTEngine();
		this.engine.getOptions().setDoubleInitialImage(false);
	}

	@Override
	public synchronized void keyPressed(final KeyEvent key) {
		if (key.getKeyCode() == KeyEvent.VK_SPACE) {
			this.videoFrame.togglePause();
		} else if (key.getKeyChar() == 'c' && this.polygonListener.getPolygon().getVertices().size() > 2) {
			try {
				final Polygon p = this.polygonListener.getPolygon().clone();
				this.polygonListener.reset();
				this.modelImage = this.capture.getCurrentFrame().process(
						new PolygonExtractionProcessor<Float[], MBFImage>(p, RGBColour.BLACK));

				if (this.matcher == null) {
					// configure the matcher
					final HomographyModel model = new HomographyModel();
					final RANSAC<Point2d, Point2d, HomographyModel> ransac = new RANSAC<Point2d, Point2d, HomographyModel>(
							model, new SingleImageTransferResidual2d<HomographyModel>(),
							3.0, 1500, new RANSAC.ProbabilisticMinInliersStoppingCondition(0.01), true);
					this.matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(
							new FastBasicKeypointMatcher<Keypoint>(8));
					this.matcher.setFittingModel(ransac);

					this.modelPanel.setPreferredSize(this.modelPanel.getSize());
				}

				this.modelFrame.setImage(ImageUtilities.createBufferedImageForDisplay(this.modelImage));

				final DoGColourSIFTEngine engine = new DoGColourSIFTEngine();
				engine.getOptions().setDoubleInitialImage(true);

				this.matcher.setModelFeatures(engine.findFeatures(this.modelImage));
			} catch (final Exception e) {
				e.printStackTrace();
			}
		} else if (key.getKeyChar() == '1') {
			this.renderMode = RenderMode.SQUARE;
		} else if (key.getKeyChar() == '2') {
			this.renderMode = RenderMode.PICTURE;
		} else if (key.getKeyChar() == '3') {
			this.renderMode = RenderMode.VIDEO;
		}
	}

	@Override
	public void keyReleased(final KeyEvent arg0) {
	}

	@Override
	public void keyTyped(final KeyEvent arg0) {
	}

	@Override
	public synchronized void afterUpdate(final VideoDisplay<MBFImage> display) {
		if (this.matcher != null && !this.videoFrame.isPaused()) {
			final MBFImage capImg = this.videoFrame.getVideo().getCurrentFrame();
			final LocalFeatureList<Keypoint> kpl = this.engine.findFeatures(capImg);

			final MBFImageRenderer renderer = capImg.createRenderer();
			renderer.drawPoints(kpl, RGBColour.MAGENTA, 3);

			MBFImage matches;
			if (this.matcher.findMatches(kpl)) {
				try {
					// Shape sh =
					// modelImage.getBounds().transform(((MatrixTransformProvider)
					// matcher.getModel()).getTransform().inverse());
					// renderer.drawShape(sh, 3, RGBColour.BLUE);
					final Matrix boundsToPoly = ((MatrixTransformProvider) this.matcher.getModel()).getTransform()
							.inverse();
					this.renderMode.render(renderer, boundsToPoly, this.modelImage.getBounds());
				} catch (final RuntimeException e) {
				}

				matches = MatchingUtilities
						.drawMatches(this.modelImage, capImg, this.matcher.getMatches(), RGBColour.RED);
			} else {
				matches = MatchingUtilities
						.drawMatches(this.modelImage, capImg, this.matcher.getMatches(), RGBColour.RED);
			}

			this.matchPanel.setPreferredSize(this.matchPanel.getSize());
			this.matchFrame.setImage(ImageUtilities.createBufferedImageForDisplay(matches));
		}
	}

	@Override
	public void beforeUpdate(final MBFImage frame) {
		this.polygonListener.drawPoints(frame);
	}

	/**
	 * Stop capture
	 */
	public void stop() {
		this.videoFrame.close();
		this.capture.stopCapture();
	}

	/**
	 * Main method
	 * 
	 * @param args
	 *            ignored
	 * @throws Exception
	 */
	public static void main(final String[] args) throws Exception {
		final JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		window.setLayout(new GridBagLayout());
		final JPanel c = new JPanel();
		c.setLayout(new GridBagLayout());
		window.getContentPane().add(c);

		final VideoColourSIFT vs = new VideoColourSIFT(c);
		SwingUtilities.getRoot(window).addKeyListener(vs);
		window.pack();
		window.setVisible(true);
	}

}
