package org.openimaj.picslurper.consumer;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.picslurper.SiteSpecificConsumer;

import com.google.gson.Gson;

public class InstagramConsumer implements SiteSpecificConsumer {
	String apiCallFormat = "http://api.instagram.com/oembed?url=http://instagr.am/p/%s";
	private transient Gson gson = new Gson();
	@Override
	public boolean canConsume(URL url) {
		// http://instagram.com/p/Mbr57UC7L6
		return url.getHost().equals("instagr.am") || url.getHost().equals("instagram.com"); 
	}

	@Override
	public List<MBFImage> consume(URL url) {
		String file = url.getFile();
		if(file.endsWith("/"))file = file.substring(0, file.length()-1);
		String[] splits = file.split("/");
		String shortID = splits[splits.length-1];
		String apiCall = String.format(apiCallFormat,shortID);
		try {
			@SuppressWarnings("unchecked")
			Map<String,Object> res = gson.fromJson(new InputStreamReader(new URL(apiCall).openConnection().getInputStream()), Map.class);
			String instagramURL = (String) res.get("url");
			return Arrays.asList(ImageUtilities.readMBF(new URL(instagramURL)));
		} catch (Exception e) {			
			return null;
		}
	}

}
