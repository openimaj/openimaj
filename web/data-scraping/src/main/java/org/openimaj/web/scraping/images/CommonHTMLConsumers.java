package org.openimaj.web.scraping.images;

import org.openimaj.web.scraping.SimpleHTMLScrapingConsumer;

/**
 * HTML based consumers for common sites
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class CommonHTMLConsumers {
	/**
	 * Consumer for fotolog.com
	 */
	public static SimpleHTMLScrapingConsumer FOTOLOG = new SimpleHTMLScrapingConsumer("fotolog", "#flog_img_holder img");

	/**
	 * Consumer for photonui.com
	 */
	public static SimpleHTMLScrapingConsumer PHOTONUI = new SimpleHTMLScrapingConsumer("photonui", "#image-box img");

	/**
	 * Consumer for pics.lockerz.com
	 */
	public static SimpleHTMLScrapingConsumer PICS_LOCKERZ = new SimpleHTMLScrapingConsumer("pics.lockerz", "#photo");

	private CommonHTMLConsumers() {
	}
}
