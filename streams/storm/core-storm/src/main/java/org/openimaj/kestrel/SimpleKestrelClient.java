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

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

import backtype.storm.spout.KestrelThriftClient;

import com.twitter.finagle.ChannelClosedException;
import com.twitter.finagle.ServiceFactory;
import com.twitter.finagle.builder.ClientBuilder;
import com.twitter.finagle.builder.ClientConfig.Yes;
import com.twitter.finagle.kestrel.java.Client;
import com.twitter.finagle.kestrel.protocol.Command;
import com.twitter.finagle.kestrel.protocol.Kestrel;
import com.twitter.finagle.kestrel.protocol.Response;
import com.twitter.finagle.memcached.util.ChannelBufferUtils;
import com.twitter.util.Duration;
import com.twitter.util.Time;

/**
 * A simple Kestrel client taken from
 * https://github.com/hogelog/simple-kestrel-client by Hogelog. Using this one
 * over {@link KestrelThriftClient} which seemed to have some major issues
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SimpleKestrelClient implements Closeable {
	private static final Logger LOG = Logger.getLogger(SimpleKestrelClient.class);

	private final Client client;

	/**
	 * Initialise an {@link InetSocketAddress#InetSocketAddress(String, int)}
	 * instance
	 *
	 * @param host
	 * @param port
	 */
	public SimpleKestrelClient(String host, int port) {
		this(new InetSocketAddress(host, port));
	}

	/**
	 * initialise a {@link Client} instance using {@link ServiceFactory} from a
	 * {@link ClientBuilder}
	 *
	 * @param addr
	 */
	public SimpleKestrelClient(InetSocketAddress addr) {
		final ClientBuilder<Command, Response, Yes, Yes, Yes> builder = ClientBuilder.get()
				.codec(Kestrel.get())
				.hosts(addr)
				.hostConnectionLimit(1);
		final ServiceFactory<Command, Response> kestrelClientBuilder = ClientBuilder.safeBuildFactory(builder);
		client = Client.newInstance(kestrelClientBuilder);
	}

	/**
	 * Calls {@link Client#delete(String)} on the underlying {@link Client}
	 * instance. This deletes the underlying journal instance in the kestrel
	 * queue
	 *
	 * @param queueName
	 */
	public void delete(String queueName) {
		client.delete(queueName).apply();
	}

	@Override
	public void close() {
		client.close();
	}

	/**
	 * Performs a put kestrel call on the string value with a expiary of 0 (i.e.
	 * does not expire)
	 *
	 * @param queueName
	 * @param value
	 */
	public void set(String queueName, String value) {
		set(queueName, 0, value);
	}

	/**
	 * Performs a put kestrel call with the provided expiration.
	 *
	 * @param queueName
	 * @param exp
	 * @param value
	 */
	public void set(String queueName, int exp, String value) {
		final Time expTime = Time.fromMilliseconds(exp);
		final ChannelBuffer buffer = ChannelBuffers.wrappedBuffer(value.getBytes(CharsetUtil.UTF_8));
		client.set(queueName, buffer, expTime);
	}

	/**
	 * Performs a put with the byte array value with no expiration
	 *
	 * @param queueName
	 * @param value
	 */
	public void set(String queueName, byte[] value) {
		set(queueName, 0, value);
	}

	/**
	 * Performs a put with the byte array valye with an expiration
	 *
	 * @param queueName
	 * @param exp
	 * @param value
	 */
	public void set(String queueName, int exp, byte[] value) {
		final Time expTime = Time.fromMilliseconds(exp);
		final ChannelBuffer buffer = ChannelBufferUtils.bytesToChannelBuffer(value);
		client.set(queueName, buffer, expTime);
	}

	/**
	 * Get the next value in the queue
	 *
	 * @param queueName
	 * @return the next value
	 */
	public String get(String queueName) {
		return get(queueName, 0);
	}

	/**
	 * Get the next value in the queue
	 *
	 * @param queueName
	 * @return the next value
	 */
	public byte[] getByte(String queueName) {
		return getByte(queueName, 0);
	}

	/**
	 * Get the next value in the queue
	 *
	 * @param queueName
	 * @param waitFor
	 * @return the next value
	 */
	public byte[] getByte(String queueName, int waitFor) {
		final Duration waitDuration = Duration.apply(waitFor, TimeUnit.MILLISECONDS);
		return getByte(queueName, waitDuration);
	}

	/**
	 * Get the next value in the queue
	 *
	 * @param queueName
	 * @param waitFor
	 * @return the next value
	 */
	public String get(String queueName, int waitFor) {
		final Duration waitDuration = Duration.apply(waitFor, TimeUnit.MILLISECONDS);
		return get(queueName, waitDuration);
	}

	private static final List<Class<? extends Exception>> THROUGH_EXCEPTIONS = new ArrayList<Class<? extends Exception>>();
	static {
		THROUGH_EXCEPTIONS.add(ChannelClosedException.class);
	};

	/**
	 * Get the next value from the queue
	 *
	 * @param queueName
	 * @param waitDuration
	 * @return the next value
	 */
	public String get(String queueName, Duration waitDuration) {
		try {
			final ChannelBuffer value = client.get(queueName, waitDuration).apply();
			return value == null ? null : value.toString(CharsetUtil.UTF_8);
		} catch (final Exception e) {
			if (THROUGH_EXCEPTIONS.contains(e.getClass())) {
				return null;
			}
			LOG.error(e.getMessage(), e);
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Get the next value from the queue
	 *
	 * @param queueName
	 * @param waitDuration
	 *            an amount of time to wait before returning null
	 * @return the next value
	 */
	public byte[] getByte(String queueName, Duration waitDuration) {
		try {
			final ChannelBuffer value = client.get(queueName, waitDuration).apply();
			return value == null ? null : value.array();
		} catch (final Exception e) {
			if (THROUGH_EXCEPTIONS.contains(e.getClass())) {
				return null;
			}
			LOG.error(e.getMessage(), e);
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Get the next value without popping it
	 *
	 * @param queueName
	 * @return the next value
	 */
	public String peek(String queueName) {
		return peek(queueName, 0);
	}

	/**
	 * The next value without popping it
	 *
	 * @param queueName
	 * @param waitFor
	 *            an amount of time to wait before returning null
	 * @return the next value
	 */
	public String peek(String queueName, int waitFor) {
		final Duration waitDuration = Duration.apply(waitFor, TimeUnit.MILLISECONDS);
		return peek(queueName, waitDuration);
	}

	/**
	 * The next value without popping it
	 *
	 * @param queueName
	 * @param waitDuration
	 *            an amount of time to wait before returning null
	 * @return the next value
	 */
	public String peek(String queueName, Duration waitDuration) {
		return get(queueName + "/peek", waitDuration);
	}

	/**
	 * Get the next value without popping it
	 *
	 * @param queueName
	 * @return the next value
	 */
	public byte[] peekByte(String queueName) {
		return peekByte(queueName, 0);
	}

	/**
	 * The next value without popping it
	 *
	 * @param queueName
	 * @param waitFor
	 *            an amount of time to wait before returning null
	 * @return the next value
	 */
	public byte[] peekByte(String queueName, int waitFor) {
		final Duration waitDuration = Duration.apply(waitFor, TimeUnit.MILLISECONDS);
		return peekByte(queueName, waitDuration);
	}

	/**
	 * The next value without popping it
	 *
	 * @param queueName
	 * @param waitDuration
	 *            an amount of time to wait before returning null
	 * @return the next value
	 */
	public byte[] peekByte(String queueName, Duration waitDuration) {
		return getByte(queueName + "/peek", waitDuration);
	}
}
