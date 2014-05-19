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
package org.openimaj.demos.sandbox.twitter.images;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.swing.JFrame;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.stream.functions.ImageFromURL;
import org.openimaj.stream.functions.ImageSiteURLExtractor;
import org.openimaj.stream.functions.twitter.TwitterURLExtractor;
import org.openimaj.stream.provider.twitter.TwitterStreamDataset;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.function.Operation;
import org.openimaj.util.stream.BlockingDroppingBufferedStream;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;

import twitter4j.Status;

public class DisplayTwitterImages {
	/**
	 * Main method
	 * 
	 * @param args
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		/*
		 * Construct a twitter stream with an
		 */
		final TwitterAPIToken token = DefaultTokenFactory.get(TwitterAPIToken.class);
		final Stream<Status> stream = new TwitterStreamDataset(token);

		final ArrayBlockingDroppingQueue<MBFImage> buffer = new ArrayBlockingDroppingQueue<MBFImage>(10);
		final BlockingDroppingBufferedStream<MBFImage> imageStream = new BlockingDroppingBufferedStream<MBFImage>(buffer);

		new Thread(new Runnable() {
			@Override
			public void run() {
				stream.parallelForEach(new Operation<Status>() {
					@Override
					public void perform(Status object) {

						final Stream<URL> imageUrlStream = new CollectionStream<URL>(new TwitterURLExtractor()
								.apply(object))
								.map(new ImageSiteURLExtractor(false, true));

						// Get images
						final Stream<MBFImage> imageStream = imageUrlStream.map(ImageFromURL.MBFIMAGE_EXTRACTOR);

						final boolean[] foundImages = { false };
						imageStream.forEach(new Operation<MBFImage>() {
							@Override
							public void perform(MBFImage image) {
								buffer.offer(image);
								foundImages[0] = true;
							}
						});
						System.out.println(foundImages[0]);
					}
				});
			}
		}).start();
		final int N_ROWS = 10; 
		final int IMAGE_WH= 50;
		final MBFImage b = new MBFImage(IMAGE_WH * N_ROWS, IMAGE_WH * N_ROWS, ColourSpace.RGB);
		final ResizeProcessor rp = new ResizeProcessor(IMAGE_WH);
		final JFrame f = DisplayUtilities.displaySimple(b, "image");
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice dev = env.getScreenDevices()[1];
		dev.setFullScreenWindow(f);
		final MBFImage base = new MBFImage(f.getWidth(), f.getHeight(), ColourSpace.RGB);
		imageStream.forEach(new Operation<MBFImage>() {
			
			int currentX = 0;
			int currentY = 0;
			@Override
			public void perform(MBFImage object) {
				MBFImage r = object.process(rp);

				final int dx = (IMAGE_WH - r.getWidth()) / 2;
				final int dy = (IMAGE_WH - r.getHeight()) / 2;

				r = r.padding(dx, dy, RGBColour.WHITE);
				base.drawImage(r, currentX * IMAGE_WH, currentY * IMAGE_WH);
				currentX++;
				if (currentX == base.getWidth()/IMAGE_WH) {
					currentY++;
					currentX = 0;
				}
				if (currentY == base.getHeight()/IMAGE_WH)
					currentY = 0;

				DisplayUtilities.display(base, f);
				
			}
		});
	}
}
