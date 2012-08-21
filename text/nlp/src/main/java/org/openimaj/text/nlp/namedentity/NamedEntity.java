package org.openimaj.text.nlp.namedentity;

/**
 * Container Class for Named Entity values
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class NamedEntity {
	
	/**
	 * Type of Named Entity
	 */
	public enum Type{
		@SuppressWarnings("javadoc")
		Organisation,
		@SuppressWarnings("javadoc")
		Person
	}
	
	/**
	 *Type of Named Entity 
	 */
	public Type type;
	/**
	 * Unique root name of entity
	 */
	public String rootName;
	/**
	 * The string that resulted in a match
	 */
	public String stringMatched;
	/**
	 * Start token of the match
	 */
	public int startToken;
	/**
	 * Stop token of the match
	 */
	public int stopToken;
	/**
	 * Start char of the match
	 */
	public int startChar;
	/**
	 * Stop char of the match
	 */
	public int stopChar;
	
	@Override
	public String toString() {
		return "NamedEntity [type=" + type + ", rootName=" + rootName
				+ ", startToken=" + startToken + ", stopToken=" + stopToken
				+ "]";
	}
	
	

}
