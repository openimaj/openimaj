package org.openimaj.tools.imagecollection.metamapper;

import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;

public interface MetaMapper {

	public void map(String name, ImageCollectionEntry<?> entry);

}
