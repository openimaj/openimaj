package org.openimaj.tools.imagecollection.collection.video;

import java.util.HashMap;
import java.util.Iterator;

import org.openimaj.image.Image;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntrySelection;
import org.openimaj.video.Video;

public class MetadataVideoIterator<T extends Image<?,T>> implements Iterator<ImageCollectionEntry<T>> {

	private Video<T> video;
	private ImageCollectionEntrySelection<T> selection;
	private int frameCount = 0;

	public MetadataVideoIterator(ImageCollectionEntrySelection<T> selection,Video<T> video) {
		this.video = video;
		this.selection = selection;
	}

	@Override
	public boolean hasNext() {
		return video.hasNextFrame();
	}

	@Override
	public ImageCollectionEntry<T> next() {
		T image = video.getNextFrame();
		ImageCollectionEntry<T> entry = new ImageCollectionEntry<T>();
		entry.meta = new HashMap<String,String>();
		entry.meta.put("timestamp", "" + this.frameCount / this.video.getFPS());
		entry.accepted = selection.acceptEntry(image);
		entry.image = image;
		this.frameCount ++;
		
		return entry;
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

}
