package org.openimaj.video;


import java.util.Iterator;

import org.openimaj.image.Image;


public class VideoIterator<T extends Image<?,T>> implements Iterator<T> {

	private Video<T> video;

	public VideoIterator(Video<T> video) {
		this.video = video;
	}

	@Override
	public boolean hasNext() {
		return video.hasNextFrame();
	}

	@Override
	public T next() {
		// TODO Auto-generated method stub
		return video.getNextFrame();
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Can't remove frames from a video");
	}

	public Video<T> getVideo() {
		return video;
	}

}
