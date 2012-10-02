package org.openimaj.picslurper.client;


import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;

import org.openimaj.io.IOUtils;
import org.openimaj.picslurper.output.WriteableImageOutput;
import org.zeromq.ZMQ.Socket;


/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ZMQStreamingPicslurperClusterer implements Runnable {
	private Socket subscriber;
	private TrendDetector detector;

	private static class CleanupRunner implements Runnable {

		@Override
		public void run() {

		}

	}

	private static class TrendingRunner implements Runnable {


		private TrendDetector trendDet;

		public TrendingRunner(TrendDetector instance) {
			trendDet = instance;
		}

		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
				}
				List<Set<WriteableImageOutput>> trending = trendDet.trending(10);
				for (Set<WriteableImageOutput> set : trending) {
					System.out.println(String.format("[%d] %s", set.size(),set.toString()));
				}
			}
		}

	}

	public ZMQStreamingPicslurperClusterer(TrendDetector instance) {
		detector = instance;
	}

	/**
	 * @param args
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String args[]) throws UnsupportedEncodingException {
		TrendDetector instance = new TrendDetector();
		new Thread(new CleanupRunner()).start();
		new Thread(new TrendingRunner(instance)).start();
		new Thread(new ZMQStreamingPicslurperClusterer(instance)).start();
	}

	@Override
	public void run() {
		while (true) {
			subscriber.recv(0);
			ByteArrayInputStream stream = new ByteArrayInputStream(
					subscriber.recv(0));
			WriteableImageOutput instance = null;
			try {
				instance = IOUtils.read(stream, WriteableImageOutput.class,"UTF-8");
				detector.indexImage(instance);

				System.out.println("SUCCESS!");
			} catch (Throwable e) {
				System.err.println("FAILED: ");
				if(instance != null){
					System.err.println("instance.file = " + instance.file);
					System.err.println("instance.url = " + instance.url);

				}
				e.printStackTrace();
			}
		}
	}
}
