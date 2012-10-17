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
package org.openimaj.rdf.storm.topology;

import java.util.List;

import net.lag.kestrel.thrift.Item;

import org.apache.thrift7.TException;
import org.openimaj.kestrel.KestrelServerSpec;
import org.openimaj.kestrel.writing.NTripleWritingScheme;

import backtype.storm.spout.KestrelThriftClient;

import com.google.common.collect.Sets;

/**
 * Print everything from a kestrel queue forever
 * 
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class KestrelQueuePrinter implements Runnable {

	private KestrelServerSpec spec;
	private String queue;

	/**
	 * @param spec
	 *            the kestrel server to connect to
	 * @param queue
	 *            the queue to read from
	 */
	public KestrelQueuePrinter(KestrelServerSpec spec, String queue) {
		this.spec = spec;
		this.queue = queue;
	}

	@Override
	public void run() {
		KestrelThriftClient client = null;
		NTripleWritingScheme scheme = null;
		try {
			client = new KestrelThriftClient(spec.host, spec.port);
			scheme = new NTripleWritingScheme();
		} catch (TException e) {
		}
		while (true) {
			List<Item> itemList;
			try {
				itemList = client.get(queue, 1, 100, 100);
			} catch (TException e) {
				e.printStackTrace();
				break;
			}
			for (Item item : itemList) {
				try {
					client.confirm(queue, Sets.newHashSet(item.get_id()));
				} catch (TException e) {
					System.out.println("Could not confirm! " + e.getMessage());
					break;
				}
				if (item != null) {
					List<Object> thing = scheme.deserialize(item.get_data());
					for (Object object : thing) {
						System.out.println(object);
					}
				}
			}
		}
	}
}
