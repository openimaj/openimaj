package org.openimaj.picslurper.consumer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.io.FileUtils;
import org.openimaj.io.HttpUtils;
import org.openimaj.picslurper.SiteSpecificConsumer;

import sun.net.www.protocol.http.HttpURLConnection;

import com.google.gson.Gson;

public class TmblrPhotoConsumer implements SiteSpecificConsumer {
	private transient Gson gson = new Gson();
	@Override
	public boolean canConsume(URL url) {
		//  http://tmblr.co/ZoH2IyP4lDVD
		return url.getHost().equals("tmblr.co") || url.getHost().endsWith("tumblr.com"); 
	}
	String tumblrAPICall = "http://api.tumblr.com/v2/blog/derekg.org/posts?id=%s&api_key=%s";

	@SuppressWarnings("unchecked")
	@Override
	public List<MBFImage> consume(URL url) {
		// construct the actual tumblr address
		try {
			String postID = getPostID(url);			
			// NOW call the tumblrAPI
			String tmblrRequest = String.format(tumblrAPICall, postID,System.getProperty("tmblrapi"));
			Map<String,Object> res = gson.fromJson(new InputStreamReader(new URL(tmblrRequest).openConnection().getInputStream()), Map.class);
			
			Map<?,?> response = (Map<?, ?>) res.get("response");
			Map<?,?> posts = (Map<?, ?>) ((List<?>) response.get("posts")).get(0);
			List<Map<?, ?>> photos =  ((List<Map<?, ?>>)posts.get("photos"));
			if(photos == null) return null; 
			List<MBFImage> images = new ArrayList<MBFImage>();
			for (Map<?, ?> photo : photos) {				
				String photoURLStr = (String) ((Map<String, Object>) photo.get("original_size")).get("url");
				URL photoURL = new URL(photoURLStr);
				InputStream strm = HttpUtils.readURLAsStream(photoURL);
				MBFImage read = ImageUtilities.readMBF(strm);
				images.add(read);
			}
			return images;
			
		} catch (Throwable e) {
			return null;
		}
	}

	/**
	 * handles the variety of ways a tumblr addresses can be forwarded to
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private String getPostID(URL url) throws IOException {
		String host = url.getHost();
		URL loc = url;
		if(host.equals("tmblr.co") || host.equals("tumblr.com") ||host.equals("www.tumblr.com") ){
			URL forwardURL = null;
			if(url.getHost().equals("tmblr.co")){
				String tumblrCode = url.getPath();
				forwardURL = new URL("http://www.tumblr.com" + tumblrCode);
			}
			else{
				forwardURL = url;
			}
			// now get the location header
			HttpURLConnection con = (HttpURLConnection) forwardURL.openConnection();
			con.setInstanceFollowRedirects(false);
			String locStr = con.getHeaderField("Location");
			loc = new URL(locStr);
			con.disconnect();
		}
		// Now extract the post ID from the actual tumblr address
		String[] parts = loc.getPath().split("[/]");
		String postID = null;
		for (int i = 0; i < parts.length; i++) {
			if(parts[i].equals("post")){
				postID = parts[i+1];
				break;
			}
		}
		return postID;
	}

}
