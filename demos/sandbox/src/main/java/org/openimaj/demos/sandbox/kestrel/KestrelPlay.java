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
package org.openimaj.demos.sandbox.kestrel;

import net.lag.kestrel.thrift.Item;

import org.apache.thrift7.TException;
import org.openimaj.kestrel.SimpleKestrelClient;

import backtype.storm.spout.KestrelThriftClient;

import com.google.common.collect.Sets;

public class KestrelPlay {
	public static void main(String[] args) throws TException {
		// client.set("sina",100, "Cheese!");
		// System.out.println(client.get("sina"));
		// client.delete("sina");
		// client.close();
		// producerConsumer(500,100);
		// producerConsumer(100,500);
		producerUnreliableConsumer(500, 500);
	}

	private static void producerUnreliableConsumer(final long produceRate, final long consumeRate) throws TException {
		final KestrelThriftClient client = new KestrelThriftClient("127.0.0.1", 2229);
		client.delete_queue("sina");
		client.close();
		new Thread(new Runnable() {
			@Override
			public void run() {
				KestrelThriftClient client = null;
				try {
					client = new KestrelThriftClient("127.0.0.1", 2229);
					int i = 0;
					while (true) {
						client.put("sina", "Cheese " + i++, 0);
						try {
							Thread.sleep(produceRate);
						} catch (final InterruptedException e) {
						}
					}
				} catch (final TException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				finally {
					client.close();
				}
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				KestrelThriftClient client = null;
				try {
					int i = 0;
					client = new KestrelThriftClient("127.0.0.1", 2229);
					while (true) {
						final Item item = client.get("sina", 1, 1000, 1000).get(0);
						final String itemStr = new String(item.get_data());
						if (i++ % 2 == 0) {
							System.out.println("Read successfully: " + itemStr);
							client.confirm("sina", Sets.newHashSet(item.get_id()));
						}
						else {
							System.err.println("Failed to read: " + itemStr);
						}
						try {
							Thread.sleep(consumeRate);
						} catch (final InterruptedException e) {
							e.printStackTrace();
						}
					}
				} catch (final Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				finally {
					client.close();
				}

			}
		}).start();
	}

	private static void producerConsumer(final long produceRate, final long consumeRate) {
		final SimpleKestrelClient client = new SimpleKestrelClient("127.0.0.1", 22133);
		client.delete("sina");
		client.close();
		new Thread(new Runnable() {
			@Override
			public void run() {
				SimpleKestrelClient client = null;
				try {
					client = new SimpleKestrelClient("127.0.0.1", 22133);
					int i = 0;
					while (true) {
						client.set("sina", "Cheese " + i++);
						try {
							Thread.sleep(produceRate);
						} catch (final InterruptedException e) {
						}
					}
				}
				finally {
					client.close();
				}
			}
		}).start();

		new Thread(new Runnable() {
			@Override
			public void run() {
				SimpleKestrelClient client = null;
				try {
					client = new SimpleKestrelClient("127.0.0.1", 22133);
					while (true) {
						System.out.println(client.get("sina"));
						try {
							Thread.sleep(consumeRate);
						} catch (final InterruptedException e) {
						}
					}
				}
				finally {
					client.close();
				}

			}
		}).start();
	}
}
