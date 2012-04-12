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
package org.openimaj.hadoop.tools.download;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.mortbay.jetty.security.Credential.MD5;

public enum URLConstructionMode implements CmdLineOptionsProvider {
	IMAGE_NET {
		@Override
		public URLConstructionModeOp getOptions() {
			return new URLConstructionModeOp() {
				@Override
				public List<URI> getURI(String url) throws URISyntaxException {
					List<URI> l = new ArrayList<URI>();
					l.add(new URI(url));
					return l;
				}

				@Override
				public String getID(String key,String url) {
					int lastDot = url.lastIndexOf(".");
					String extention = url.substring(lastDot);
					return key + extention;
				}				
			};
		}
	},
	WIKIPEDIA_FILE {
		@Override
		public URLConstructionModeOp getOptions() {
			return new URLConstructionModeOp() {
				@Option(name="--wikipedia-baseurl", aliases="-wbase", required=false, usage="wikipedia upload files base urls. add many urls to check different locations for each image. defaults to upload.wikimedia.org/wikipedia/commons and upload.wikimedia.org/wikipedia/en", multiValued=true)
				private List<String> wikipediaBase;

				@Override
				public List<URI> getURI(String url) throws URISyntaxException {
					List<URI> l = new ArrayList<URI>();
					String[] parts = url.split(":");
					String hash = MD5.digest(parts[1]).split(":")[1];
					String dirStructure = String.format("%s/%s", hash.substring(0, 1),hash.substring(0, 2));
					for(String base : wikipediaBase){
						String compelteURL = String.format("%s/%s/%s", base,dirStructure,parts[1].replace(" ", "_"));
						l.add(new URI(compelteURL));
					}
					return l;
				}

				@Override
				public String getID(String key, String url) {
					return key;
				}

				@Override
				public void setup(){
					if(wikipediaBase == null){
						wikipediaBase = new ArrayList<String>();
						wikipediaBase.add("http://upload.wikimedia.org/wikipedia/commons");
						wikipediaBase.add("http://upload.wikimedia.org/wikipedia/en");
					}
				}
			};
		}
	};

	@Override
	public abstract URLConstructionModeOp getOptions();

	public abstract class URLConstructionModeOp {
		public abstract List<URI> getURI(String url) throws Exception;

		public abstract String getID(String key, String url) ;

		public void setup() {

		}
	}
}
