package org.openimaj.demos.twitter;

import java.net.URL;

import org.openimaj.data.dataset.StreamingDataset;
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

		// Parallel.forEachUnpartioned(
		// dataset.map(new Function<Status, URL>() {
		//
		// @Override
		// public URL apply(Status status) {
		// if (status.getURLEntities() != null) {
		// for (final URLEntity map : status.getURLEntities()) {
		// String u = map.getExpandedURL();
		//
		// if (u == null) {
		// u = map.getURL();
		//
		// if (u == null)
		// continue;
		// }
		//
		// try {
		// return new URL(u);
		// } catch (final MalformedURLException e) {
		// }
		// }
		// }
		// return null;
		// }
		// }),
		// new Operation<URL>() {
		// @Override
		// public void perform(URL object) {
		// if (object != null)
		// System.out.println(object);
		// System.out.println(buffer.dropCount());
		// }
		// },
		// GlobalExecutorPool.getPool());

		dataset.map(new TwitterLinkExtractor())
				// .map(new ImageURLExtractor())
				.parallelForEach(new Operation<URL>() {
					@Override
					public void perform(URL url) {
						System.out.println(buffer.dropCount() + " " + buffer.insertCount());
						// try {
						// DisplayUtilities.displayName(ImageUtilities.readMBF(url),
						// "image");
						// } catch (final IOException e) {
						// e.printStackTrace();
						// }
					}
				});
	}
}
