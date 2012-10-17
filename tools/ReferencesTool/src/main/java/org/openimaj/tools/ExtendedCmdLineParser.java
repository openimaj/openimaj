/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
