package org.openimaj.demos.twitter;

import java.io.IOException;
import java.net.URL;

import org.openimaj.data.dataset.StreamingDataset;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.ArrayBlockingDroppingQueue;
import org.openimaj.util.function.Operation;

import twitter4j.Status;

public class TwitterStreamingPlayground {
	public static void main(String[] args) {
		final TwitterAPIToken token = DefaultTokenFactory.getInstance().getToken(TwitterAPIToken.class);

		final ArrayBlockingDroppingQueue<Status> buffer = new ArrayBlockingDroppingQueue<Status>(1);
		final StreamingDataset<Status> dataset = new TwitterStreamingDataset(token, buffer);

		dataset.map(new TwitterLinkExtractor())
				.map(new ImageURLExtractor())
				.forEach(new Operation<URL>() {
					@Override
					public void perform(URL url) {
						System.out.println(buffer.dropCount());
						try {
							DisplayUtilities.displayName(ImageUtilities.readMBF(url), "image");
						} catch (final IOException e) {
							e.printStackTrace();
						}
					}
				});
	}
}
