package org.openimaj.util.stream.window;

import java.util.List;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <PAYLOAD> what the window contains a list of
 * @param <META> information about the window
 *
 */
public final class Window<PAYLOAD,META> extends Aggregation<List<PAYLOAD>,META>{



	/**
	 * @param meta
	 * @param payload
	 */
	public Window(META meta, List<PAYLOAD> payload) {
		super(payload,meta);
	}


}
