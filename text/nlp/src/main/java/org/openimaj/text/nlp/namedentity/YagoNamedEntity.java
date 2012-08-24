package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;

public class YagoNamedEntity extends NamedEntity{
	
	public ArrayList<String> aliasList;
	private StringBuffer rawContext;
	public String wikiURL;
	
	public YagoNamedEntity() {
		super();
		aliasList = new ArrayList<String>();
		rawContext= new StringBuffer();
	}
	
	public YagoNamedEntity(String rootName, Type type) {
		super(rootName, type);
		aliasList = new ArrayList<String>();
		rawContext= new StringBuffer();
	}
	
	public void addAlias(String alias){
		aliasList.add(alias);
	}
	
	public void addContext(String context){
		rawContext.append(context);
	}
	
	public String getContext(){
		return rawContext.toString();
	}
	
	
	
}
