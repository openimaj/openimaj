package org.openimaj.tools;

import java.lang.reflect.Method;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.spi.OptionHandler;

/**
 * Extended {@link CmdLineParser} to allow for the possibility of options that
 * should be consumed as arguments
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 */
class ExtendedCmdLineParser extends CmdLineParser {
	public ExtendedCmdLineParser(Object bean) {
		super(bean);
	}

	@Override
	protected boolean isOption(String arg) {
		final boolean isKeyValuePair = arg.indexOf('=') != -1;

		final OptionHandler<?> handler = isKeyValuePair ? findHandler(arg) : findByName(arg);

		if (handler == null) {
			return false;
		}

		return super.isOption(arg);
	}

	private OptionHandler<?> findByName(String name) {
		Method m;
		try {
			m = CmdLineParser.class.getDeclaredMethod("findOptionByName", String.class);
			m.setAccessible(true);
			return (OptionHandler<?>) m.invoke(this, name);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	private OptionHandler<?> findHandler(String name) {
		try {
			final Method m = CmdLineParser.class.getDeclaredMethod("findOptionHandler", String.class);
			m.setAccessible(true);
			return (OptionHandler<?>) m.invoke(this, name);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}
}
