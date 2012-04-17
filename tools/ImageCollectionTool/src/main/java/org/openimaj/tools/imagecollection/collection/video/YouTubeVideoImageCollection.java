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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

import org.openimaj.tools.imagecollection.collection.ImageCollectionSetupException;
import org.openimaj.tools.imagecollection.collection.config.ImageCollectionConfig;
import org.openimaj.video.xuggle.XuggleVideo;

import com.google.gdata.client.youtube.YouTubeService;
import com.google.gdata.data.youtube.VideoEntry;
import com.google.gdata.util.ServiceException;
import com.xuggle.utils.collections.KeyValuePair;
import com.xuggle.utils.net.URLParams;
import com.xuggle.utils.net.YouTube;

public class YouTubeVideoImageCollection extends XuggleVideoImageCollection.FromURL{
	String developerKey = "AI39si4l2-2ZI94omuJk1U9mk5QvBFoPXbZ0Jsb5LnEtosQDSEOMR0DD5gBjlOG4kmUZ17r6cI-WBejYWvBk7oNm9U409KJjEA";
	String gDataURLTemplate = "http://gdata.youtube.com/feeds/api/videos/%s";
	private VideoEntry entry;
	public YouTubeVideoImageCollection() {
	}
	public YouTubeVideoImageCollection(String youtubeURLStr) throws ImageCollectionSetupException {
		String youtubeJSON = String.format("{video:{url:\"%s\"}}",youtubeURLStr);
		ImageCollectionConfig youtubeConfig = new ImageCollectionConfig(youtubeJSON);
		this.setup(youtubeConfig);
	}

	@Override
	protected XuggleVideo loadXuggleVideo(String videoEntry) {
		String youtubeId = parseYoutubeID(videoEntry);
		
		if(youtubeId == null) return null;
		String youtubeFLV = YouTube.getLocation(youtubeId);
		
		YouTubeService service = new YouTubeService("thisinthat",developerKey);
		URL gDataURL;
		try {
			gDataURL = new URL(String.format(gDataURLTemplate, youtubeId));
			this.entry = service.getEntry(gDataURL, VideoEntry.class);
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return new XuggleVideo(youtubeFLV);
	}
	
	@Override
	public int countImages(){
		return (int) (this.entry.getMediaGroup().getDuration() * this.video.getFPS());
	}
	
	@Override 
	public int useable(ImageCollectionConfig config){
		if(super.useable(config) < 0) return -1;
		String youtubeID = parseYoutubeID(config);
		if(youtubeID == null) return -1;
		return 1000;
	}

	private String parseYoutubeID(ImageCollectionConfig config) {
		String urlStr;
		try {
			urlStr = config.read(this.videoTag());
		} catch (ParseException e1) {
			return null;
		}
		
		return parseYoutubeID(urlStr);

	}

	private String parseYoutubeID(String urlStr) {
//		http://www.youtube.com/watch?v=X4fRYSeIpIQ
		URL u;
		try {
			u = new URL(urlStr);
		} catch (MalformedURLException e) {
			return null;
		}
		if(!u.getHost().matches(".*[.]youtube.com$")) return null;
		
		List<KeyValuePair> l = URLParams.parseQueryString(u.getQuery());
		String foundID = null;
		for (KeyValuePair pair : l) {
			if(pair.getKey().equals("v")){
				foundID = pair.getValue();
				break;
			}
		}
		return foundID;
	}
}
