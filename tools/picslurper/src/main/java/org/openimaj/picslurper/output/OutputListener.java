package org.openimaj.picslurper.output;

import org.openimaj.picslurper.StatusConsumer;
import org.openimaj.picslurper.StatusConsumption;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public interface OutputListener {
	/**
	 * Called for each image successfully downloaded. This function is told
	 * about the status which generated the image, the url from which the image
	 * was eventually downloaded (after various redirects) and the output file.
	 * The {@link StatusConsumption} instance from the {@link StatusConsumer} that
	 * processed the status is also provided
	 * @param written
	 *
	 *
	 */
	public void newImageDownloaded(WriteableImageOutput written);
}
