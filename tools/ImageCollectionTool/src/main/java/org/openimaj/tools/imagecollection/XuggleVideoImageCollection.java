package org.openimaj.tools.imagecollection;

import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.video.xuggle.XuggleVideo;

public abstract class XuggleVideoImageCollection implements ImageCollection<MBFImage>{
	public XuggleVideo video;

	@Override
	public Iterator<MBFImage> iterator() {
		return new VideoIterator<MBFImage>(video);
	}

	@Override
	public void setup(ImageCollectionConfig config) throws ImageCollectionSetupException {
		if(!useable(config)) throw new ImageCollectionSetupException("Can't use config as collection");
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
	public boolean useable(ImageCollectionConfig config){
		return config.containsValid(videoTag());
	}


	@Override
	public List<MBFImage> getAll() {
		List<MBFImage> allFrames = new ArrayList<MBFImage>();
		for(MBFImage image : this){
			allFrames.add(image);
		}
		return allFrames;
	}
	
	static class FromFile extends XuggleVideoImageCollection{
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
	
	static class FromURL extends XuggleVideoImageCollection{
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