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
package org.openimaj.tools.imagecollection.collection.config;

import org.openimaj.image.MBFImage;
import org.openimaj.tools.imagecollection.collection.ImageCollection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionSetupException;
import org.openimaj.tools.imagecollection.collection.video.XuggleVideoImageCollection;
import org.openimaj.tools.imagecollection.collection.video.YouTubeVideoImageCollection;
import org.openimaj.tools.imagecollection.collection.webpage.AbstractWebpageImageCollection;
import org.openimaj.tools.imagecollection.collection.webpage.FlickrWebpageImageCollection;

public enum ImageCollectionMode {
	XUGGLE_VIDEO_URL{
		@Override
		public ImageCollection<MBFImage> newCollection() {
			return new XuggleVideoImageCollection.FromURL();
		}
	},
	XUGGLE_VIDEO_FILE{
		@Override
		public ImageCollection<MBFImage> newCollection() {
			return new XuggleVideoImageCollection.FromFile();
		}
	},
	YOUTUBE_VIDEO{
		@Override
		public ImageCollection<MBFImage> newCollection() {
			return new YouTubeVideoImageCollection();
		}
	}
	,GENERIC_WEBPAGE{
		@Override
		public ImageCollection<MBFImage> newCollection() {
			return new AbstractWebpageImageCollection.Generic();
		}
	}
	,FLICKR_WEBPAGE_GALLERY{
		@Override
		public ImageCollection<MBFImage> newCollection() {
			return new FlickrWebpageImageCollection.Gallery();
		}
	}
	,FLICKR_WEBPAGE_SET{
		@Override
		public ImageCollection<MBFImage> newCollection() {
			return new FlickrWebpageImageCollection.FlickrPhotoSet();
		}
	}
	,FLICKR_WEBPAGE_COLLECTION{
		@Override
		public ImageCollection<MBFImage> newCollection() {
			return new FlickrWebpageImageCollection.FlickrPhotoCollection();
		}
	};
	public ImageCollection<MBFImage> initCollection(ImageCollectionConfig config) throws ImageCollectionSetupException{
		ImageCollection<MBFImage> col = newCollection();
		col.setup(config);
		return col;
	}
	public int usability(ImageCollectionConfig config){
		ImageCollection<MBFImage> col = newCollection();
		return col.useable(config);
	}
	
	public abstract ImageCollection<MBFImage> newCollection();
	
	public static ImageCollection<MBFImage> guessType(ImageCollectionConfig config) throws ImageCollectionSetupException{
		ImageCollectionMode found = null;
		int best = -Integer.MAX_VALUE;
		for(ImageCollectionMode s : ImageCollectionMode.values()){
			int use = s.usability(config);
			if(use > best && use >= 0){
				best = use;
				found = s;
			}
		}
		return found.initCollection(config);
	}
}