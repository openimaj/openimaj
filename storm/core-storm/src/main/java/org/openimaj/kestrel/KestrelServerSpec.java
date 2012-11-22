/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.kestrel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;


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

	/**
	 * Parse a list of strings in the format: host:port. If either host or port is left blank then
	 * the default is used
	 * @param kestrelHosts
	 * @return all server specs
	 */
	public static List<KestrelServerSpec> parseKestrelAddressList(List<String> kestrelHosts) {
		List<KestrelServerSpec> ret = new ArrayList<KestrelServerSpec>();
		for (String hostport : kestrelHosts) {
			String host = "";
			String port = "";
			if(hostport.contains(":")){
				int split = hostport.lastIndexOf(":");
				host = hostport.substring(0, split);
				port = hostport.substring(split+1);
			}
			else{
				host = hostport;
			}
			if(host.length()==0)host = KestrelServerSpec.LOCALHOST;
			if(port.length()==0)port = "" + KestrelServerSpec.DEFAULT_KESTREL_THRIFT_PORT;
			ret.add(new KestrelServerSpec(host, Integer.parseInt(port)));
		}
		return ret;
	}

	/**
	 * Construct a string that looks like this: "host1:port1 host2:port2" from the list of {@link KestrelServerSpec}
	 * @param kestrelSpecList
	 * @param port
	 * @return  a string that looks like this: "host1:port1 host2:port2"
	 */
	public static String kestrelAddressListAsString(List<KestrelServerSpec> kestrelSpecList, int port) {
		List<String> retList = new ArrayList<String>();
		for (KestrelServerSpec kestrelServerSpec : kestrelSpecList) {
			retList.add(String.format("%s:%s", kestrelServerSpec.host,port));
		}
		return StringUtils.join(retList, " ");
	}

}
