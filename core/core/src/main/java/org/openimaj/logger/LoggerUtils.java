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
package org.openimaj.logger;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Filter.Result;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Make using log4j slightly less awful
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class LoggerUtils {
	/**
	 * Prepare a console logger with a default layout. This allows {@link Logger}
	 * instances to be used while still being useful as the line to prepare logger
	 * can be removed and the default log4j.properties will be correctly listened
	 * to.
	 */
	public static void prepareConsoleLogger() {
		final String PATTERN = "%m\n";
		final PatternLayout layout = PatternLayout.newBuilder().withPattern(PATTERN).build();
		final ThresholdFilter filter = ThresholdFilter.createFilter(Level.DEBUG, Result.ACCEPT, Result.DENY);

		final ConsoleAppender console = ConsoleAppender.newBuilder().withName("default-consoleappender")
				.withLayout(layout).withFilter(filter).build();

		// add appender to any Logger (here is root)
		final Logger root = LogManager.getRootLogger();
		if (root instanceof org.apache.logging.log4j.core.Logger)
			((org.apache.logging.log4j.core.Logger) root).addAppender(console);
	}

	/**
	 * Debug message occationally
	 *
	 * @param logger
	 * @param message
	 * @param b
	 */
	public static void debug(Logger logger, String message, boolean b) {
		if (b) {
			logger.debug(message);
		}
	}

	/**
	 * Calls {@link #format(Logger, String, Level, Object...)} with level
	 * {@link Level#DEBUG}
	 *
	 * @param logger
	 * @param string
	 * @param obj
	 */
	public static void debugFormat(Logger logger, String string, Object... obj) {
		format(logger, string, Level.DEBUG, obj);
	}

	/**
	 * Checks the level, if acceptable calls
	 * {@link String#format(String, Object...)} at the appropriate level
	 *
	 * @param logger
	 * @param string
	 * @param debug
	 * @param obj
	 */
	public static void format(Logger logger, String string, Level debug, Object... obj) {
		final Level l = logger.getLevel();
		if (debug.isMoreSpecificThan(l)) {
			logger.log(debug, String.format(string, obj));
		}
	}
}
