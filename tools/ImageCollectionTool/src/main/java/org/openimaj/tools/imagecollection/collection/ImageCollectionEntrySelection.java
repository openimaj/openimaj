package org.openimaj.tools.imagecollection.collection;

import org.openimaj.image.Image;


public interface ImageCollectionEntrySelection<T extends Image<?,T>> {
	public boolean acceptEntry(T image);
}
