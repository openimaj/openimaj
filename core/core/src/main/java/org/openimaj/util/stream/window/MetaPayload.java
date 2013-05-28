package org.openimaj.util.stream.window;





/**
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
	public PAYLOAD getPayload(){
		return this.payload;
	}

	/**
	 * @return infromation about the window
	 */
	public META getMeta(){
		return this.meta;
	}

	/**
	 * @param a
	 * @param b
	 * @return
	 */
	public static <A,B> MetaPayload<A, B> create(A a, B b)
	{
		return new MetaPayload<A, B>(a, b);
	}
}
