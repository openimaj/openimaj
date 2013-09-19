package org.openimaj.util.stream;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class BufferedReaderStream extends AbstractStream<String>{
	private BufferedReader reader;
	private String line;

	/**
	 * Construct with the given collection.
	 * @param reader 
	 * 
	 */
	public BufferedReaderStream(BufferedReader reader) {
		this.reader = reader;
		try {
			this.line = reader.readLine();
		} catch (IOException e) {
			line = null;
		}
	}

	@Override
	public boolean hasNext() {
		return line!=null;
	}

	@Override
	public String next() {
		String pline = line;
		try {
			line = this.reader.readLine();
		} catch (IOException e) {
			line = null;
		}
		return pline;
	}
}
