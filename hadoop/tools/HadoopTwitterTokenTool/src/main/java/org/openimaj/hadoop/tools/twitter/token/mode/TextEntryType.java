package org.openimaj.hadoop.tools.twitter.token.mode;

/**
 * The types of stats that can be held
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public enum TextEntryType{
	/**
	 * valid, this is the global counter 
	 */
	VALID, 
	/**
	 * invalid for some other reason 
	 */
	INVALID, 
	/**
	 * invalid because of malformed json 
	 */
	INVALID_JSON, 
	/**
	 * invalid because the entry being read had zero length 
	 */
	INVALID_ZEROLENGTH, 
	/**
	 * invalid because the entry being read had no time entry 
	 */
	INVALID_TIME, 
	/**
	 * an actual emit is made 
	 */
	ACUAL_EMITS,
}