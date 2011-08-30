package org.openimaj.tools.imagecollection.collection;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.List;

import org.openimaj.video.xuggle.XuggleVideo;

import com.xuggle.utils.collections.KeyValuePair;
import com.xuggle.utils.net.URLParams;
import com.xuggle.utils.net.YouTube;

public class YouTubeVideoImageCollection extends XuggleVideoImageCollection.FromURL{
	
	protected XuggleVideo loadXuggleVideo(String videoEntry) {
		String youtubeId = parseYoutubeID(videoEntry);
		
		if(youtubeId == null) return null;
		String youtubeFLV = YouTube.getLocation(youtubeId);
		
		return new XuggleVideo(youtubeFLV);
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
