package org.openimaj.demos.sandbox.kestrel;

public class KestrelPlay {
	public static void main(String[] args) {
//		client.set("sina",100, "Cheese!");
//		System.out.println(client.get("sina"));
//		client.delete("sina");
//		client.close();
		new Thread(new Runnable() {
			@Override
			public void run() {
				SimpleKestrelClient client = new SimpleKestrelClient("127.0.0.1", 22133);
				int i = 0;
				while(true){
					client.set("sina", "Cheese " + i ++);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				SimpleKestrelClient client = new SimpleKestrelClient("127.0.0.1", 22133);
				while(true){
					System.out.println(client.get("sina"));
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}
}
