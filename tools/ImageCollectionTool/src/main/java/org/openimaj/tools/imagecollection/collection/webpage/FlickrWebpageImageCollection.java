package org.openimaj.tools.imagecollection.collection.webpage;


import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openimaj.tools.imagecollection.collection.ImageCollectionConfig;
import org.openimaj.tools.imagecollection.collection.ImageCollectionSetupException;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.collections.CollectionsInterface;
import com.aetrion.flickr.collections.CollectionsSearchParameters;
import com.aetrion.flickr.collections.PhotoCollection;
import com.aetrion.flickr.galleries.GalleriesInterface;
import com.aetrion.flickr.galleries.SearchParameters;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photosets.Photoset;
import com.aetrion.flickr.photosets.PhotosetsInterface;


public abstract class FlickrWebpageImageCollection extends AbstractWebpageImageCollection {
	
	
	protected Flickr flickr;

	@Override
	public void setup(ImageCollectionConfig config) throws ImageCollectionSetupException {
		
		try {
			String apikey = config.read("webpage.flickr.apikey");
			String secret = config.read("webpage.flickr.secret");
			flickr = new Flickr(apikey, secret, new REST(Flickr.DEFAULT_HOST));
			
		} catch (Exception e) {
			throw new ImageCollectionSetupException("Faile to setup, error creating the flickr client");
		}
		
		super.setup(config);
	}
	@Override
	public Set<URL> prepareURLs(URL url) throws ImageCollectionSetupException {
		System.out.println("Flickr query was: " + url.getFile());
		PhotoList results = null;
		try {
			results = flickrProcess(url.getPath()); 
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Failed performing flickr query");
			return null;
		}
		Set<URL> urls = new HashSet<URL>();
		for (int i = 0; i < results.size(); i++) {
			Photo photo = (Photo) results.get(i) ;
			try {
				urls.add(new URL(photo.getMediumUrl()));
			} catch (MalformedURLException e) {
				
			}
		}
		return urls;
	}
	
	protected abstract PhotoList flickrProcess(String string);

	@Override
	public int useable(ImageCollectionConfig config) {
		String urlStr;
		String apiKey = null;
		String secret = null;
		try {
			urlStr = config.read("webpage.url");
			apiKey = config.read("webpage.flickr.apikey");
			secret = config.read("webpage.flickr.secret");
		} catch (ParseException e) {
			return -1;
		}
		if(urlStr==null || apiKey == null || secret == null) return -1;
		
		URL url;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			return -1;
		}
		if(url.getHost().endsWith("flickr.com")) {
			return flickrUseable(url.getPath());
		}
		return -1;	
	}
	
	public abstract int flickrUseable(String path);
	
	public static class Gallery extends FlickrWebpageImageCollection{
		Pattern r = Pattern.compile(".*/photos/.*/galleries/[0-9]*(/|$)");
		@Override
		public int flickrUseable(String path) {
			return r.matcher(path).matches() ? 1000 : -1;
		}
		@Override
		protected PhotoList flickrProcess(String path) {
			try{
				GalleriesInterface galleriesInterface = flickr.getGalleriesInterface();
				String galleryId = flickr.getUrlsInterface().lookupGallery(path);
				SearchParameters params = new SearchParameters();
				params.setGalleryId(galleryId);
				return galleriesInterface.getPhotos(params, 18, 0);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
	}
	public static class FlickrPhotoSet extends FlickrWebpageImageCollection{
		Pattern r = Pattern.compile(".*/photos/.*/sets/([0-9]*)(/|$)");
		@Override
		public int flickrUseable(String path) {
			return r.matcher(path).matches() ? 1000 : -1;
		}
		@Override
		protected PhotoList flickrProcess(String path) {
			try{
				PhotosetsInterface setsInterface = flickr.getPhotosetsInterface();
				Matcher matcher = r.matcher(path);
				matcher.find();
				String setId = matcher.group(1);
				Photoset set = setsInterface.getInfo(setId);
				return setsInterface.getPhotos(setId, set.getPhotoCount(), 0);
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public static class FlickrPhotoCollection extends FlickrWebpageImageCollection{
		Pattern r = Pattern.compile(".*/photos/(.*)/collections/([0-9]*)(/|$)");
		@Override
		public int flickrUseable(String path) {
			return r.matcher(path).matches() ? 1000 : -1;
		}
		@Override
		protected PhotoList flickrProcess(String path) {
			try{CollectionsInterface collectionsInterface = flickr.getCollectionsInterface();				
				Matcher matcher = r.matcher(path);
				matcher.find();
				String userName = matcher.group(1);
				String collectionsId = matcher.group(2);
				CollectionsSearchParameters params = new CollectionsSearchParameters();
				params.setCollectionId(collectionsId);
				params.setUserId(flickr.getPeopleInterface().findByUsername(userName).getId());
				PhotoCollection c = collectionsInterface.getTree(params);
				return c.getPhotoUrls(flickr.getPhotosetsInterface());
			}
			catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
	}
}
