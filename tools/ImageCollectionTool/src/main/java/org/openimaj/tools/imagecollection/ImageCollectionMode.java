package org.openimaj.tools.imagecollection;

import org.openimaj.image.MBFImage;
import org.openimaj.tools.imagecollection.collection.ImageCollection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionConfig;
import org.openimaj.tools.imagecollection.collection.ImageCollectionSetupException;
import org.openimaj.tools.imagecollection.collection.XuggleVideoImageCollection;
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