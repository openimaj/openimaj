package org.openimaj.tools.imagecollection.metamapper;

import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;
import org.openimaj.tools.imagecollection.utils.MetaMapUtils;

public class ConsoleMetaMapper extends MetaMapper{
	@Override
	public void mapItem(String name, ImageCollectionEntry<?> entry) {
		String jsonString = MetaMapUtils.metaAsJson(entry.meta);
		System.out.println(String.format("%s: %s",name,jsonString));
	}
}
