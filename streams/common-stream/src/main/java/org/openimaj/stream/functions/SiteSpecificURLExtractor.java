package org.openimaj.stream.functions;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.openimaj.io.HttpUtils;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.web.scraping.SiteSpecificConsumer;

/**
 * This class implements a function that will given an input URL outputs a list
 * of URLs based on applying a list of {@link SiteSpecificConsumer}s to the
 * input.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class SiteSpecificURLExtractor implements MultiFunction<URL, URL> {
	private static final Logger logger = Logger.getLogger(SiteSpecificURLExtractor.class);

	/**
	 * the site specific consumers
	 */
	protected List<SiteSpecificConsumer> siteSpecific = new ArrayList<SiteSpecificConsumer>();

	/**
	 * Construct with the given list of consumers.
	 * 
	 * @param consumers
	 *            the consumers
	 */
	public SiteSpecificURLExtractor(List<SiteSpecificConsumer> consumers) {
		this.siteSpecific = consumers;
	}

	/**
	 * Construct with the given consumers.
	 * 
	 * @param consumers
	 *            the consumers
	 */
	public SiteSpecificURLExtractor(SiteSpecificConsumer... consumers) {
		this.siteSpecific = Arrays.asList(consumers);
	}

	/**
	 * Construct with empty list of consumers.
	 */
	protected SiteSpecificURLExtractor() {
		this.siteSpecific = new ArrayList<SiteSpecificConsumer>();
	}

	@Override
	public List<URL> apply(URL in) {
		final List<URL> imageUrls = processURLs(in);

		if (imageUrls == null)
			return new ArrayList<URL>();

		return imageUrls;
	}

	/**
	 * First, try all the {@link SiteSpecificConsumer} instances loaded into
	 * {@link #siteSpecific}. If any consumer takes control of a link the
	 * consumer's output is used
	 * 
	 * if this fails use
	 * {@link HttpUtils#readURLAsByteArrayInputStream(URL, org.apache.http.client.RedirectStrategy)}
	 * with a {@link StatusConsumerRedirectStrategy} which specifically
	 * disallows redirects to be dealt with automatically and forces this
	 * function to be called for each redirect.
	 * 
	 * @param url
	 * @return a list of images or null
	 */
	protected List<URL> processURLs(URL url) {
		logger.debug("Resolving URL: " + url);
		logger.debug("Attempting site specific consumers");

		for (final SiteSpecificConsumer consumer : siteSpecific) {
			if (consumer.canConsume(url)) {
				logger.debug("Site specific consumer: " + consumer.getClass().getName() + " working on link");
				final List<URL> urlList = consumer.consume(url);

				if (urlList != null && !urlList.isEmpty()) {
					logger.debug("Site specific consumer returned non-null, returning the URLs");

					return urlList;
				}
			}
		}
		return null;
	}
}
