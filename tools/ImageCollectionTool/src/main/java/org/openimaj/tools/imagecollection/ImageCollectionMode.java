package org.openimaj.tools.imagecollection;

import org.openimaj.image.MBFImage;
import org.openimaj.tools.imagecollection.collection.ImageCollection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionConfig;
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