package org.openimaj.demos.sandbox.twitter.images;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

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

		imageStream.forEach(new Operation<MBFImage>() {
			int currentX = 0;
			int currentY = 0;
			MBFImage base = new MBFImage(320 * 3, 320 * 3, ColourSpace.RGB);
			ResizeProcessor rp = new ResizeProcessor(320);

			@Override
			public void perform(MBFImage object) {
				MBFImage r = object.process(rp);

				final int dx = (320 - r.getWidth()) / 2;
				final int dy = (320 - r.getHeight()) / 2;

				r = r.padding(dx, dy, RGBColour.WHITE);
				base.drawImage(r, currentX * 320, currentY * 320);
				currentX++;
				if (currentX == 3) {
					currentY++;
					currentX = 0;
				}
				if (currentY == 3)
					currentY = 0;

				DisplayUtilities.displayName(base, "image");
			}
		});
	}
}
