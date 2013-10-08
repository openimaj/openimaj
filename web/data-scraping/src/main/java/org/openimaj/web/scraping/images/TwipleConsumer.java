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
