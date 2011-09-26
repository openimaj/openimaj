/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.tools.imagecollection.collection.video.selection;

import java.text.ParseException;

import org.openimaj.image.MBFImage;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntrySelection;
import org.openimaj.tools.imagecollection.collection.config.ImageCollectionConfig;
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
