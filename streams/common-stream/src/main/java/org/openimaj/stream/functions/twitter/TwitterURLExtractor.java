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
package org.openimaj.stream.functions.twitter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openimaj.text.nlp.patterns.URLPatternProvider;
import org.openimaj.util.function.MultiFunction;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;

/**
 * This class implements a function that processes Twitter {@link Status}
 * objects to extract all the mentioned URLs. URLs are extracted from both the
 * entities field and the Tweet message body (by applying a regular expression).
 *
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class TwitterURLExtractor implements MultiFunction<Status, URL> {
	private final static Logger logger = Logger.getLogger(TwitterURLExtractor.class);
	private final static Pattern urlPattern = new URLPatternProvider().pattern();

	@Override
	public List<URL> apply(Status status) {
		final Set<String> urls = new HashSet<String>();

		// Add all the entries from entities.urls
		if (status.getURLEntities() != null) {
			for (final URLEntity map : status.getURLEntities()) {
				String u = map.getExpandedURL();

				if (u == null) {
					u = map.getURL();

					if (u == null)
						continue;
				}

				urls.add(u);
			}
		}

		// Add all the entries from media.urls
		for (final MediaEntity map : status.getMediaEntities()) {
			String u = map.getMediaURL();

			if (u == null) {
				u = map.getMediaURLHttps();

				if (u == null)
					u = map.getDisplayURL();

				if (u == null)
					continue;
			}
			urls.add(u);
		}

		// Find the URLs in the raw text
		final String text = status.getText();
		if (text != null) { // why was text null?
			final Matcher matcher = urlPattern.matcher(text);

			while (matcher.find()) {
				String urlString = text.substring(matcher.start(), matcher.end());

				if (!urlString.contains("://"))
					urlString = "http://" + urlString;

				urls.add(urlString);
			}
		}

		// get the final URLs
		final ArrayList<URL> finalUrls = new ArrayList<URL>();
		for (final String u : urls) {
			try {
				finalUrls.add(new URL(u));
			} catch (final MalformedURLException e) {
				logger.warn("ignoring URL ", e);
			}
		}
		return finalUrls;
	}
}
