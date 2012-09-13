package org.openimaj.picslurper.consumer;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openimaj.io.HttpUtils;
import org.openimaj.picslurper.SiteSpecificConsumer;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public abstract class HTMLScrapingSiteSpecificConsumer implements SiteSpecificConsumer{
	@Override
	public List<URL> consume(URL url) {
		try {
			ByteArrayInputStream stream = HttpUtils.readURLAsByteArrayInputStream(url, 1000, 1000, null, HttpUtils.DEFAULT_USERAGENT).getSecondObject();
			byte[] retPage = org.apache.commons.io.IOUtils.toByteArray(stream);
			Document soup = Jsoup.parse(new String(retPage,"UTF-8"));
			Elements imageElement = soup.select(cssSelect());
			List<URL> ret = new ArrayList<URL>();
			for (Element element : imageElement) {
				String imageSource = element.attr("src");
				if(imageSource!=null){
					try{
						URL link = new URL(imageSource);
						ret.add(link);
					}
					catch(Throwable e){
						// ?? maybe it didn't have the host in the src?
						URL link = new URL(url.getProtocol(),url.getHost(),imageSource);
						ret.add(link);
					}
				}
			}
			return ret;
		} catch (Throwable e) {
			return null;
		}
	}

	/**
	 * @return the css selection from which to find the img to scrape
	 */
	public abstract String cssSelect() ;
}
