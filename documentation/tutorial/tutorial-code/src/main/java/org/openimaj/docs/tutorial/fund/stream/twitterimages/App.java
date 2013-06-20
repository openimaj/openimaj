package org.openimaj.docs.tutorial.fund.stream.twitterimages;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.stream.functions.ImageFromURL;
import org.openimaj.stream.functions.ImageSiteURLExtractor;
import org.openimaj.stream.functions.twitter.TwitterURLExtractor;
import org.openimaj.stream.provider.twitter.TwitterStreamDataset;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.function.Operation;
import org.openimaj.util.stream.Stream;

/**
 * OpenIMAJ Hello world!
 * 
 */
public class App {
	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		 * Construct a twitter stream with an
		 */
		final TwitterAPIToken token = DefaultTokenFactory.get(TwitterAPIToken.class);
		final TwitterStreamDataset stream = new TwitterStreamDataset(token);

		/*
		 * Consume the stream and print the tweets
		 */
		// stream.forEach(new Operation<Status>() {
		// @Override
		// public void perform(Status status) {
		// System.out.println(status.getText());
		// }
		// });

		// Get the URLs
		final Stream<URL> urlStream = stream.map(new TwitterURLExtractor());

		// Transform/filter to get potential image URLs
		final Stream<URL> imageUrlStream = urlStream.map(new ImageSiteURLExtractor(false));

		// Get images
		final Stream<MBFImage> imageStream = imageUrlStream.map(ImageFromURL.MBFIMAGE_EXTRACTOR);

		/*
		 * Display images
		 */
		// imageStream.forEach(new Operation<MBFImage>() {
		// @Override
		// public void perform(MBFImage image) {
		// DisplayUtilities.displayName(image, "image");
		// }
		// });

		imageStream.map(new MultiFunction<MBFImage, MBFImage>() {
			HaarCascadeDetector detector = HaarCascadeDetector.BuiltInCascade.frontalface_default.load();

			@Override
			public List<MBFImage> apply(MBFImage in) {
				final List<DetectedFace> detected = detector.detectFaces(in.flatten());

				final List<MBFImage> faces = new ArrayList<MBFImage>();
				for (final DetectedFace face : detected)
					faces.add(in.extractROI(face.getBounds()));

				return faces;
			}
		}).forEach(new Operation<MBFImage>() {
			@Override
			public void perform(MBFImage image) {
				DisplayUtilities.displayName(image, "image");
			}
		});
	}
}
