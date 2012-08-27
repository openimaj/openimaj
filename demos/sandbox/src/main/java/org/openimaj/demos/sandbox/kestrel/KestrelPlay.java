package org.openimaj.demos.sandbox.kestrel;

import java.util.List;

import net.lag.kestrel.thrift.Item;

import org.apache.hadoop.thirdparty.guava.common.collect.Sets;
import org.apache.thrift7.TException;
import org.openimaj.kestrel.SimpleKestrelClient;

import backtype.storm.spout.KestrelThriftClient;

public class KestrelPlay {
	public static void main(String[] args) throws TException {
//		client.set("sina",100, "Cheese!");
//		System.out.println(client.get("sina"));
//		client.delete("sina");
//		client.close();
//		producerConsumer(500,100);
//		producerConsumer(100,500);
		producerUnreliableConsumer(500,500);
	}

	private static void producerUnreliableConsumer(final long produceRate, final long consumeRate) throws TException {
		KestrelThriftClient client = new KestrelThriftClient("127.0.0.1", 2229);
		client.delete_queue("sina");
		client.close();
		new Thread(new Runnable() {
			@Override
			public void run() {
				KestrelThriftClient client = null;
				try{
					client = new KestrelThriftClient("127.0.0.1", 2229);
					int i = 0;
					while(true){
						client.put("sina", "Cheese " + i ++,0);
						try {
							Thread.sleep(produceRate);
						} catch (InterruptedException e) {
						}
					}
				} catch (TException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				finally{
					client.close();
				}
			}
		}).start();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				KestrelThriftClient client = null;
				try{
					int i = 0;
					client = new KestrelThriftClient("127.0.0.1", 2229);
					while(true){
						Item item = client.get("sina", 1, 1000, 1000).get(0);
						String itemStr = new String(item.get_data());
						if(i++ % 2 == 0){
							System.out.println("Read successfully: " + itemStr);
							client.confirm("sina", Sets.newHashSet(item.get_id()));
						}
						else{
							System.err.println("Failed to read: " + itemStr);
						}
						try {
							Thread.sleep(consumeRate);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				finally{
					client.close();
				}
				
			}
		}).start();
	}

	private static void producerConsumer(final long produceRate, final long consumeRate) {
		SimpleKestrelClient client = new SimpleKestrelClient("127.0.0.1", 22133);
		client.delete("sina");
		client.close();
		new Thread(new Runnable() {
			@Override
			public void run() {
				SimpleKestrelClient client = null;
				try{
					client = new SimpleKestrelClient("127.0.0.1", 22133);
					int i = 0;
					while(true){
						client.set("sina", "Cheese " + i ++);
						try {
							Thread.sleep(produceRate);
						} catch (InterruptedException e) {
						}
					}
				}
				finally{
					client.close();
				}
			}
		}).start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				SimpleKestrelClient client = null;
				try{
					client = new SimpleKestrelClient("127.0.0.1", 22133);
					while(true){
						System.out.println(client.get("sina"));
						try {
							Thread.sleep(consumeRate);
						} catch (InterruptedException e) {
						}
					}
				}
				finally{
					client.close();
				}
				
			}
		}).start();
	}
}
