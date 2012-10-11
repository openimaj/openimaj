package org.openimaj.rdf.storm.tool.source;

import java.io.IOException;
import java.net.URL;

import org.kohsuke.args4j.Option;
import org.openimaj.kestrel.NTripleKestrelTupleWriter;

/**
 * Load triples from this URI.
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class URITriplesInputMode implements TriplesInputMode {

	/**
	 * The name of the topology to submit
	 */
	@Option(
			name = "--uri-source",
			aliases = "-us",
			required = true,
			usage = "The URI containing NTriples",
			metaVar = "STRING")
	String url;

	@Override
	public NTripleKestrelTupleWriter asKestrelWriter() throws IOException {
		return new NTripleKestrelTupleWriter(new URL(url));
	}

}
