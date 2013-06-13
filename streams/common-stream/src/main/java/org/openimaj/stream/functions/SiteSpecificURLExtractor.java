package org.openimaj.stream.functions;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.openimaj.image.ImageUtilities;
import org.openimaj.io.HttpUtils;
import org.openimaj.io.HttpUtils.MetaRefreshRedirectStrategy;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.web.scraping.SiteSpecificConsumer;

import com.google.common.collect.Lists;

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
	private List<SiteSpecificConsumer> siteSpecific = new ArrayList<SiteSpecificConsumer>();

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

	@Override
	public List<URL> apply(URL in) {
		System.out.println(in);

		final List<URL> imageUrls = urlToImage(in);

		if (imageUrls == null)
			return new ArrayList<URL>();

		return imageUrls;
	}

	/**
	 * An extension of the {@link MetaRefreshRedirectStrategy} which disallows
	 * all redirects and instead remembers a redirect for use later on.
	 * 
	 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
	 */
	private static class StatusConsumerRedirectStrategy extends MetaRefreshRedirectStrategy {
		private boolean wasRedirected = false;
		private URL redirection;

		@Override
		public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
				throws ProtocolException
		{
			wasRedirected = super.isRedirected(request, response, context);

			if (wasRedirected) {
				try {
					this.redirection = this.getRedirect(request, response, context).getURI().toURL();
				} catch (final MalformedURLException e) {
					this.wasRedirected = false;
				}
			}
			return false;
		}

		/**
		 * @return whether a redirect was found
		 */
		public boolean wasRedirected() {
			return wasRedirected;
		}

		/**
		 * @return the redirection
		 */
		public URL redirection() {
			return redirection;
		}
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
	private List<URL> urlToImage(URL url) {
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

		try {
			logger.debug("Site specific consumers failed, trying the raw link");

			final StatusConsumerRedirectStrategy redirector = new StatusConsumerRedirectStrategy();
			final IndependentPair<HttpEntity, ByteArrayInputStream> headersBais = HttpUtils
					.readURLAsByteArrayInputStream(url, 1000, 1000, redirector, HttpUtils.DEFAULT_USERAGENT);

			if (redirector.wasRedirected()) {
				logger.debug("Redirect intercepted, adding redirection to list");

				final URL redirect = redirector.redirection();
				if (!redirect.toString().equals(url.toString()))
					return urlToImage(redirect);
			}

			// at this point any redirects have been resolved and the content
			// can't be handled by any of the SSCs
			// we now check to see if it's image data

			final HttpEntity headers = headersBais.firstObject();
			final ByteArrayInputStream bais = headersBais.getSecondObject();

			final String typeValue = headers.getContentType().getValue();
			if (typeValue.contains("text")) {
				logger.debug(url + " ignored -- text content");
				return null;
			} else {
				// Not text? try reading it as an image!
				if (typeValue.contains("gif")) {
					// It is a gif! just download it normally (i.e. null image
					// but not null URL)
					return Lists.newArrayList(url);
				} else {
					// otherwise just try to read the damn image
					ImageUtilities.readMBF(bais);
					return Lists.newArrayList(url);
				}
			}
		} catch (final Throwable e) {
			// This input is probably not an image!
			logger.debug(url + " ignored -- exception", e);

			return null;
		}
	}
}
