package org.openimaj.tools.imagecollection.collection.webpage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;

public class URLImageIterator implements Iterator<MBFImage> {

	private Iterator<URL> imageList;

	public URLImageIterator(Collection<URL> imageList) {
		this.imageList = imageList.iterator();
	}

	@Override
	public boolean hasNext() {
		return imageList.hasNext();
	}

	@Override
	public MBFImage next() {
		URL u = imageList.next();
		try {
			MBFImage image = ImageUtilities.readMBF(u.openConnection().getInputStream());
			return image;
		} catch (IOException e) {
		}
		return null;
	}

	@Override
	public void remove() {
		this.imageList.remove();
	}

}
