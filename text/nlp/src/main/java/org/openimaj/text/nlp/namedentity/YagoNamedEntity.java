package org.openimaj.text.nlp.namedentity;

import java.util.ArrayList;

/**
 * Used in the construction of the Yago EntityExtraction resource folder.
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class YagoNamedEntity extends NamedEntity{
	
	@SuppressWarnings("javadoc")
	public ArrayList<String> aliasList;
	private StringBuffer rawContext;
	@SuppressWarnings("javadoc")
	public String wikiURL;
	
	@SuppressWarnings("javadoc")
	public YagoNamedEntity() {
		super();
		aliasList = new ArrayList<String>();
		rawContext= new StringBuffer();
	}
	
	@SuppressWarnings("javadoc")
	public YagoNamedEntity(String rootName, Type type) {
		super(rootName, type);
		aliasList = new ArrayList<String>();
		rawContext= new StringBuffer();
	}
	
	@SuppressWarnings("javadoc")
	public void addAlias(String alias){
		aliasList.add(alias);
	}
	
	@SuppressWarnings("javadoc")
	public void addContext(String context){
		rawContext.append(context);
	}
	
	@SuppressWarnings("javadoc")
	public String getContext(){
		return rawContext.toString();
	}
	
	
	
}
