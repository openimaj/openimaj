/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.tools.imagecollection.collection.webpage;


import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openimaj.tools.imagecollection.collection.ImageCollectionSetupException;
import org.openimaj.tools.imagecollection.collection.config.ImageCollectionConfig;
import org.openimaj.util.pair.IndependentPair;

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
	public Set<IndependentPair<URL, Map<String, String>>> prepareURLs(URL url) throws ImageCollectionSetupException {
		System.out.println("Flickr query was: " + url.getFile());
		PhotoList results = null;
		try {
			results = flickrProcess(url.getPath()); 
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Failed performing flickr query");
			return null;
		}
		Set<IndependentPair<URL, Map<String, String>>> urls = new HashSet<IndependentPair<URL, Map<String, String>>>();
		for (int i = 0; i < results.size(); i++) {
			Map<String,String> meta = new HashMap<String,String>();
			Photo photo = (Photo) results.get(i) ;
			meta.put("flickr_photo_id", photo.getId());
			try {
				urls.add(IndependentPair.pair(new URL(photo.getMediumUrl()),meta));
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
	
	@Override
	public int useable(String rawInput) {
		return flickrUseable(rawInput);
	}
	
	@Override
	public ImageCollectionConfig defaultConfig(String rawInput) {
		return new ImageCollectionConfig(String.format("{webpage{url:%s,flickr:{apikey:%s,secret:%s}}}",rawInput,"a","b"));
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
		
		@Override
		public ImageCollectionConfig defaultConfig(String rawInput) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
