package org.openimaj.stream.functions;

import org.openimaj.web.scraping.SiteSpecificConsumer;
import org.openimaj.web.scraping.images.CommonHTMLConsumers;
import org.openimaj.web.scraping.images.FacebookConsumer;
import org.openimaj.web.scraping.images.ImgurConsumer;
import org.openimaj.web.scraping.images.InstagramConsumer;
import org.openimaj.web.scraping.images.OwlyImageConsumer;
import org.openimaj.web.scraping.images.TmblrPhotoConsumer;
import org.openimaj.web.scraping.images.TwipleConsumer;
import org.openimaj.web.scraping.images.TwitPicConsumer;
import org.openimaj.web.scraping.images.TwitterPhotoConsumer;
import org.openimaj.web.scraping.images.YfrogConsumer;

/**
 * This class implements a function that will given an input URL outputs a list
 * of URLs to the possible images related to the input URL. This works by using
 * a set of {@link SiteSpecificConsumer}s for common image hosting sites to
 * determine if the input URL is likely to lead to an image of images.
 * <p>
 * Currently, the following consumers are included:
 * <ul>
 * <li> {@link InstagramConsumer}
 * <li> {@link TwitterPhotoConsumer}
 * <li> {@link TmblrPhotoConsumer}
 * <li> {@link TwitPicConsumer}
 * <li> {@link ImgurConsumer}
 * <li> {@link FacebookConsumer}
 * <li> {@link YfrogConsumer}
 * <li> {@link OwlyImageConsumer}
 * <li> {@link TwipleConsumer}
 * <li> {@link CommonHTMLConsumers#FOTOLOG}
 * <li> {@link CommonHTMLConsumers#PHOTONUI}
 * <li> {@link CommonHTMLConsumers#PICS_LOCKERZ}
 * </ul>
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class ImageSiteURLExtractor extends SiteSpecificURLExtractor {
	/**
	 * Default constructor.
	 */
	public ImageSiteURLExtractor() {
		super(new InstagramConsumer(),
				new TwitterPhotoConsumer(),
				new TmblrPhotoConsumer(),
				new TwitPicConsumer(),
				new ImgurConsumer(),
				new FacebookConsumer(),
				new YfrogConsumer(),
				new OwlyImageConsumer(),
				new TwipleConsumer(),
				CommonHTMLConsumers.FOTOLOG,
				CommonHTMLConsumers.PHOTONUI,
				CommonHTMLConsumers.PICS_LOCKERZ);
	}
}
