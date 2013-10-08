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


import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Make using log4j slightly less awful
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class LoggerUtils {
	/**
	 * Prepare a console logger with a default layout.
	 * This allows {@link Logger} instances to be used while
	 * still being useful as the line to prepare logger can be removed
	 * and the default log4j.properties will be correctly listened to.
	 */
	public static void prepareConsoleLogger(){
		  ConsoleAppender console = new ConsoleAppender(); //create appender
		  //configure the appender
		  String PATTERN = "%m\n";
		  console.setLayout(new PatternLayout(PATTERN)); 
		  console.setThreshold(Level.DEBUG);
		  console.activateOptions();
		  //add appender to any Logger (here is root)
		  Logger.getRootLogger().addAppender(console);
	}

	/**
	 * Debug message occationally
	 * @param logger
	 * @param message
	 * @param b
	 */
	public static void debug(Logger logger, String message, boolean b) {
		if(b){
			logger.debug(message);
		}
	}

	/**
	 * Calls {@link #format(Logger, String, Level, Object...)} with level {@link Level#DEBUG}
	 * @param logger
	 * @param string
	 * @param obj
	 */
	public static void debugFormat(Logger logger, String string, Object ... obj) {
		format(logger,string,Level.DEBUG,obj);
	}

	/**
	 * Checks the level, if acceptable calls {@link String#format(String, Object...)} at the appropriate level
	 * @param logger
	 * @param string
	 * @param debug
	 * @param obj
	 */
	public static void format(Logger logger, String string, Level debug, Object ... obj) {
		Level l = logger.getEffectiveLevel();
		if(debug.isGreaterOrEqual(l)){
			logger.log(debug, String.format(string,obj));
		}
	}
}
