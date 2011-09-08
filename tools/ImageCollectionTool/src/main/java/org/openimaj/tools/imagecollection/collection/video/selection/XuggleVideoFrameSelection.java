package org.openimaj.tools.imagecollection.collection.video.selection;

import java.text.ParseException;

import org.openimaj.image.MBFImage;
import org.openimaj.tools.imagecollection.collection.ImageCollectionConfig;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntrySelection;
import org.openimaj.video.xuggle.XuggleVideo;

public abstract class XuggleVideoFrameSelection implements ImageCollectionEntrySelection<MBFImage> {
	public static class Proxy extends XuggleVideoFrameSelection {
		private ImageCollectionEntrySelection<MBFImage> proxy;

		public Proxy(ImageCollectionEntrySelection<MBFImage> proxy) {
			this.proxy = proxy;
		}
		
		@Override
		public boolean acceptEntry(MBFImage image){
			return proxy.acceptEntry(image);
		}
	}
	public XuggleVideoFrameSelection(ImageCollectionConfig config){
		
	}
	
	public XuggleVideoFrameSelection() {
	}

	@Override
	public boolean acceptEntry(MBFImage image){
		return true;
	}

	public void init(XuggleVideo video){
		
	}
	public static class All extends XuggleVideoFrameSelection{

		public All(ImageCollectionConfig config) {
			super(config);
		}
	}
	
	public static class FramesPerSecond extends XuggleVideoFrameSelection{

		private Integer framesPerSecond;
		private int framesToSkip;
		private int framesCount;

		public FramesPerSecond(ImageCollectionConfig config) throws ParseException {
			super(config);
			this.framesPerSecond = config.read("video.framespersecond");
			if(this.framesPerSecond  == null){
				this.framesPerSecond = 2;
			}
		}
		
		@Override
		public void init(XuggleVideo video){
			this.framesToSkip = (int) (video.getFPS() / this.framesPerSecond);
			if(this.framesToSkip < 1) this.framesToSkip = 1;
			this.framesCount = 0;
		}
		
		@Override
		public boolean acceptEntry(MBFImage image){
			boolean good = (this.framesCount % this.framesToSkip) == 0;
			this.framesCount++;
			return good;
		}
	}
	
	private enum Styles{
		ALL {
			@Override
			public XuggleVideoFrameSelection style(ImageCollectionConfig config) {
				return new XuggleVideoFrameSelection.All(config);
			}
		},FRAMESPERSECOND {
			@Override
			public XuggleVideoFrameSelection style(ImageCollectionConfig config) throws ParseException {
				return new XuggleVideoFrameSelection.FramesPerSecond(config);
			}
		}
//		,KEYFRAMES {
//			@Override
//			public FrameStyle style(ImageCollectionConfig config) {
//				return new FrameStyle.KeyFrames(config);
//			}
//		}
		;
		public abstract XuggleVideoFrameSelection style(ImageCollectionConfig config) throws ParseException;
	}
	public static XuggleVideoFrameSelection byName(String read,ImageCollectionConfig config) throws ParseException {
		return Styles.valueOf(read).style(config);
	}

}
