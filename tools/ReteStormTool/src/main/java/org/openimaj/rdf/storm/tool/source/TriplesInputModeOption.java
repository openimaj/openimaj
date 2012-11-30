package org.openimaj.rdf.storm.tool.source;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.kestrel.KestrelTupleWriter;
import org.openimaj.rdf.storm.tool.ReteStormOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Triple;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public enum TriplesInputModeOption implements CmdLineOptionsProvider {
	/**
	 * Consume triples from a URI
	 */
	URI {
		@Override
		public TriplesInputMode getOptions() {
			return new URITriplesInputMode();
		}

	},
	NONE {

		@Override
		public TriplesInputMode getOptions() {
			return new TriplesInputMode() {

				private KestrelTupleWriter writer;
				private Logger logger = LoggerFactory.getLogger(getClass());

				@Override
				public void init(ReteStormOptions options) {
					ArrayList<URL> urlList = new ArrayList<URL>();
					for (String urls : options.kestrelHosts) {
						try {
							urlList.add(new URL(urls));
						} catch (MalformedURLException e) {
							logger.debug("failing");
						}
					}
					try {
						writer = new KestrelTupleWriter(urlList) {

							@Override
							public void send(List<Triple> cache) {

							}
						};
					} catch (IOException e) {
						logger.debug("failing");
					}
				}

				@Override
				public KestrelTupleWriter asKestrelWriter() throws IOException {
					return writer;
				}
			};
		}

	};

	@Override
	public abstract TriplesInputMode getOptions();
}
