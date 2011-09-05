package org.openimaj.tools.imagecollection.collection;

import org.openimaj.image.Image;

public class ImageCollectionEntry<T extends Image<?,T>> {
	public String name;
	public Image<?,T> image;
	public boolean accepted;
}
