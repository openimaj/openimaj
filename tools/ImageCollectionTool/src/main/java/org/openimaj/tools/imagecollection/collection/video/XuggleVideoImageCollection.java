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
package org.openimaj.tools.imagecollection.collection.video;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openimaj.image.MBFImage;
import org.openimaj.tools.imagecollection.collection.ImageCollection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntry;
import org.openimaj.tools.imagecollection.collection.ImageCollectionEntrySelection;
import org.openimaj.tools.imagecollection.collection.ImageCollectionSetupException;
import org.openimaj.tools.imagecollection.collection.config.ImageCollectionConfig;
import org.openimaj.tools.imagecollection.collection.video.selection.XuggleVideoFrameSelection;
import org.openimaj.video.xuggle.XuggleVideo;

public abstract class XuggleVideoImageCollection implements ImageCollection<MBFImage>{
	public XuggleVideo video;
	private XuggleVideoFrameSelection frameStyle;

	@Override
	public Iterator<ImageCollectionEntry<MBFImage>> iterator() {
		frameStyle.init(video);
		return new MetadataVideoIterator<MBFImage>(frameStyle,video);
	}
	
	@Override
	public void setEntrySelection(ImageCollectionEntrySelection<MBFImage> selection){
		if(selection instanceof XuggleVideoFrameSelection)
			frameStyle = (XuggleVideoFrameSelection) selection;
		else
			frameStyle = new XuggleVideoFrameSelection.Proxy(selection);
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
		
		if(!config.containsValid("video.framestyle")){
			frameStyle = new XuggleVideoFrameSelection.All(config);
		}
		else{
			String name;
			try {
				name = config.read("video.framestyle");
				frameStyle = XuggleVideoFrameSelection.byName(name,config);
			} catch (ParseException e) {
				throw new ImageCollectionSetupException("Failed to set framestyle");
			}
		}
	}
	
	@Override
	public int countImages(){
		return (int) this.video.countFrames();
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

		@Override
		public int useable(String rawInput) {
			File f = new File(rawInput);
			if(f.exists()) return 0;
			else return -1;
		}

		@Override
		public ImageCollectionConfig defaultConfig(String rawInput) {
			return new ImageCollectionConfig(
					String.format("{video: {file: %s}}",rawInput)
			);
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
		
		@Override
		public int useable(String rawInput) {
			try {
				new URL(rawInput);
				return 0;
			} catch (MalformedURLException e) {
				return -1;
			}
		}

		@Override
		public ImageCollectionConfig defaultConfig(String rawInput) {
			return new ImageCollectionConfig(
				String.format("{video: {url: %s}}",rawInput)
			);
		}
	}
}