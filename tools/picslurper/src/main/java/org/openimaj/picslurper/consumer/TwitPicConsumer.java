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

public class TwitPicConsumer implements SiteSpecificConsumer {
	@Override
	public boolean canConsume(URL url) {
		// http://twitpic.com/a67733
		return url.getHost().contains("twitpic.com"); 
	}

	@Override
	public List<MBFImage> consume(URL url) {
		String largeURLStr = url.toString();
		if(!largeURLStr.endsWith("full")){
			largeURLStr += "/full";
		}
		try {
			Document doc = Jsoup.connect(largeURLStr).get();
			Elements largeimage = doc.select("img");
			String imgSrc = "";
			for (Element  e: largeimage) {
				imgSrc = e.attr("src");
				if(imgSrc.contains("photos") || imgSrc.contains("cloudfront")){
					break;
				}
			}
			return Arrays.asList(ImageUtilities.readMBF(new URL(imgSrc)));
		} catch (Exception e) {
			return null;
		}
		
	}
}
