package org.openimaj.video.xuggle;

import java.io.IOException;

import com.xuggle.xuggler.io.IURLProtocolHandler;
import com.xuggle.xuggler.io.IURLProtocolHandlerFactory;

/**
 * An {@link IURLProtocolHandlerFactory} for jar file resource urls
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
public class JarURLProtocolHandlerFactory implements IURLProtocolHandlerFactory {
	@Override
	public IURLProtocolHandler getHandler(String protocol, String url, int flags) {
		try {
			return new JarURLProtocolHandler(url);
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
