package org.openimaj.util.stream.window;




/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 * @param <PAYLOAD>
 * @param <META>
 */
public class Aggregation<PAYLOAD, META> {
	private PAYLOAD payload;
	private META meta;
	/**
	 * @param payload
	 * @param meta
	 */
	public Aggregation(PAYLOAD payload, META meta) {
		this.payload = payload;
		this.meta = meta;
	}

	/**
	 * @return the payload items in this window
	 */
	public PAYLOAD getPayload(){
		return this.payload;
	}

	/**
	 * @return infromation about the window
	 */
	public META getMeta(){
		return this.meta;
	}
}
