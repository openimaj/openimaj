package org.openimaj.io;

import java.io.File;
import java.io.IOException;


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
 * if the cachable object returns true on {@link Cachable#skipCache}, caching is skipped, the object is both
 * not saved and no attempt is made to load.
 * 
 * @author ss
 *
 */
public class Cache {
	
	private final static String cacheProp = "org.openimaj.cache.dir";
	private static final String CACHE_DIR_NAME = ".OIcache";
	/**
	 * @param wang
	 * @param objects
	 * @return
	 */
	public static <T> T create(Class<? extends T> clazz, Object ... objects ) {
		Class<?>[] classes = new Class<?>[objects.length];
		int i = 0;
		for (Object object : objects) {
			classes[i++] = object.getClass();
		}
		T instance = null;
		try {
			instance = clazz.getConstructor(classes).newInstance(objects);
		} catch (Exception e) {
		}
		if(instance instanceof Cachable){
			Cachable cinstance = (Cachable)instance;
			if(!cinstance.skipCache()){
				String cdir = System.getProperty(cacheProp);
				if(cdir == null || cdir.equals(""))
					cdir = System.getProperty("user.home") + "/" + CACHE_DIR_NAME;
				
				String pname = clazz.getPackage().getName();
				String cname = clazz.getName();
				String uname = cinstance.uniqueName();
				File cachedLocation = new File(cdir,String.format("%s/%s/%s",pname,cname,uname));
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
						if(!cachedLocation.getParentFile().mkdirs()){
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
					System.err.println("Error reading or writing object from cache");
				}
					
			}
		}
		return instance;
	}
	
	private static <T extends ReadWriteableASCII> T cacheFromASCII(T rwascii,File f) throws IOException {
		return IOUtils.read(f, (Class<T>)rwascii.getClass());
	}
	
	private static <T extends ReadWriteableBinary > T cacheFromBinary(T rwbin,File f) throws IOException {
		return IOUtils.read(f, (Class<T>)rwbin.getClass());
	}

	public static void main(String[] args) {
		String a = create(String.class);
		Integer b = create(Integer.class);
	}
	
}
