package org.openimaj.tools.imagecollection.collection.webpage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openimaj.image.MBFImage;
import org.openimaj.tools.imagecollection.collection.CountingImageCollectionIterator;
import org.openimaj.tools.imagecollection.collection.ImageCollection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionConfig;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntrySelection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionSetupException;

public abstract class AbstractWebpageImageCollection implements ImageCollection<MBFImage>{

	private Set<URL> imageList;
	private ImageCollectionEntrySelection<MBFImage> selection = null;

	@Override
	public CountingImageCollectionIterator<MBFImage> iterator() {
		if(selection == null)
			return new CountingImageCollectionIterator<MBFImage>(new URLImageIterator(imageList));
		else
			return new CountingImageCollectionIterator<MBFImage>(selection, new URLImageIterator(imageList));
	}
	
	@Override
	public void setup(ImageCollectionConfig config) throws ImageCollectionSetupException {
		
		String url = null;
		
		try {
			url = config.read("webpage.url");
		} catch (ParseException e) {
			throw new ImageCollectionSetupException("Could not deal with image source url, configuration error");
		}
		try {
			this.imageList = prepareURLs(new URL(url));
		} catch (MalformedURLException e) {
			throw new ImageCollectionSetupException("Could not deal with image source url, invalid URL");
		}
	}

	public abstract Set<URL> prepareURLs(URL url) throws ImageCollectionSetupException;
	

	@Override
	public int useable(ImageCollectionConfig config) {
		String url;
		try {
			url = config.read("webpage.url");
		} catch (ParseException e) {
			return -1;
		}
		if(url!=null) return 0;
		return -1;
	}

	@Override
	public List<ImageCollectionEntry<MBFImage>> getAll() {
		List<ImageCollectionEntry<MBFImage>> entries = new ArrayList<ImageCollectionEntry<MBFImage>>();
		for (ImageCollectionEntry<MBFImage> imageCollectionEntry : this) {
			entries.add(imageCollectionEntry);
		}
		return entries;
	}

	@Override
	public int countImages() {
		return this.imageList.size();
	}

	@Override
	public void setEntrySelection(ImageCollectionEntrySelection<MBFImage> selection) {
		this.selection  = selection;
		
	}
	
	public static class Generic extends AbstractWebpageImageCollection{
		@Override
		public Set<URL> prepareURLs(URL url) throws ImageCollectionSetupException {
			Document doc = null;
			try {
				doc = Jsoup.parse(url, 1000);
			} catch (IOException e) {
				throw new ImageCollectionSetupException("Could not deal with image source url, problem parsing HTML");
			}
			Set<URL> imageList = new HashSet<URL>();
			imageList.addAll(WebpageUtils.allURLs(doc,"img","src"));
			imageList.addAll(WebpageUtils.allURLs(doc,"a[href$=.png]","href"));
			return imageList;
		}
	}
}
