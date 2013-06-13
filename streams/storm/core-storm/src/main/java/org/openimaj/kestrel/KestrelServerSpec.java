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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.thrift7.TException;

import backtype.storm.spout.KestrelThriftClient;

/**
 * Define a connection to a single or set of Kestrel servers
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
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

	private static final Logger logger = Logger.getLogger(KestrelServerSpec.class);
	/**
	 * the kestrel host
	 */
	public String host;
	/**
	 * the kestrel host port
	 */
	public int port;

	private KestrelThriftClient client;

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
	public static KestrelServerSpec localMemcached() {
		final KestrelServerSpec ret = new KestrelServerSpec();
		ret.port = DEFAULT_KESTREL_MEMCACHED_PORT;
		return ret;
	}

	/**
	 * @return a local server spec using thrift
	 */
	public static KestrelServerSpec localThrift() {
		final KestrelServerSpec ret = new KestrelServerSpec();
		ret.port = DEFAULT_KESTREL_THRIFT_PORT;
		return ret;
	}

	/**
	 * @return a local server spec using text
	 */
	public static KestrelServerSpec localText() {
		final KestrelServerSpec ret = new KestrelServerSpec();
		ret.port = DEFAULT_KESTREL_TEXT_PORT;
		return ret;
	}

	/**
	 * Parse a list of strings in the format: host:port. If either host or port
	 * is left blank then the default is used
	 * 
	 * @param kestrelHosts
	 * @return all server specs
	 */
	public static List<KestrelServerSpec> parseKestrelAddressList(List<String> kestrelHosts) {
		final List<KestrelServerSpec> ret = new ArrayList<KestrelServerSpec>();
		for (final String hostport : kestrelHosts) {
			String host = "";
			String port = "";
			if (hostport.contains(":")) {
				final int split = hostport.lastIndexOf(":");
				host = hostport.substring(0, split);
				port = hostport.substring(split + 1);
			}
			else {
				host = hostport;
			}
			if (host.length() == 0)
				host = KestrelServerSpec.LOCALHOST;
			if (port.length() == 0)
				port = "" + KestrelServerSpec.DEFAULT_KESTREL_THRIFT_PORT;
			ret.add(new KestrelServerSpec(host, Integer.parseInt(port)));
		}
		return ret;
	}

	/**
	 * Construct a string that looks like this: "host1:port1 host2:port2" from
	 * the list of {@link KestrelServerSpec}
	 * 
	 * @param kestrelSpecList
	 * @param port
	 * @return a string that looks like this: "host1:port1 host2:port2"
	 */
	public static String kestrelAddressListAsString(List<KestrelServerSpec> kestrelSpecList, int port) {
		final List<String> retList = new ArrayList<String>();
		for (final KestrelServerSpec kestrelServerSpec : kestrelSpecList) {
			retList.add(String.format("%s:%s", kestrelServerSpec.host, port));
		}
		return StringUtils.join(retList, " ");
	}

	/**
	 * Get a valid Kestrel client, reconnecting if necessary
	 * 
	 * @return a valid client
	 * @throws TException
	 */
	public KestrelThriftClient getValidClient() throws TException {
		if (this.client == null) { // If client was blacklisted, remake it.
			logger.info("Attempting reconnect to kestrel " + this.host + ":" + this.port);
			this.client = new KestrelThriftClient(this.host, this.port);
		}
		return this.client;
	}

	/**
	 * An iterator to access a list of {@link KestrelServerSpec} in a round
	 * robin fasion. This iterator will always return a next as long as there
	 * are {@link KestrelServerSpec} in the provided list.
	 * 
	 * @param kestrelSpecList
	 * @return the iterator
	 */
	public static Iterator<KestrelThriftClient> thriftClientIterator(final List<KestrelServerSpec> kestrelSpecList) {
		return new Iterator<KestrelThriftClient>() {
			int index = 0;

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

			@Override
			public KestrelThriftClient next() {
				final int startIndex = index;
				do {
					final KestrelServerSpec toRet = kestrelSpecList.get(index);
					index++;
					if (index >= kestrelSpecList.size()) {
						index = 0;
					}
					try {
						return toRet.getValidClient();
					} catch (final TException e) {
					}
				} while (index != startIndex);
				throw new RuntimeException("Couldn't find valid client");
			}

			@Override
			public boolean hasNext() {
				return kestrelSpecList.size() > 0;
			}
		};
	}

	public void close() {
		this.client.close();
	}

}
