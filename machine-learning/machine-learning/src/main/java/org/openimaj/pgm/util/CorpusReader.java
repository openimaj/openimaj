package org.openimaj.pgm.util;

import java.io.IOException;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface CorpusReader {
	/**
	 * @return produce a corpus
	 * @throws IOException 
	 */
	public Corpus readCorpus() throws IOException;
}
