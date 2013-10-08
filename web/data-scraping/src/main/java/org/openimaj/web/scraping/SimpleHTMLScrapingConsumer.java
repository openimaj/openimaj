/**
 * Copyright 2011 The University of Southampton, Yahoo Inc., and the
 * individual contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openimaj.web.scraping;

import java.net.URL;

/**
 * Simple scraper that just uses the given css selector to find all
 * relevant data in the page
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class SimpleHTMLScrapingConsumer extends HTMLScrapingSiteSpecificConsumer {
	private String linkContains;
	private String select;

	/**
	 * @param linkContains
	 *            the link should contain this
	 * @param select
	 *            the css selector for the img
	 */
	public SimpleHTMLScrapingConsumer(String linkContains, String select) {
		this.linkContains = linkContains;
		this.select = select;
	}

	@Override
	public boolean canConsume(URL url) {
		return url.getHost().contains(linkContains);
	}

	@Override
	public String cssSelect() {
		return select;
	}

}
