package org.openimaj.kestrel;


/**
 * Define a connection to a single or set of Kestrel servers
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class KestrelServerSpec {

	/**
	 * the default kestrel memcached port
	 */
	public static final int DEFAULT_KESTREL_MEMCACHED_PORT = 22133;
	
	/**
	 * the default kestrel thrift port
	 */
	public static final int DEFAULT_KESTREL_THRIFT_PORT = 2229;

	
	/**
	 * the default kestrel text protocol port
	 */
	public static final int DEFAULT_KESTREL_TEXT_PORT = 2222;
	
	/**
	 * the localhost
	 */
	public static final String LOCALHOST = "127.0.0.1";
	/**
	 * the kestrel host
	 */
	public String host;
	/**
	 * the kestrel host port
	 */
	public int port;

	/**
	 * A single kestrel host
	 * 
	 * @param kestrelHost
	 * @param port
	 */
	public KestrelServerSpec(String kestrelHost, int port) {
		this.host = kestrelHost;
		this.port = port;
	}

	private KestrelServerSpec() {
		this.host = LOCALHOST;
	}
	
	/**
	 * @return a local server spec using memcached
	 */
	public static KestrelServerSpec localMemcached(){
		KestrelServerSpec ret = new KestrelServerSpec();
		ret.port = DEFAULT_KESTREL_MEMCACHED_PORT;
		return ret;
	}
	
	/**
	 * @return a local server spec using thrift
	 */
	public static KestrelServerSpec localThrift(){
		KestrelServerSpec ret = new KestrelServerSpec();
		ret.port = DEFAULT_KESTREL_THRIFT_PORT;
		return ret;
	}
	
	/**
	 * @return a local server spec using text
	 */
	public static KestrelServerSpec localText(){
		KestrelServerSpec ret = new KestrelServerSpec();
		ret.port = DEFAULT_KESTREL_TEXT_PORT;
		return ret;
	}

}
