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

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.text.DecimalFormat;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.capture.VideoCaptureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example showing how the built-in OpenIMAJ fps counter works and how to 
 * dipsplay real-time fps. Since {@link VideoDisplay#getDisplayFPS()} reports 
 * FPS speed with which display is updated, it is not necessairly the speed 
 * at which FPS is rendered out to the display. This program shows this 
 * discrepancy. Notice that while "capture" FPS reports at the consistent 
 * range, depending on frame delay set, the actual render FPS may be quite 
 * different. This demo implementation uses simple thread sleep to simulate 
 * expensive rendering operation.
 * 
 * @author Adam Zimowski (mrazjava)
 */
public class VideoCaptureFramesExample extends KeyAdapter {
	
	private static final Logger log = LoggerFactory.getLogger(VideoCaptureFramesExample.class);

	VideoCapture vc;
	VideoDisplay<MBFImage> display;
	Thread displayThread;
	
	private static final String DELAY_LBL = "Delay (ms): ";
	
	private int fpsDelayMillis = 0;
	
	private final JLabel fpsDelayLabel;
	
	private JTextField fpsDelayText;

	/**
	 * @throws VideoCaptureException
	 */
	public VideoCaptureFramesExample() throws VideoCaptureException {
		
		this.fpsDelayLabel = new JLabel(VideoCaptureFramesExample.DELAY_LBL + this.fpsDelayMillis);
		
		// open the capture device and create a window to display in 320, 240
		this.vc = new VideoCapture(1024, 768);

		
		final FrameDemoImageComponent ic = new FrameDemoImageComponent();
		ic.setAllowZoom( false );
		ic.setAllowPanning( false );
		ic.setTransparencyGrid( false );
		ic.setShowPixelColours( false );
		ic.setShowXYPosition( true );		

		this.buildGui(ic).setVisible(true);
		
		this.display = new VideoDisplay<MBFImage>( this.vc, null, ic );
		this.display.addVideoListener(ic);
		
		this.displayThread = new Thread(this.display);
		this.displayThread.start();
	}
	
	/**
	 * @param ic frame displaying component
	 * @return
	 */
	private JFrame buildGui(final ImageComponent ic) {
		
		final JFrame win = new JFrame("Frame Counting Demo");
		win.setPreferredSize(new Dimension(640, 480));
		win.getContentPane().setLayout(new BorderLayout());
		
		final JPanel controlPanel = new JPanel(new BorderLayout());
		final JPanel setDelayPanel = new JPanel(new FlowLayout());
		
		final JButton setButton = new JButton("Set Delay");
		setButton.setPreferredSize(new Dimension(150, 20));
		setButton.setMinimumSize(new Dimension(75, 20));
		setButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				try {
					final int delay = Integer.valueOf(VideoCaptureFramesExample.this.fpsDelayText.getText());
					if(delay < 0) throw new NumberFormatException();
					VideoCaptureFramesExample.this.fpsDelayMillis = delay;
					VideoCaptureFramesExample.this.fpsDelayLabel.setText(VideoCaptureFramesExample.DELAY_LBL + VideoCaptureFramesExample.this.fpsDelayText.getText());
				}
				catch(final NumberFormatException nfe) {
					VideoCaptureFramesExample.this.fpsDelayText.setText(Integer.toString(VideoCaptureFramesExample.this.fpsDelayMillis));
				}
			}
		});
		this.fpsDelayText = new JTextField();
		this.fpsDelayText.setText(Integer.toString(this.fpsDelayMillis));
		this.fpsDelayText.setPreferredSize(new Dimension(100, 20));
		this.fpsDelayText.setMinimumSize(new Dimension(25, 20));
		
		setDelayPanel.add(this.fpsDelayText);
		setDelayPanel.add(setButton);
		
		controlPanel.add(this.fpsDelayLabel, BorderLayout.WEST);
		controlPanel.add(Box.createGlue(), BorderLayout.CENTER);
		controlPanel.add(setDelayPanel, BorderLayout.EAST);
		
		win.getContentPane().add(ic, BorderLayout.CENTER);
		win.getContentPane().add(controlPanel, BorderLayout.SOUTH);
		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		win.pack();

		return win;
	}
	
	class FrameDemoImageComponent extends ImageComponent implements VideoDisplayListener<MBFImage> {

		private static final long serialVersionUID = 1618169267341185294L;

		private final float FPS_BOX_ALPHA = 0.45f;
		
		private final int FONT_SIZE = 10;
		
		private final int AVG_SAMPLE = 20;
		
		private long lastFrameTimestamp = 0;
		
		private double renderedWorstFps = Double.MAX_VALUE;
		
		private double renderedBestFps = 0d;
		
		private final double[] renderedSampleFps = new double[this.AVG_SAMPLE];
		
		private double capturedWorstFps = Double.MAX_VALUE;
		
		private double capturedBestFps = 0d;
		
		private final double[] capturedSampleFps = new double[this.AVG_SAMPLE];
		
		private long framesRendered = 0;
		
		private final DecimalFormat df = new DecimalFormat("#.#######");
		
		private double renderedFps;
		
		private long timedFps;
		
		
		public FrameDemoImageComponent() {
		}
		
		private double getFps() {
			final long currentFrameTimestamp = System.currentTimeMillis();
			final double fps = 1000d/(currentFrameTimestamp - this.lastFrameTimestamp);
			this.lastFrameTimestamp = currentFrameTimestamp;
			return fps;
		}
		
		private double computeRenderedAvgFps() {
			double avgFps = 0d;
			for(final double fps : this.renderedSampleFps) {
				if(fps > 0d) avgFps += fps;
			}
			return avgFps / this.renderedSampleFps.length;
		}
		
		private double computeCapturedAvgFps() {
			double avgFps = 0d;
			for(final double fps : this.capturedSampleFps) {
				if(fps > 0d) avgFps += fps;
			}
			return avgFps / this.capturedSampleFps.length;			
		}
		
		private String[] buildFpsMessages() {
			
			final int currentSampleIndex = (int)(this.framesRendered % this.AVG_SAMPLE);
			
			final double capturedAvgFps = this.computeCapturedAvgFps();
			this.capturedSampleFps[currentSampleIndex] = VideoCaptureFramesExample.this.display.getDisplayFPS();
			final double renderedAvgFps = this.computeRenderedAvgFps();
			this.renderedSampleFps[currentSampleIndex] = this.renderedFps;
			
			final double tmpRenderedWorstFps = Math.min(this.renderedSampleFps[currentSampleIndex], this.renderedWorstFps);
			// we won't report zero as a valid worst fps
			if(tmpRenderedWorstFps > 7.367059952818556E-10) {
				this.renderedWorstFps = tmpRenderedWorstFps;
			}
			final double tmpRenderedBestFps = Math.max(this.renderedSampleFps[currentSampleIndex], this.renderedBestFps);
			// consider best within the mans of reasonable deviation
			//if(tmpRenderedBestFps > renderedBestFps && tmpRenderedBestFps < (renderedAvgFps*2)) {
			if(tmpRenderedBestFps > this.renderedBestFps) {
				this.renderedBestFps = tmpRenderedBestFps;
			}

			final double tmpCapturedWorstFps = Math.min(this.capturedSampleFps[currentSampleIndex], this.capturedWorstFps);
			// we won't report zero as a valid worst fps
			if(tmpCapturedWorstFps > 7.367059952818556E-10) {
				this.capturedWorstFps = tmpCapturedWorstFps;
			}
			final double tmpCapturedBestFps = Math.max(this.capturedSampleFps[currentSampleIndex], this.capturedBestFps);
			// consider best within the mans of reasonable deviation
			//if(tmpCapturedBestFps > capturedBestFps && tmpCapturedBestFps < (capturedAvgFps*2)) {
			if(tmpCapturedBestFps > this.capturedBestFps) {
				this.capturedBestFps = tmpCapturedBestFps;
			}
			
			return new String[] {
					"Rendered Live FPS: " + this.df.format(this.renderedSampleFps[currentSampleIndex]),
					"Rendered Avg FPS: " + this.df.format(renderedAvgFps), 
					"Rendered Worst FPS: " + this.df.format(this.renderedWorstFps),
					"Rendered Best FPS: " + this.df.format(this.renderedBestFps),
					"Captured FSP: " + this.df.format(this.capturedSampleFps[currentSampleIndex]), 
					"Captured Avg FPS: " + this.df.format(capturedAvgFps), 
					"Captured Worst FPS: " + this.df.format(this.capturedWorstFps), 
					"Captured Best FPS: " + this.df.format(this.capturedBestFps)
			};
		}
		
		private void renderFpsStats(final Graphics g) {
			// show fps stats
			if(VideoCaptureFramesExample.this.display != null) {
				final String[] fps = this.buildFpsMessages();
				final int type = AlphaComposite.SRC_OVER; 
				final AlphaComposite composite = AlphaComposite.getInstance(type, this.FPS_BOX_ALPHA);
				final Graphics2D g2 = (Graphics2D) g.create();
			    g2.setComposite(composite);
				g2.setColor(Color.DARK_GRAY);
				final Font font = new Font("SansSerif", Font.PLAIN, this.FONT_SIZE);
				final FontMetrics fm = g.getFontMetrics(font);				
				g2.fillRect(0, 0, this.getWidth(), (fm.getHeight()*fps.length)+(this.FONT_SIZE/2));
				g2.dispose();
				if(this.framesRendered > 0) {
					g.setFont(font);
					g.setColor(Color.WHITE);
					int x = 1;
					final int xOffset = 3;
					for(; x<=(fps.length/2); ++x) {
						g.drawString(fps[x-1], xOffset, fm.getHeight()*x);
					}
					g.setColor(Color.YELLOW);
					for(; x<=fps.length; ++x) {
						g.drawString(fps[x-1], xOffset, fm.getHeight()*x);
					}
					g.setColor(Color.CYAN);
					final String timedFpsStr = "Timed FPS: ";
					final int timedFpsStrWidth = (int)(fm.getStringBounds(timedFpsStr + 1000, g).getWidth());
					g.drawString(timedFpsStr + this.timedFps, this.getWidth()-timedFpsStrWidth, fm.getHeight());
				}
				else {
					g.setColor(Color.YELLOW);
					g.drawString("Initializing ...", 3, fm.getHeight());
				}
			}			
		}
		
		private boolean slowFrameInProgress = false;
		private Image slowFrame = null;
		
		class FrameRenderer extends SwingWorker<Void, Void> {
			
			private final int width;
			private final int height;
			
			public FrameRenderer(final int frameWidth, final int frameHeight) {
				this.width = frameWidth;
				this.height = frameHeight;
			}
			
			@Override
			public Void doInBackground() {
				FrameDemoImageComponent.this.slowFrameInProgress = true;
				FrameDemoImageComponent.this.slowFrame = FrameDemoImageComponent.this.image.getScaledInstance(this.width, this.height, Image.SCALE_FAST);
				try {
					// simulating a really expensive frame rendering operation
					Thread.sleep(VideoCaptureFramesExample.this.fpsDelayMillis);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void done() {
				FrameDemoImageComponent.this.renderedFps = FrameDemoImageComponent.this.getFps();
				FrameDemoImageComponent.this.framesRendered++;
				FrameDemoImageComponent.this.slowFrameInProgress = false;
			}			
		}

		@Override
		public void paint(final Graphics g) {
			if(VideoCaptureFramesExample.this.display != null) {
				if(VideoCaptureFramesExample.this.fpsDelayMillis > 0) {
					if(!this.slowFrameInProgress) {
						new FrameRenderer(this.getWidth(), this.getHeight()).execute();
					}
					if(this.slowFrame != null) {
						g.drawImage(this.slowFrame, 0, 0, null);
					}
					else {
						// rather than having blank display that one time when 
						// slow frame scales for the first time, let it render 
						// from base but don't track frame count since this is 
						// just an eye-candy effect
						super.paint(g);
					}
				}
				else {
					if(this.framesRendered > 0) {
						// we are rendering at base (OpenIMAJ) speed.
						super.paint(g);
						this.framesRendered++;
						this.renderedFps = this.getFps();
					}
					else {
						// container is ready to paint us but cam (display var) 
						// is still initializing
						VideoCaptureFramesExample.log.debug(null);
					}
				}
				this.renderFpsStats(g);
			}
		}

		@Override
		public void afterUpdate(final VideoDisplay<MBFImage> display) {
			// tell us when frames begin to appear on display (they likely 
			// will have been already streamed from the device)
			if(this.framesRendered == 0) {
				this.framesRendered++;
				new Timer(1000, this.timerListener).start();
			}
		}

		@Override
		public void beforeUpdate(final MBFImage frame) {
			// not needed
		}
		
		private final ActionListener timerListener = new ActionListener() {
			private long lastFrameCount = 0;
			@Override
			public void actionPerformed(final ActionEvent e) {
				final long rendered = FrameDemoImageComponent.this.framesRendered;
				VideoCaptureFramesExample.log.debug("lastFrameCount: {}, framesRendered: {}", this.lastFrameCount, rendered);
				FrameDemoImageComponent.this.timedFps = rendered - this.lastFrameCount;
				this.lastFrameCount = rendered;			
			}
		};
	}
	
	/**
	 * Runs the program contained in this class.
	 * 
	 * @param args not used
	 * @throws VideoCaptureException
	 *             if their is a problem with the video capture hardware
	 */
	public static void main(final String[] args) throws VideoCaptureException {
		new VideoCaptureFramesExample();
		//double tmp = Math.min(1.2341231234d, Double.MAX_VALUE);
		//System.out.println(tmp);
	}
}
