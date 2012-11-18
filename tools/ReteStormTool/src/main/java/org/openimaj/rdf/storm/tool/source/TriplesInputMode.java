package org.openimaj.rdf.storm.tool.source;

import java.io.IOException;

import org.openimaj.kestrel.KestrelTupleWriter;
import org.openimaj.kestrel.NTripleKestrelTupleWriter;
import org.openimaj.rdf.storm.tool.ReteStormOptions;

/**
 * {@link TriplesInputMode} can write triples to a Kestrel message queue by
 * providing a {@link NTripleKestrelTupleWriter} instance
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public interface TriplesInputMode {

	/**
	 * initialise the input mode
	 * 
	 * @param options
	 */
	public void init(ReteStormOptions options);

	/**
	 * @return the writer used to write triples to kestrel initially
	 * @throws IOException
	 */
	public KestrelTupleWriter asKestrelWriter() throws IOException;

}
