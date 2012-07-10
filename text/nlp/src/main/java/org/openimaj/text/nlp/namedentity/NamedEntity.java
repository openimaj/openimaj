package org.openimaj.text.nlp.namedentity;

public class NamedEntity {
	
	public String rootName;
	public String entityType;	
	
	public NamedEntity(String rootName, String entityType) {		
		this.rootName = rootName;
		this.entityType = entityType;
	}
	
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
