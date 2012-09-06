package org.openimaj.picslurper.consumer;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.io.HttpUtils;
import org.openimaj.picslurper.SiteSpecificConsumer;

/**
 * A yfrog screen scraper
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class YfrogConsumer implements SiteSpecificConsumer{

	@Override
	public boolean canConsume(URL url) {
		return url.getHost().contains("yfrog");
	}

	@Override
	public List<MBFImage> consume(URL url) {
		try {
			byte[] retPage = HttpUtils.readURLAsBytes(url, false);
			Document soup = Jsoup.parse(new String(retPage,"UTF-8"));
			Elements imageElement = soup.select(".the-image img");
			List<MBFImage> ret = new ArrayList<MBFImage>();
			for (Element element : imageElement) {
				String imageSource = element.attr("src");
				if(imageSource!=null){
					ret.add(ImageUtilities.readMBF(new URL(imageSource)));
				}
			}
			return ret;
		} catch (Throwable e) {
			return null;
		}
	}

}
