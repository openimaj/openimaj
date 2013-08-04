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
