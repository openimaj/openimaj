package org.openimaj.tools.reddit;

import java.util.List;

import org.openimaj.twitter.collection.StreamJSONStatusList.ReadableWritableJSON;

/**
 * The count split mode fills each file with an equal number of items.
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class CountSplitMode extends SplitMode {

	@Override
	public void output(List<ReadableWritableJSON> read) {
	}

}
