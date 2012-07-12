package org.kohsuke.args4j.spi;

import java.lang.reflect.Field;

/**
 * An abstract Getter is of the type of the underlying bean and is not multivalued
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk) 
 *
 * @param <T>
 */
public abstract class AbstractGetter<T> implements Getter<T>{
	protected Class<?> type;
	protected Field f;
	protected Object bean;
	private String name;

	/**
	 * @param name 
	 * @param bean
	 * @param f
	 */
	public AbstractGetter(String name, Object bean, Field f) {
		this.type = bean.getClass();
		this.f = f;
		this.bean = bean;
		this.name = name;
	}
	
	@Override
	public String getOptionName() {
		return name;
	}
	
	@Override
	public Class<?> getType() {
		return type;
	}
	
	@Override
	public boolean isMultiValued() {
		return false;
	}
}
