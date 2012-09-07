package org.openimaj.picslurper.consumer;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.picslurper.SiteSpecificConsumer;
import org.openimaj.util.pair.IndependentPair;

/**
 * Use JSoup to load the twitpic page and find the img tag that has a source
 * which contains the string "photos" or "cloudfront"
 *
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TwitPicConsumer implements SiteSpecificConsumer {
	@Override
	public boolean canConsume(URL url) {
		// http://twitpic.com/a67733
		return url.getHost().contains("twitpic.com");
	}

	@Override
	public List<IndependentPair<URL, MBFImage>> consume(URL url) {
		String largeURLStr = url.toString();
		if (!largeURLStr.endsWith("full")) {
			largeURLStr += "/full";
		}
		try {
			Document doc = Jsoup.connect(largeURLStr).get();
			Elements largeimage = doc.select("img");
			String imgSrc = "";
			for (Element e : largeimage) {
				imgSrc = e.attr("src");
				if (imgSrc.contains("photos") || imgSrc.contains("cloudfront")) {
					break;
				}
			}
			URL link = new URL(imgSrc);
			MBFImage img = ImageUtilities.readMBF(link);
			@SuppressWarnings("unchecked")
			List<IndependentPair<URL, MBFImage>> a = Arrays.asList(IndependentPair.pair(link, img));
			return a;
		} catch (Exception e) {
			return null;
		}

	}
}
