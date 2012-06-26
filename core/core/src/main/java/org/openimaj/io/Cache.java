/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.io;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;


import cern.colt.Arrays;


/**
 * In its simplest form this function constructs an object using the construct found for the objects
 * specified. This works with all java classes. If the class also happens to be cachable, the property
 * org.openimaj.cache.dir is use (or $HOME/.OIcache) to save the class. The object instance is saved in
 * $CACHE_DIR/classpackage/classname/unique-name where unique-name is taken from cachable. 
 * 
 * If cachable objects already exist they are read in using {@link IOUtils#read(java.io.File)}.
 * 
 * Once created, cachable objects are saved using either {@link IOUtils#writeASCII(java.io.File, WriteableASCII)} or
 * {@link IOUtils#writeBinary(java.io.File, WriteableBinary)}
 * 
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class Cache {
	
	private final static String cacheProp = "org.openimaj.cache.dir";
	private static final String CACHE_DIR_NAME = ".OIcache";
	
	/**
	 * load an instance using {@link #load(Object, Class, boolean)} with class as instance#getClass. The cache lookup is not
	 * skipped. 
	 * @param <T>
	 * @param instance
	 * @return a loaded instance
	 */
	public static <T> T load(T instance){
		@SuppressWarnings("unchecked")
		T ret = (T) load(instance,instance.getClass(),false);
		return ret; 
	}
	
	/**
	 * Create an instance of the clazz for the objects (for the constructor).
	 * If the class creates {@link Cachable} instances, an attempt is made to load
	 * the instance from the Cache. 
	 * 
	 * @param <T> The type of the object returned
	 * @param clazz the class which to get a cached instance of
	 * @param objects the parameters used to instantiate and index the cached object
	 * @return an instance of the clazz 
	 */
	public static <T> T load(Class<? extends T> clazz, Object ... objects ) {
		return load(clazz,false,objects);
	}
	
	/**
	 * Create an instance of the clazz for the objects (for the constructor).
	 * If the class creates {@link Cachable} instances, an attempt is made to load
	 * the instance from the Cache. 
	 * 
	 * @param <T> The type of the object returned
	 * @param clazz the class which to get a cached instance of
	 * @param objects the parameters used to instantiate and index the cached object
	 * @return an instance of the clazz 
	 */
	public static <T> T loadSkipCache(Class<? extends T> clazz, Object ... objects ) {
		return load(clazz,true,objects);
	}
	
	/**
	 * Clear the cache entry for a given clazz and a set of constructors
	 * @param <T>
	 * @param clazz
	 * @param objects
	 */
	public static <T extends Cachable> void clear(Class<T> clazz, Object ... objects) {
		T instance = createInstance(clazz,objects);
		File location = constructCachedLocation(instance, clazz);
		FileUtils.deleteRecursive(location);
	}
	
	/**
	 * Clear the cache entry for a given cachable object
	 * @param instance
	 */
	public static void clear(Cachable instance) {
		File location = constructCachedLocation(instance, instance.getClass());
		FileUtils.deleteRecursive(location);
	}
	
	/**
	 * @param <T> The type of the object returned
	 * @param clazz the class which to get a cached instance of
	 * @param skipcache force the cache to be skipped, for a new instance to be returned 
	 * @param objects the parameters used to instantiate and index the cached object
	 * @return an instance of the clazz
	 */
	private static <T> T load(Class<? extends T> clazz, boolean skipcache, Object ... objects ) {
		T instance = createInstance(clazz,objects);
		return load(instance,clazz,skipcache);
	}
	
	/**
	 * @param <T> The type of the object returned
	 * @param instance the instance to attempt to load from the cache if the instance is {@link Cachable}
	 * @param clazz the class which to get a cached instance of
	 * @param skipcache force the cache to be skipped, for a new instance to be returned
	 * @return an instance of the clazz
	 */
	@SuppressWarnings("unchecked")
	public static <T> T load(T instance, Class<? extends T> clazz, boolean skipcache) {
		if(instance instanceof Cachable && !skipcache){
			Cachable cinstance = (Cachable)instance;
			File cachedLocation = constructCachedLocation(cinstance,clazz);
			try{
				// if it exists load it!
				if(cachedLocation.exists()){
					if(cinstance instanceof ReadWriteableASCII){
						ReadWriteableASCII rwinstance = cacheFromASCII((ReadWriteableASCII)cinstance,cachedLocation);
						instance = (T) rwinstance;
					}
					else if(cinstance instanceof ReadWriteableBinary){
						ReadWriteableBinary rwinstance = cacheFromBinary((ReadWriteableBinary)cinstance,cachedLocation);
						instance = (T) rwinstance;
					}
				}
				// if not, create the parent directory and save it!
				else{
					if(!cachedLocation.getParentFile().exists() && !cachedLocation.getParentFile().mkdirs()){
						throw new IOException("Couldn't create cache directory!");
					}
					if(cinstance instanceof ReadWriteableASCII){
						IOUtils.writeASCII(cachedLocation, (ReadWriteableASCII) instance);
					}
					else if(cinstance instanceof ReadWriteableBinary){
						IOUtils.writeBinary(cachedLocation, (ReadWriteableBinary) instance);
					}
				}
				
			}
			catch(Exception e){
				e.printStackTrace();
				System.err.println("Error reading or writing object from cache");
			}
		}
		return instance;
	}
	
	private static File constructCachedLocation(Cachable cinstance, Class<?> clazz) {
		String cdir = System.getProperty(cacheProp);
		if(cdir == null || cdir.equals(""))
			cdir = System.getProperty("user.home") + "/" + CACHE_DIR_NAME;
		
		String pname = clazz.getPackage().getName();
		String cname = clazz.getName();
		String uname = cinstance.identifier();
		File cachedLocation = new File(cdir,String.format("%s/%s/%s",pname,cname,uname));
		return cachedLocation;
	}

	private static <T> T createInstance(Class<T> clazz, Object ... objects) {
		Class<?>[] classes = new Class<?>[objects.length];
		int i = 0;
		for (Object object : objects) {
			classes[i++] = object.getClass();
		}
		T instance = null;
		try {
			instance = clazz.getConstructor(classes).newInstance(objects);
		} catch (Exception e) {
			System.err.format("Error finding constructor for class %s with classes %s\n",clazz.toString(),Arrays.toString(classes));
		}
		return instance;
	}

	@SuppressWarnings("unchecked")
	private static <T extends ReadWriteableASCII> T cacheFromASCII(T rwascii,File f) throws IOException {
		return IOUtils.read(f, (Class<T>)rwascii.getClass());
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends ReadWriteableBinary > T cacheFromBinary(T rwbin,File f) throws IOException {
		return IOUtils.read(f, (Class<T>)rwbin.getClass());
	}
	
	static class CachableStringInteger implements CachableASCII{
		int integer;
		String string;
		
		public CachableStringInteger(){
			
		}
		
		public CachableStringInteger(Integer integer, String string) {
			this.integer = integer;
			this.string = string;
		}
		
		@Override
		public void readASCII(Scanner in) throws IOException {
			this.integer = Integer.parseInt(in.nextLine());
			this.string = in.nextLine();
		}

		@Override
		public String asciiHeader() {
			return "STRING_INT\n";
		}

		@Override
		public void writeASCII(PrintWriter out) throws IOException {
			out.println(integer);
			out.println(string);
		}

		@Override
		public String identifier() {
			return String.format("%d-%s",integer,string);
		}
		
		@Override
		public String toString() {
			return identifier();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String a = load(String.class,"wang");
		System.out.println("This is the created string: " + a);
		CachableStringInteger csi = load(CachableStringInteger.class,1,"wang");
		System.out.println("Got this from the cache.create():" + csi);
	}
	
}
