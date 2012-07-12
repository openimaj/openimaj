package org.openimaj.text.nlp.namedentity;

/**
 * Container class that for a Named Entity
 * @author Laurence Willmore <lgw1e10@ecs.soton.ac.uk>
 *
 */
public class NamedEntity {
	/**
	 * Unique defining name.
	 */
	public String rootName;
	/**
	 * Type of entity eg. Company.
	 */
	public String entityType;	
	
	/**
	 * 
	 * @param rootName = Unique defining name.
	 * @param entityType = Type of entity eg. Company.
	 */
	public NamedEntity(String rootName, String entityType) {		
		this.rootName = rootName;
		this.entityType = entityType;
	}
	
	/**
	 * Empty Constructor
	 */
	public NamedEntity(){};



	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof NamedEntity))return false;
		NamedEntity that = (NamedEntity)obj;
		if(!this.rootName.equals(that.rootName))return false;
		if(!this.entityType.equals(that.entityType))return false;
		return true;
	}
	
	

}
