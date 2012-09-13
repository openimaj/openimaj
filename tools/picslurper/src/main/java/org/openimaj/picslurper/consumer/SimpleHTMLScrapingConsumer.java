package org.openimaj.picslurper.consumer;

import java.net.URL;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SimpleHTMLScrapingConsumer extends HTMLScrapingSiteSpecificConsumer{

	private String linkContains;
	private String select;

	/**
	 * @param linkContains the link should contain this
	 * @param select the css selector for the img
	 */
	public SimpleHTMLScrapingConsumer(String linkContains, String select) {
		this.linkContains = linkContains;
		this.select = select;
	}
	@Override
	public boolean canConsume(URL url) {
		return url.getHost().contains(linkContains );
	}

	@Override
	public String cssSelect() {
		return select;
	}

}
