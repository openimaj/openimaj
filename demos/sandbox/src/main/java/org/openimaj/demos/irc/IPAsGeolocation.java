package org.openimaj.demos.irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.openimaj.io.HttpUtils;
import org.openimaj.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Given an IP, output a geolocation from freegeoio.net
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class IPAsGeolocation implements Function<String, FreeGeoIPLocation> {

	private Gson gson;
	public IPAsGeolocation() {
		this.gson = new GsonBuilder().create();
	}
	@Override
	public FreeGeoIPLocation apply(String ip) {

		InputStream is;
		try {
			URL url = new URL("http://freegeoip.net/json/" + ip);
			is = HttpUtils.readURL(url);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			for (String line; (line = reader.readLine()) != null;) {
				if(line.contains("Not Found")) return null;
				try{
					return gson.fromJson(line, FreeGeoIPLocation.class);
				}catch(Throwable t){
					return null;
				}
			}
		} catch (IOException e) {
			return null;
		}
		return null;
	}

}
