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
package org.openimaj.stream.functions;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.web.scraping.SiteSpecificConsumer;
import org.openimaj.web.scraping.images.CommonHTMLConsumers;
import org.openimaj.web.scraping.images.FacebookConsumer;
import org.openimaj.web.scraping.images.ImgurConsumer;
import org.openimaj.web.scraping.images.InstagramConsumer;
import org.openimaj.web.scraping.images.OwlyImageConsumer;
import org.openimaj.web.scraping.images.TmblrPhotoConsumer;
import org.openimaj.web.scraping.images.TwipleConsumer;
import org.openimaj.web.scraping.images.TwitPicConsumer;
import org.openimaj.web.scraping.images.TwitterPhotoConsumer;
import org.openimaj.web.scraping.images.YfrogConsumer;

import com.google.common.collect.Lists;

/**
 * This class implements a function that will given an input URL outputs a list
 * of URLs to the possible images related to the input URL. This works by using
 * a set of {@link SiteSpecificConsumer}s for common image hosting sites to
 * determine if the input URL is likely to lead to an image of images.
 * <p>
 * Currently, the following consumers are included:
 * <ul>
 * <li> {@link InstagramConsumer}
 * <li> {@link TwitterPhotoConsumer}
 * <li> {@link TmblrPhotoConsumer}
 * <li> {@link TwitPicConsumer}
 * <li> {@link ImgurConsumer}
 * <li> {@link FacebookConsumer}
 * <li> {@link YfrogConsumer}
 * <li> {@link OwlyImageConsumer}
 * <li> {@link TwipleConsumer}
 * <li> {@link CommonHTMLConsumers#FOTOLOG}
 * <li> {@link CommonHTMLConsumers#PHOTONUI}
 * <li> {@link CommonHTMLConsumers#PICS_LOCKERZ}
 * </ul>
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ImageSiteURLExtractor extends SiteSpecificURLExtractor {
	private static final Logger logger = Logger.getLogger(ImageSiteURLExtractor.class);

	private boolean fallback = false;

	/**
	 * Construct with or without Tumblr support
	 *
	 * @param tumblr
	 *            true if tumblr is required.
	 * @param fallback
	 *            true if should try to download directly
	 */
	public ImageSiteURLExtractor(boolean tumblr, boolean fallback) {
		this(tumblr);
		this.fallback = fallback;
	}

	/**
	 * Construct with or without Tumblr support
	 *
	 * @param tumblr
	 *            true if tumblr is required.
	 */
	public ImageSiteURLExtractor(boolean tumblr) {
		super();

		siteSpecific.addAll(Arrays.asList(
				new TwitterPhotoConsumer(),
				new InstagramConsumer(),
				new TwitPicConsumer(),
				new ImgurConsumer(),
				new FacebookConsumer(),
				new YfrogConsumer(),
				new OwlyImageConsumer(),
				new TwipleConsumer(),
				CommonHTMLConsumers.FOTOLOG,
				CommonHTMLConsumers.PHOTONUI,
				CommonHTMLConsumers.PICS_LOCKERZ));

		if (tumblr)
			siteSpecific.add(new TmblrPhotoConsumer());
	}

	/**
	 * Default constructor; includes tumblr support.
	 */
	public ImageSiteURLExtractor() {
		this(true);
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
	@Override
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

		if (fallback) {
			try {
				logger.debug("Site specific consumers failed, trying the raw link");

				final StatusConsumerRedirectStrategy redirector = new StatusConsumerRedirectStrategy();
				final IndependentPair<HttpEntity, ByteArrayInputStream> headersBais = HttpUtils
						.readURLAsByteArrayInputStream(url, 1000, 1000, redirector, HttpUtils.DEFAULT_USERAGENT);

				if (redirector.wasRedirected()) {
					logger.debug("Redirect intercepted, adding redirection to list");

					final URL redirect = redirector.redirection();
					if (!redirect.toString().equals(url.toString()))
						return processURLs(redirect);
				}

				// at this point any redirects have been resolved and the
				// content
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
						// It is a gif! just download it normally (i.e. null
						// image
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
			}
		}

		return null;
	}
}
