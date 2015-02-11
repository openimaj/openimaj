package org.openimaj.picslurper.output;

import java.net.URL;

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

	/**
	 * Called for every URL that is a failure
	 * @param url
	 * @param reason
	 */
	public void failedURL(URL url, String reason);

	/**
	 * When the picslurper is done
	 */
	public void finished();

	/**
	 * start things off
	 */
	public void prepare();
}
