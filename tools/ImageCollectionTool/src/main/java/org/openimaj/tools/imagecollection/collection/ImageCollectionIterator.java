package org.openimaj.tools.imagecollection.collection;

import java.util.Iterator;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoIterator;

public class ImageCollectionIterator<T> implements Iterator<ImageCollectionEntry<MBFImage>> {

	private VideoIterator<MBFImage> videoIterator;

	public ImageCollectionIterator(VideoIterator<MBFImage> videoIterator) {
		this.videoIterator = videoIterator;
	}

	@Override
	public boolean hasNext() {
		return this.videoIterator.hasNext();
	}

	@Override
	public ImageCollectionEntry<MBFImage> next() {
		ImageCollectionEntry<MBFImage> entry = new ImageCollectionEntry<MBFImage>();
		entry.image = this.videoIterator.next();
		entry.name = this.videoIterator.getVideo().getCurrentFrameIndex() + "";
		return entry;
	}

	@Override
	public void remove() {
		this.videoIterator.remove();
	}

}
