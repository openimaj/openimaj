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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
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
