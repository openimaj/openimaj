package org.openimaj.tools.imagecollection.collection;

import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.video.VideoIterator;
import org.openimaj.video.xuggle.XuggleVideo;

public abstract class XuggleVideoImageCollection implements ImageCollection<MBFImage>{
	public XuggleVideo video;

	@Override
	public Iterator<ImageCollectionEntry<MBFImage>> iterator() {
		return new ImageCollectionIterator<MBFImage>(new VideoIterator<MBFImage>(video));
	}

	@Override
	public void setup(ImageCollectionConfig config) throws ImageCollectionSetupException {
		if(useable(config) < 0) throw new ImageCollectionSetupException("Can't use config as collection");
		String videoEntry;
		try {
			videoEntry = config.read(videoTag());
		} catch (ParseException e) {
			throw new ImageCollectionSetupException(e);
		}
		this.video = loadXuggleVideo(videoEntry);
		if(this.video == null){
			throw new ImageCollectionSetupException("Failed to load youtube video");
		}
	}

	protected abstract XuggleVideo loadXuggleVideo(String object);
	protected abstract String videoTag();

	@Override 
	public int useable(ImageCollectionConfig config){
		if(config.containsValid(videoTag()))
			return 0;
		else return -1;
	}


	@Override
	public List<ImageCollectionEntry<MBFImage>> getAll() {
		List<ImageCollectionEntry<MBFImage>> allFrames = new ArrayList<ImageCollectionEntry<MBFImage>>();
		for(ImageCollectionEntry<MBFImage> image : this){
			allFrames.add(image);
		}
		return allFrames;
	}
	
	public static class FromFile extends XuggleVideoImageCollection{
		@Override
		protected XuggleVideo loadXuggleVideo(String videoEntry) {
			File videoFile = new File(videoEntry);
			return new XuggleVideo(videoFile);
		}

		@Override
		protected String videoTag() {
			return "video.file";
		}
	}
	
	public static class FromURL extends XuggleVideoImageCollection{
		@Override
		protected XuggleVideo loadXuggleVideo(String videoEntry) {
			return new XuggleVideo(videoEntry);
		}

		@Override
		protected String videoTag() {
			return "video.url";
		}
	}
}