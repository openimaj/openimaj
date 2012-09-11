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
		Person,		
		@SuppressWarnings("javadoc")
		Location
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
	
	@SuppressWarnings("javadoc")
	public NamedEntity(){
		
	}
	
	@SuppressWarnings("javadoc")
	public NamedEntity(String rootName, Type type){
		this.rootName = rootName;
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((rootName == null) ? 0 : rootName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NamedEntity other = (NamedEntity) obj;
		if (rootName == null) {
			if (other.rootName != null)
				return false;
		} else if (!rootName.equals(other.rootName))
			return false;
		return true;
	}
}
