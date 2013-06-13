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
package backtype.storm.spout;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.lag.kestrel.thrift.Item;
import net.lag.kestrel.thrift.Kestrel;
import net.lag.kestrel.thrift.QueueInfo;

import org.apache.thrift7.TException;
import org.apache.thrift7.protocol.TBinaryProtocol;
import org.apache.thrift7.protocol.TProtocol;
import org.apache.thrift7.transport.TFramedTransport;
import org.apache.thrift7.transport.TSocket;
import org.apache.thrift7.transport.TTransport;

/* Thin wrapper around Thrift Client for Kestrel */
public class KestrelThriftClient implements Kestrel.Iface {
	Kestrel.Client _client = null;
	TTransport _transport = null;

	public KestrelThriftClient(String hostname, int port)
			throws TException
	{

		_transport = new TFramedTransport(new TSocket(hostname, port));
		final TProtocol proto = new TBinaryProtocol(_transport);
		_client = new Kestrel.Client(proto);
		_transport.open();
	}

	public void close() {
		_transport.close();
		_transport = null;
		_client = null;
	}

	@Override
	public QueueInfo peek(String queue_name) throws TException {
		return _client.peek(queue_name);
	}

	@Override
	public void delete_queue(String queue_name) throws TException {
		_client.delete_queue(queue_name);
	}

	@Override
	public String get_version() throws TException {
		return _client.get_version();
	}

	@Override
	public int put(String queue_name, List<ByteBuffer> items, int expiration_msec) throws TException {
		return _client.put(queue_name, items, expiration_msec);
	}

	public void put(String queue_name, String item, int expiration_msec) throws TException {
		final List<ByteBuffer> toPut = new ArrayList<ByteBuffer>();
		try {
			toPut.add(ByteBuffer.wrap(item.getBytes("UTF-8")));
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		put(queue_name, toPut, expiration_msec);
	}

	@Override
	public List<Item> get(String queue_name, int max_items, int timeout_msec, int auto_abort_msec) throws TException {
		return _client.get(queue_name, max_items, timeout_msec, auto_abort_msec);
	}

	@Override
	public int confirm(String queue_name, Set<Long> ids) throws TException {
		return _client.confirm(queue_name, ids);
	}

	@Override
	public int abort(String queue_name, Set<Long> ids) throws TException {
		return _client.abort(queue_name, ids);
	}

	@Override
	public void flush_queue(String queue_name) throws TException {
		_client.flush_queue(queue_name);
	}

	@Override
	public void flush_all_queues() throws TException {
		_client.flush_all_queues();
	}

}
