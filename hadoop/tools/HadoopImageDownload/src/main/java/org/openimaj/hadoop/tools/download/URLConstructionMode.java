package org.openimaj.hadoop.tools.download;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.mortbay.jetty.security.Credential.MD5;

public enum URLConstructionMode implements CmdLineOptionsProvider{
	IMAGE_NET {
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
	},
	WIKIPEDIA_FILE {
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

	@Override
	public Object getOptions() {
		return this;
	}

	public abstract List<URI> getURI(String url) throws Exception;
	public abstract String getID(String key, String url) ;
	public void setup(){
		
	}

}
