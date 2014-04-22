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
