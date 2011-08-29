package org.openimaj.tools.imagecollection;

import org.openimaj.image.Image;
import org.openimaj.image.MBFImage;
import org.openimaj.tools.imagecollection.collection.ImageCollection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionConfig;
import org.openimaj.tools.imagecollection.collection.ImageCollectionSetupException;
import org.openimaj.tools.imagecollection.collection.XuggleVideoImageCollection;
import org.openimaj.tools.imagecollection.collection.XuggleVideoImageCollection.FromFile;
import org.openimaj.tools.imagecollection.collection.XuggleVideoImageCollection.FromURL;
import org.openimaj.tools.imagecollection.collection.YouTubeVideoImageCollection;

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
	};
	public <T extends Image<?,T>> ImageCollection<T> initCollection(ImageCollectionConfig config) throws ImageCollectionSetupException{
		ImageCollection<T> col = newCollection();
		col.setup(config);
		return col;
	}
	public <T extends Image<?,T>> int usability(ImageCollectionConfig config){
		ImageCollection<T> col = newCollection();
		return col.useable(config);
	}
	
	public abstract <T extends Image<?,T>> ImageCollection<T> newCollection();
	
	public static <T extends Image<?,T>> ImageCollection<T> guessType(ImageCollectionConfig config) throws ImageCollectionSetupException{
		ImageCollectionMode found = null;
		int best = -Integer.MAX_VALUE;
		for(ImageCollectionMode s : ImageCollectionMode.values()){
			int use = s.<T>usability(config);
			if(use > best && use >= 0){
				best = use;
				found = s;
			}
		}
		return found.initCollection(config);
	}
}