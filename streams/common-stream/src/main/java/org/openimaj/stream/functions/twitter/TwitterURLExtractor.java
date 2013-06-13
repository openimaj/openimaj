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
