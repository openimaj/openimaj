package org.openimaj.math.hash;

import java.lang.reflect.Constructor;

import cern.jet.random.engine.MersenneTwister;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @param <A> The item to hash
 * @param <B> The HashFunction class
 *
 */
public class HashFunctionFactory<A,B extends HashFunction<A,B>> {
	private MersenneTwister random;
	private Class<? extends B> clazz;

	/**
	 * 
	 */
	public HashFunctionFactory() {
		this(new MersenneTwister());
	}

	/**
	 * @param random
	 */
	public HashFunctionFactory(MersenneTwister random) {
		this.random = random;
	}
	/**
	 * @param random
	 * @param clazz the class of {@link HashFunction} to generate
	 */
	public HashFunctionFactory(MersenneTwister random, Class<? extends B> clazz) {
		this.random = random;
		this.clazz = clazz;
	}
	
	/**
	 * @param clazz the class of {@link HashFunction} to generate
	 */
	public HashFunctionFactory(Class<? extends B> clazz) {
		this.random = new MersenneTwister();
		this.clazz = clazz;
	}
	
	/**
	 * Create a new {@link HashFunction} instance
	 * @return the new instance
	 */
	public B create(){
		return create(this.clazz);
	}
	
	/**
	 * Create a new {@link HashFunction} instance
	 * @param clazz the instance class
	 * @return the new instance
	 */
	private B create(Class<? extends B> clazz){
		try {
			Constructor<? extends B> construc = clazz.getConstructor(MersenneTwister.class);
			return construc.newInstance(this.random);
		}
		catch(Throwable e){
			return null;
		}
	}

	/**
	 * @param clazz 
	 * @return a factory instance
	 */
	public static HashFunctionFactory<String, StringMurmurHashFunction> get(Class<StringMurmurHashFunction> clazz) {
		return new HashFunctionFactory<String,StringMurmurHashFunction>(clazz);
	}
}
