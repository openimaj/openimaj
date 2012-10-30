package org.openimaj.rdf.storm.tool.source;

import java.io.IOException;

import org.openimaj.kestrel.NTripleKestrelTupleWriter;

/**
 * {@link TriplesInputMode} can write triples to a Kestrel message queue by
 * providing a {@link NTripleKestrelTupleWriter} instance
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface TriplesInputMode {

	/**
	 * @return
	 * @throws IOException
	 */
	public NTripleKestrelTupleWriter asKestrelWriter() throws IOException;

}
