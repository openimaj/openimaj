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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.openimaj.demos.Demo;
import org.openimaj.feature.DoubleFV;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.colour.Transforms;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.renderer.MBFImageRenderer;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.ml.clustering.assignment.hard.ExactByteAssigner;
import org.openimaj.ml.clustering.random.RandomByteClusterer;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

/**
 * 	Demonstration of different feature extraction techniques
 * 	that produce a single global histogram for a given image. Currently
 * 	this includes RGB and HSV colour histograms, and a simple SIFT-based
 * 	Bag of Visual Words. The demo opens the first webcam and displays a
 * 	histogram of features. Press the space bar to toggle between the
 * 	different feature types.
 * 
 *  @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *	
 *	@created 15 Feb 2012
 */
@Demo(
		author = "Jonathon Hare", 
		description = "Demonstration of different feature extraction techniques " +
				"that produce a single global histogram for a given image. Currently " +
				"this includes RGB and HSV colour histograms, and a simple SIFT-based " +
				"Bag of Visual Words. The demo opens the first webcam and displays a " +
				"histogram of features. Press the space bar to toggle between the " +
				"different feature types.",
		keywords = { "features", "video", "histogram", "sift", "webcam", "bag-of-visual-words" }, 
		title = "Video Feature Extraction"
	)
public class VideoFeatureExtraction implements VideoDisplayListener<MBFImage>, KeyListener {
	private VideoCapture capture;
	private VideoDisplay<MBFImage> videoDisplay;
	private Mode mode = Mode.RGB_HISTOGRAM;
	private MBFImage histogramImage;
	private ImageComponent modelFrame;
	private JComponent modelPanel;
	
	/**
	 * 	Default constructor
	 *  @param window The window to display the demo in
	 *  @throws IOException
	 */
	public VideoFeatureExtraction(JComponent window) throws IOException {
		capture = new VideoCapture(640, 480);
		
		window.setLayout(new GridBagLayout());
		
		JPanel vidPanel = new JPanel(new GridBagLayout());
		vidPanel.setBorder( BorderFactory.createTitledBorder( "Live Video" ) );
		videoDisplay = VideoDisplay.createVideoDisplay(capture, vidPanel);
		videoDisplay.addVideoListener(this);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.PAGE_START;
		window.add( vidPanel, gbc);
		
		
		modelPanel = new JPanel(new GridBagLayout());
		modelPanel.setBorder( BorderFactory.createTitledBorder( "Feature type: " + mode.toString() ) );
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.PAGE_END;
		gbc.gridy = 1;
		window.add( modelPanel, gbc );
		
		modelFrame = new ImageComponent(true, false);
		modelPanel.add(modelFrame);
		histogramImage = new MBFImage(640, 60, ColourSpace.RGB);
		modelFrame.setImage(ImageUtilities.createBufferedImageForDisplay(histogramImage));
		
		((JFrame) SwingUtilities.getRoot(videoDisplay.getScreen())).addKeyListener(this);
	}
	
	@Override
	public void afterUpdate(VideoDisplay<MBFImage> display) {
		
	}

	@Override
	public synchronized void beforeUpdate(MBFImage frame) {
		DoubleFV histogram = mode.createFeature(frame);
		
		drawHistogramImage(histogram);
		modelFrame.setImage(ImageUtilities.createBufferedImageForDisplay(histogramImage));
	}
	
	private void drawHistogramImage(DoubleFV histogram) {
		histogram = histogram.normaliseFV();
		
		final int width = histogramImage.getWidth();
		final int height = histogramImage.getHeight();
		
		int bw = width / histogram.length();
		
		histogramImage.zero();
		MBFImageRenderer renderer = histogramImage.createRenderer();
		Rectangle s = new Rectangle();
		s.width = bw;
		for (int i=0; i<histogram.values.length; i++) {
			int rectHeight = (int) (histogram.values[i] * height);
			int remHeight = height - rectHeight;
			
			s.x = i * bw;
			s.y = remHeight;
			s.height = rectHeight;
			renderer.drawShapeFilled(s, mode.colourForBin(i));
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		//do nothing
	}

	@Override
	public synchronized void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == ' ') {
			int newOrdinal = mode.ordinal() + 1;
			if (newOrdinal >= Mode.values().length)
				newOrdinal = 0;
			
			mode = Mode.values()[newOrdinal];
			
			modelPanel.setBorder( BorderFactory.createTitledBorder( "Feature type: " + mode.toString() ) );
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		//do nothing				
	}
	
	/**
	 * 	Default main
	 *  @param args Command-line arguments
	 *  @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		JFrame window = new JFrame();
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		window.setLayout(new GridBagLayout());
		JPanel c = new JPanel();
		c.setLayout(new GridBagLayout());
		window.getContentPane().add(c);
		
		VideoFeatureExtraction vfe = new VideoFeatureExtraction(c);
		window.pack();
		window.setVisible(true);
		window.addKeyListener(vfe);
	}
}


enum Mode {
	RGB_HISTOGRAM {
		HistogramModel model = new HistogramModel(4,4,4);
		Float[][] binCols = null;
		
		@Override
		public String toString() {
			return "64-bin RGB Histogram";
		}
		
		@Override
		public DoubleFV createFeature(MBFImage image) {
			model.estimateModel(image);
			return model.histogram;
		}

		void buildBinCols() {
			binCols = new Float[4*4*4][3];
			for (int k=0; k<4; k++) {
				for (int j=0; j<4; j++) {
					for (int i=0; i<4; i++) {
						binCols[k*4*4 + j*4 + i][0] = (float)i/4 + (0.5f/4);
						binCols[k*4*4 + j*4 + i][1] = (float)j/4 + (0.5f/4);
						binCols[k*4*4 + j*4 + i][2] = (float)k/4 + (0.5f/4);
					}
				}
			}
		}
		
		@Override
		public Float[] colourForBin(int bin) {
			if (binCols == null) buildBinCols();
			
			return binCols[bin];
		}
	},
	HSV_HISTOGRAM {
		HistogramModel model = new HistogramModel(4,4,4);
		Float[][] binCols = null;
		
		@Override
		public String toString() {
			return "64-bin HSV Histogram";
		}
		
		@Override
		public DoubleFV createFeature(MBFImage image) {
			image = Transforms.RGB_TO_HSV(image);
			model.estimateModel(image);
			return model.histogram;
		}
		
		void buildBinCols() {
			binCols = new Float[4*4*4][];
			for (int k=0; k<4; k++) {
				for (int j=0; j<4; j++) {
					for (int i=0; i<4; i++) {
						float h = (float)i/4 + (0.5f/4);
						float s = (float)j/4 + (0.5f/4);
						float v = (float)k/4 + (0.5f/4);
						
						MBFImage img = new MBFImage(1,1,ColourSpace.HSV);
						img.setPixel(0, 0, new Float[] {h,s,v});
						
						img = Transforms.HSV_TO_RGB(img);
						
						binCols[k*4*4 + j*4 + i] = img.getPixel(0, 0);
					}
				}
			}
		}
		
		@Override
		public Float[] colourForBin(int bin) {
			if (binCols == null) buildBinCols();
			
			return binCols[bin];
		}
	},
	SIFT {
		ExactByteAssigner rabc = null;
		DoubleFV fv = null;
		DoGSIFTEngine engine = new DoGSIFTEngine();
		
		@Override
		public String toString() {
			return "100-term SIFT BoVW";
		}
		
		@Override
		public DoubleFV createFeature(MBFImage image) {
			if (rabc == null) {
				try {
					RandomByteClusterer clusterer = IOUtils.read(Mode.class.getResourceAsStream("/org/openimaj/demos/codebooks/random-100-highfield-codebook.voc"), RandomByteClusterer.class);
					rabc = new ExactByteAssigner(clusterer);
					fv = new DoubleFV(clusterer.numClusters());
					engine.getOptions().setDoubleInitialImage(false);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			FImage img = Transforms.calculateIntensity(image);
			img = ResizeProcessor.halfSize(img);
			List<Keypoint> keys = engine.findFeatures(img);
			
			for (Keypoint keypoint : keys) {
				image.drawPoint(new Point2dImpl(keypoint.x * 2f, keypoint.y * 2f), RGBColour.RED, 3);
			}
			
			Arrays.fill(fv.values, 0);
			
			for (Keypoint k : keys) {
				fv.values[rabc.assign(k.ivec)]++;
			}
			
			return fv;
		}

		@Override
		public Float[] colourForBin(int bin) {
			return RGBColour.RED;
		}
	}	
	;
	public abstract DoubleFV createFeature(MBFImage image);
	public abstract Float[] colourForBin(int bin);
}
