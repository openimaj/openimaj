package org.openimaj.picslurper.consumer;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.picslurper.SiteSpecificConsumer;

public class TwitterPhotoConsumer implements SiteSpecificConsumer {
	@Override
	public boolean canConsume(URL url) {
		// http://twitter.com/HutchSelenator/status/222772697531301890/photo/1
		return url.getHost().equals("twitter.com") && url.getPath().contains("photo"); 
	}

	@Override
	public List<MBFImage> consume(URL url) {
		String largeURLStr = url.toString();
		if(!largeURLStr.endsWith("large")){
			largeURLStr += "/large";
		}
		try {
			Document doc = Jsoup.connect(largeURLStr).get();
			Elements largeimage = doc.select(".media-slideshow-image");
			return Arrays.asList(ImageUtilities.readMBF(new URL(largeimage.get(0).attr("src"))));
		} catch (Exception e) {
			return null;
		}
		
	}

}
