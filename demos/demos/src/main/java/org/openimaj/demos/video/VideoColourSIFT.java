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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
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
				"Hold an object in front of the camera, and press space. Select"  +
				"the outline of the object by clicking points on the frozen video " +
				"image, and press C when you're done. Press space to start the video " +
				"again, and the object should be tracked. This demo uses a homography " +
				"to constrain the matches.",
		keywords = { "video", "sift", "object tracking" }, 
		title = "VideoSIFT"
	)
public class VideoColourSIFT implements KeyListener, VideoDisplayListener<MBFImage> {
	enum RenderMode{
		SQUARE {
			@Override
			public void render(MBFImageRenderer renderer,Matrix transform, Rectangle rectangle) {
				renderer.drawShape(rectangle.transform(transform), 3, RGBColour.BLUE);
			}
		},
		PICTURE {
			MBFImage toRender = null;
			private Matrix renderToBounds;
			@Override
			public void render(MBFImageRenderer renderer,Matrix transform, Rectangle rectangle) {
				if(toRender == null){
					try {
						toRender = ImageUtilities.readMBF(VideoColourSIFT.class.getResource("/org/openimaj/demos/OpenIMAJ.png"));
					} catch (IOException e) {
						System.err.println("Can't load image to render");
					}
					renderToBounds = TransformUtilities.makeTransform(toRender.getBounds(), rectangle);
				}
				
				MBFProjectionProcessor mbfPP = new MBFProjectionProcessor();
				mbfPP.setMatrix(transform.times(renderToBounds));
				mbfPP.accumulate(toRender);
				mbfPP.performProjection(0, 0, renderer.getImage());
				
			}
		},
		VIDEO {
			private XuggleVideo toRender;
			private Matrix renderToBounds;

			@Override
			public void render(MBFImageRenderer renderer,Matrix transform, Rectangle rectangle) {
				if(toRender == null){
					toRender = new XuggleVideo(VideoColourSIFT.class.getResource("/org/openimaj/demos/video/keyboardcat.flv"),true);
					renderToBounds = TransformUtilities.makeTransform(new Rectangle(0,0,toRender.getWidth(), toRender.getHeight()), rectangle);
				}
				
				MBFProjectionProcessor mbfPP = new MBFProjectionProcessor();
				mbfPP.setMatrix(transform.times(renderToBounds));
				mbfPP.accumulate(toRender.getNextFrame());
				mbfPP.performProjection(0, 0, renderer.getImage());
			}
		};
		public abstract void render(MBFImageRenderer renderer, Matrix transform, Rectangle rectangle);
	}
	private VideoCapture capture;
	private VideoDisplay<MBFImage> videoFrame;
	private ImageComponent modelFrame;
	private ImageComponent matchFrame;

	private MBFImage modelImage;

	private ConsistentLocalFeatureMatcher2d<Keypoint> matcher;
	private DoGColourSIFTEngine engine;
	private PolygonDrawingListener polygonListener;
	private JPanel vidPanel;
	private JPanel modelPanel;
	private JPanel matchPanel;
	private RenderMode renderMode = RenderMode.SQUARE;

	public VideoColourSIFT(JComponent window) throws Exception {
		this(window, new VideoCapture(320, 240));
	}
	
	public VideoColourSIFT(JComponent window, VideoCapture capture) throws Exception {
		int width = capture.getWidth();
		int height = capture.getHeight();
		this.capture = capture; 
		polygonListener = new PolygonDrawingListener();

		GridBagConstraints gbc;

		vidPanel = new JPanel(new GridBagLayout());
		vidPanel.setBorder( BorderFactory.createTitledBorder( "Live Video" ) );
		videoFrame = VideoDisplay.createVideoDisplay(capture, vidPanel);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.PAGE_START;
		gbc.gridx = 0;
		window.add( vidPanel );

		modelPanel = new JPanel(new GridBagLayout());
		modelPanel.setBorder( BorderFactory.createTitledBorder( "Model" ) );
		modelFrame = new ImageComponent(true, false);
		modelFrame.setSize(width, height);
		modelFrame.setPreferredSize(new Dimension(width, height));
		modelPanel.add(modelFrame);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.PAGE_START;
		gbc.gridx = 1;
		window.add( modelPanel );

		matchPanel = new JPanel(new GridBagLayout());
		matchPanel.setBorder( BorderFactory.createTitledBorder( "Matches" ) );
		matchFrame = new ImageComponent(true, false);
		matchFrame.setSize(width*2, height);
		matchFrame.setPreferredSize(new Dimension(width*2, height));
		matchPanel.add(matchFrame);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.PAGE_END;
		gbc.gridy=1;
		gbc.gridwidth = 2;
		window.add( matchPanel, gbc);

		videoFrame.getScreen().addMouseListener(polygonListener);

		videoFrame.addVideoListener(this);
		engine = new DoGColourSIFTEngine();
		engine.getOptions().setDoubleInitialImage(false);
	}

	@Override
	public synchronized void keyPressed(KeyEvent key) {
		if(key.getKeyCode() == KeyEvent.VK_SPACE) {
			this.videoFrame.togglePause();
		} else if (key.getKeyChar() == 'c' && this.polygonListener.getPolygon().getVertices().size() > 2) {
			try {
				Polygon p = this.polygonListener.getPolygon().clone();
				this.polygonListener.reset();
				modelImage = capture.getCurrentFrame().process(new PolygonExtractionProcessor<Float[],MBFImage>(p,RGBColour.BLACK));

				if (matcher == null) {
					//configure the matcher
					HomographyModel model = new HomographyModel(3.0f);
					RANSAC<Point2d, Point2d> ransac = new RANSAC<Point2d, Point2d>(model, 1500, new RANSAC.ProbabilisticMinInliersStoppingCondition(0.01), true);
					matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(new FastBasicKeypointMatcher<Keypoint>(8));
					matcher.setFittingModel(ransac);

					modelPanel.setPreferredSize(modelPanel.getSize());
				} 

				modelFrame.setImage(ImageUtilities.createBufferedImageForDisplay(modelImage));

				DoGColourSIFTEngine engine = new DoGColourSIFTEngine();
				engine.getOptions().setDoubleInitialImage(true);

				matcher.setModelFeatures(engine.findFeatures(modelImage));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (key.getKeyChar() == '1'){
			renderMode = RenderMode.SQUARE;
		} else if (key.getKeyChar() == '2'){
			renderMode = RenderMode.PICTURE;
		} else if (key.getKeyChar() == '3'){
			renderMode = RenderMode.VIDEO;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) { }

	@Override
	public void keyTyped(KeyEvent arg0) { }

	@Override
	public synchronized void afterUpdate(VideoDisplay<MBFImage> display) {
		if (matcher != null && !videoFrame.isPaused()) {
			MBFImage capImg = videoFrame.getVideo().getCurrentFrame();
			LocalFeatureList<Keypoint> kpl = engine.findFeatures(capImg);

			MBFImageRenderer renderer = capImg.createRenderer();
			renderer.drawPoints(kpl, RGBColour.MAGENTA, 3);

			MBFImage matches;
			if (matcher.findMatches(kpl)) {
				try {
//					Shape sh = modelImage.getBounds().transform(((MatrixTransformProvider) matcher.getModel()).getTransform().inverse());
//					renderer.drawShape(sh, 3, RGBColour.BLUE);
					Matrix boundsToPoly = ((MatrixTransformProvider) matcher.getModel()).getTransform().inverse();
					renderMode.render(renderer,boundsToPoly,modelImage.getBounds());
				} catch (RuntimeException e) {}

				matches = MatchingUtilities.drawMatches(modelImage, capImg, matcher.getMatches(), RGBColour.RED);
			} else {
				matches = MatchingUtilities.drawMatches(modelImage, capImg, matcher.getMatches(), RGBColour.RED);
			}

			matchPanel.setPreferredSize(matchPanel.getSize());
			matchFrame.setImage(ImageUtilities.createBufferedImageForDisplay(matches));
		}
	}

	@Override
	public void beforeUpdate(MBFImage frame) {
		this.polygonListener.drawPoints(frame);
	}

	public void stop() {
		this.videoFrame.close();
		this.capture.stopCapture();
	}

	public static void main(String [] args) throws Exception {
		JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		window.setLayout(new GridBagLayout());
		JPanel c = new JPanel();
		c.setLayout(new GridBagLayout());
		window.getContentPane().add(c);

		VideoColourSIFT vs = new VideoColourSIFT(c);
		SwingUtilities.getRoot(window).addKeyListener(vs);
		window.pack();
		window.setVisible(true);
	}

}
