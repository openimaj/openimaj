package org.openimaj.tools.imagecollection.collection.webpage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebpageUtils {

	public static List<URL> allURLs(Document doc,String entity, String attribute) {
		List<URL> imageList = new ArrayList<URL>();
		Elements elements = doc.select("img");
		for (Element element : elements) {
			String imgURL = element.absUrl("src");
			try {
				imageList.add(new URL(imgURL));
			} catch (MalformedURLException e) {
				
			}
		}
		return imageList;
	}
	

}
