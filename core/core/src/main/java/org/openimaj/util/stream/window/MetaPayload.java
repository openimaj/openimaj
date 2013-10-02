package org.openimaj.util.stream.window;

/**
 * A payload with metadata
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 * @param <PAYLOAD>
 * @param <META>
 */
public class MetaPayload<PAYLOAD, META> {
	private PAYLOAD payload;
	private META meta;

	/**
	 * @param payload
	 * @param meta
	 */
	public MetaPayload(PAYLOAD payload, META meta) {
		this.payload = payload;
		this.meta = meta;
	}

	/**
	 * @return the payload items in this window
	 */
	public PAYLOAD getPayload() {
		return this.payload;
	}

	/**
	 * @return infromation about the window
	 */
	public META getMeta() {
		return this.meta;
	}

	/**
	 * Create a new {@link MetaPayload} created from the given payload and
	 * metadata
	 * 
	 * @param a
	 *            the payload
	 * @param b
	 *            the metadata
	 * @return a new {@link MetaPayload} created from the given payload and
	 *         metadata
	 */
	public static <A, B> MetaPayload<A, B> create(A a, B b)
	{
		return new MetaPayload<A, B>(a, b);
	}
}
