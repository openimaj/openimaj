package org.openimaj.rdf.storm.tool.source;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Option;
import org.openimaj.kestrel.GraphKestrelTupleWriter;
import org.openimaj.kestrel.KestrelTupleWriter;
import org.openimaj.kestrel.NTripleKestrelTupleWriter;
import org.openimaj.rdf.storm.tool.ReteStormOptions;
import org.openimaj.rdf.storm.tool.lang.RuleLanguageMode;

/**
 * Load triples from this URI.
 *
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
			metaVar = "STRING",
			multiValued=true)
	List<String> urlStrings = new ArrayList<String>();
	private boolean tripleMode;

	@Override
	public KestrelTupleWriter asKestrelWriter() throws IOException {
		ArrayList<URL> urlList = new ArrayList<URL>();
		for (String urls : urlStrings) {
			urlList.add(new URL(urls));
		}
		if (tripleMode)
			return new NTripleKestrelTupleWriter(urlList);
		else
			return new GraphKestrelTupleWriter(urlList);
	}

	@Override
	public void init(ReteStormOptions options) {
		this.tripleMode = true;
		if (options.ruleLanguageMode == RuleLanguageMode.SPARQL) {
			this.tripleMode = false;
		}
	}

}
