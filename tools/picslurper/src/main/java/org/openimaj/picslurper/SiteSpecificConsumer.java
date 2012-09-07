package org.openimaj.picslurper;

import java.net.URL;
import java.util.List;

/**
 * Site specific consumers answer whether they can handle a URL
 * and when asked handle the URL, returning another URL from which the image
 * can be downloaded.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface SiteSpecificConsumer {
	/**
	 * @param url
	 * @return whether this URL can be handled by this consumer
	 */
	public boolean canConsume(URL url);
	/**
	 * @param url
	 * @return A list of images at URLs
	 */
	public List<URL> consume(URL url);
}
