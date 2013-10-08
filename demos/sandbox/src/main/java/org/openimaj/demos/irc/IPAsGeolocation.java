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
