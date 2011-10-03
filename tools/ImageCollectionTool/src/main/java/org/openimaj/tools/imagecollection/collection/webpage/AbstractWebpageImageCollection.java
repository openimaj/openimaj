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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openimaj.image.MBFImage;
import org.openimaj.tools.imagecollection.collection.ImageCollection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntrySelection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionSetupException;
import org.openimaj.tools.imagecollection.collection.config.ImageCollectionConfig;
import org.openimaj.util.pair.IndependentPair;

public abstract class AbstractWebpageImageCollection implements ImageCollection<MBFImage>{

	private ImageCollectionEntrySelection<MBFImage> selection = null;
	private Set<IndependentPair<URL, Map<String, String>>> imageList;

	@Override
	public Iterator<ImageCollectionEntry<MBFImage>> iterator() {
		return new URLImageIterator(imageList,selection);
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

	public abstract Set<IndependentPair<URL, Map<String, String>>> prepareURLs(URL url) throws ImageCollectionSetupException;
	

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
		public Set<IndependentPair<URL, Map<String, String>>> prepareURLs(URL url) throws ImageCollectionSetupException {
			Document doc = null;
			try {
				doc = Jsoup.parse(url, 1000);
			} catch (IOException e) {
				throw new ImageCollectionSetupException("Could not deal with image source url, problem parsing HTML");
			}
			Set<IndependentPair<URL, Map<String, String>>> imageList = 
				new HashSet<IndependentPair<URL, Map<String, String>>>();
			imageList.addAll(WebpageUtils.allURLs(doc,"img","src"));
			imageList.addAll(WebpageUtils.allURLs(doc,"a[href$=.png]","href"));
			return imageList;
		}

		@Override
		public int useable(String rawInput) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public ImageCollectionConfig defaultConfig(String rawInput) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
