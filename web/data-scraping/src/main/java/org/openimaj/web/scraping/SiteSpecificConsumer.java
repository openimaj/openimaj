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
import java.util.List;

/**
 * Site specific consumers answer whether they can handle a URL and when asked
 * handle the URL, returning another URL from which the data can be downloaded.
 * This interface doesn't specify exactly what the data is, but specific
 * implementations will usually only apply to a single data modality.
 * <p>
 * Typical uses are for the downloading of images on a URL to an image hosting
 * site.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public interface SiteSpecificConsumer {
	/**
	 * Determine whether the given URL can be handled by this consumer.
	 * 
	 * @param url
	 *            the url to test
	 * @return true if the URL can be handled by this consumer; false otherwise.
	 */
	public boolean canConsume(URL url);

	/**
	 * Get the data urls at the given URL.
	 * 
	 * @param url
	 *            the url to test
	 * @return A list of URLs to data items at the given URL
	 */
	public List<URL> consume(URL url);
}
