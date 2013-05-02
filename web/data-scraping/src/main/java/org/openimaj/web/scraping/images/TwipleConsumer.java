package org.openimaj.web.scraping.images;

import java.net.URL;

import org.openimaj.web.scraping.HTMLScrapingSiteSpecificConsumer;

/**
 * A Twiple screen scraper
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class TwipleConsumer extends HTMLScrapingSiteSpecificConsumer {
	@Override
	public boolean canConsume(URL url) {
		return url.getHost().contains("twipple");
	}

	@Override
	public String cssSelect() {
		return "#img_box img";
	}
}
