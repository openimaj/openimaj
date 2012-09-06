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
 * Consume facebook posts/pictures using the {@link com.restfb.FacebookClient} client
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class FacebookConsumer implements SiteSpecificConsumer{

	@Override
	public boolean canConsume(URL url) {
		return url.getHost().contains("facebook");
	}

	@Override
	public List<MBFImage> consume(URL url) {
		// posts == http://www.facebook.com/jsproducoes/posts/426306737404997
		// photos == http://www.facebook.com/photo.php?pid=1307526&l=3d755a0895&id=353116314727854
		String urlFile = url.getFile();
		if(urlFile.startsWith("/photo.php")){
			return consumeFacebookPhoto(url);
		}
		else if(urlFile.contains("/posts/")){
			return consumeFacebookPost(url);
		}
		return null;
	}

	private List<MBFImage> consumeFacebookPost(URL url) {
		try {
			byte[] retPage = HttpUtils.readURLAsBytes(url, false);
			Document soup = Jsoup.parse(new String(retPage,"UTF-8"));
			Elements imageElement = soup.select(".storyInnerContent img");
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

	private List<MBFImage> consumeFacebookPhoto(URL url) {
		try {
			byte[] retPage = HttpUtils.readURLAsBytes(url, false);
			Document soup = Jsoup.parse(new String(retPage,"UTF-8"));
			Elements imageElement = soup.select("#fbPhotoImage");
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
