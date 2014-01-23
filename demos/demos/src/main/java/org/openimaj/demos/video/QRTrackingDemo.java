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
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.openimaj.demos.Demo;
import org.openimaj.image.DisplayUtilities.ImageComponent;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.transform.MBFProjectionProcessor;
import org.openimaj.image.typography.general.GeneralFont;
import org.openimaj.image.typography.general.GeneralFontRenderer;
import org.openimaj.image.typography.general.GeneralFontStyle;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.geometry.transforms.TransformUtilities;
import org.openimaj.util.pair.Pair;
import org.openimaj.video.Video;
import org.openimaj.video.VideoDisplay;
import org.openimaj.video.VideoDisplayListener;
import org.openimaj.video.capture.VideoCapture;

import cern.colt.Arrays;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

/**
 * A demo of ZXing using OpenIMAJ to grab images from the camera
 * 
 * @author Mike Cook
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @created 28 Sep 2011
 */
@Demo(author = "Michael Cook", description = "Using ZXing, QR image rendering with OpenIMAJ", keywords = { "video" }, title = "QR Code Tracking")
public class QRTrackingDemo extends JPanel implements
VideoDisplayListener<MBFImage> {
	/** */
	private static final long serialVersionUID = 1L;

	/** The video */
	private Video<MBFImage> video;
	/** The video display which will play the video */
	private VideoDisplay<MBFImage> videoDisplay;
	/** The image component into which the video is being painted (reused) */
	private final ImageComponent ic;
	/** The thread which is running the video playback */
	private Thread videoThread;

	/** Mike: Need to deal in BufferedImage's for QR code reader */
	private BufferedImage bimg;
	/** Mike: added MBFImage for processing frames */
	private MBFImage toDraw;
	/** Mike: added qr code reader class */
	com.google.zxing.Reader reader;
	/** Mike: add last image string */
	String lastImage; // This will have the cover image
	/** The QR points matched*/
	List<Pair<Point2d>> points = new ArrayList<Pair<Point2d>>();
	/** Time QR points were last matched */
	long timeLastMatched;

	/**
	 * Default constructor.
	 * 
	 * @throws IOException
	 */
	public QRTrackingDemo() throws IOException {
		this.ic = new ImageComponent(true);
		this.ic.setPreferredSize(new Dimension(320, 240));
		this.toDraw = new MBFImage(320, 240, 3);
		// Now test to see if it has a QR code embedded in it
		this.reader = new com.google.zxing.qrcode.QRCodeReader();
		this.lastImage = "";
		this.add(this.ic);
	}

	/**
	 * 
	 Set the video source to be the webcam
	 * 
	 * @throws IOException
	 */
	public void useWebcam() throws IOException {
		// Setup a new video from the VideoCapture class
		this.video = new VideoCapture(320, 240);
		// Reset the video displayer to use the capture class
		this.videoDisplay = new VideoDisplay<MBFImage>(this.video, this.ic);
		// Make sure the listeners are sorted
		this.videoDisplay.addVideoListener(this);
		// Start the new video playback thread
		this.videoThread = new Thread(this.videoDisplay);
		this.videoThread.start();
	}



	/**
	 * {@inheritDoc}
	 * @see org.openimaj.video.VideoDisplayListener#afterUpdate(org.openimaj.video.VideoDisplay)
	 */
	@Override
	public void afterUpdate(final VideoDisplay<MBFImage> display) {
		if(System.currentTimeMillis() - this.timeLastMatched > 100){
			this.points.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.openimaj.video.VideoDisplayListener#beforeUpdate(org.openimaj.image.Image)
	 */
	@Override
	public void beforeUpdate(final MBFImage frame) {
		this.bimg = ImageUtilities.createBufferedImageForDisplay(frame, this.bimg);
		final LuminanceSource source = new BufferedImageLuminanceSource(this.bimg);

		final BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

		try {
			final Result result = this.reader.decode(bitmap);
			if (result.getText() != null) {
				if( !result.getText().equals( this.lastImage ))
				{
					this.lastImage = result.getText();
					try {
						final MBFImage img = new MBFImage( 200, 50, 3 );

						final GeneralFont font = new GeneralFont( "Arial", Font.PLAIN );
						final GeneralFontStyle<Float[]> gfs = new GeneralFontStyle<Float[]>( font, img.createRenderer(), false );
						final GeneralFontRenderer<Float[]> gfr = new GeneralFontRenderer<Float[]>();
						final Rectangle b = gfr.getSize( this.lastImage, gfs );

						final MBFImage img2 = new MBFImage( (int)b.width, (int)(b.height*1.3), 3 );

						img2.drawText( this.lastImage, 0, (int)b.height, font, 30 );
						this.toDraw = img2;
					}

					catch (final Exception e) {
						e.printStackTrace();
						System.out.println("could not read url");
					}
				}

				try {
					final ResultPoint[] rpoints = result.getResultPoints();
					System.out.println( Arrays.toString( rpoints ) );
					this.points.clear();
					if( rpoints.length >= 3 )
					{
						float s = 1;
						float xx1 = 0;
						float xx2 = 0;
						for (int i = 2; i >= 0; i--) {
							final Point2dImpl pa = new Point2dImpl(rpoints[i].getX(),rpoints[i].getY());
							Point2dImpl pb = null;
							if(i == 1) {
								pb = new Point2dImpl(0,0);
								xx1 = pa.x;
							}
							if(i == 2) {
								pb = new Point2dImpl(this.toDraw.getWidth(),0);
								xx2 = pa.x;
							}
							if(i == 0) {
								s = this.toDraw.getWidth() / (xx2 - xx1) * 3;
								pb = new Point2dImpl( 0, this.toDraw.getHeight()*s );
							}
							this.points.add(new Pair<Point2d>(pa,pb));
						}
					}
					this.timeLastMatched = System.currentTimeMillis();
					//					frame.createRenderer().drawImage(toDraw, x, y);


				} catch (final Exception e) {
					System.out.println("could not find image");
				}

			}
		} catch (final Exception e) {

		}
		if(this.points.size()>2){
			final MBFProjectionProcessor pp = new MBFProjectionProcessor();
			pp.accumulate(frame);
			pp.setMatrix(TransformUtilities.affineMatrix(this.points).inverse());
			pp.accumulate(this.toDraw);
			frame.internalAssign(pp.performProjection());
		}


	}

	/**
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		try {

			final QRTrackingDemo demo = new QRTrackingDemo();
			final JFrame f = new JFrame("Video Processing Demo -- Mike");
			f.getContentPane().add(demo);
			f.pack();
			f.setVisible(true);
			demo.useWebcam();
			// demo.useFile(new
			// File("/Users/ss/Downloads/20070701_185500_bbcthree_doctor_who_confidential.ts"));
		} catch (final HeadlessException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}