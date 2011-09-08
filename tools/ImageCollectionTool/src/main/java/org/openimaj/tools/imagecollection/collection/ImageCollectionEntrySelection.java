package org.openimaj.tools.imagecollection.collection;

import org.openimaj.image.Image;

public interface ImageCollectionEntrySelection<T extends Image<?,T>> {
	public boolean acceptEntry(T image);
	public static class All<T extends Image<?,T>> implements ImageCollectionEntrySelection<T>{
		@Override
		public boolean acceptEntry(T image) {
			return true;
		}
	}
}
