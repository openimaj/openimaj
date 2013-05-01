package org.openimaj.picslurper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.openimaj.io.ReadWriteable;

/**
 * Statistics about status consumption
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class StatusConsumption implements ReadWriteable {

	/**
	 * The consume image urls
	 */
	public List<URL> imageURLs = new ArrayList<URL>();

	/**
	 * number of urls consumed
	 */
	public int nURLs;
	/**
	 * number of images consumed
	 */
	public int nImages;
	/**
	 * number of tweets consumed
	 */
	public int nTweets;

	@Override
	public void readASCII(Scanner in) throws IOException {
		in.next();
		nURLs = in.nextInt();
		in.next();
		nImages = in.nextInt();
		// Now read how many image urls were saved (on the same line)
		final int savedURLs = in.nextInt();
		in.next();
		nTweets = in.nextInt();
		in.nextLine(); // complete the line
		this.imageURLs = new ArrayList<URL>();
		for (int i = 0; i < savedURLs; i++) {
			this.imageURLs.add(new URL(in.nextLine()));
		}
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.printf("nURLS: %d\n", nURLs);
		out.printf("nImages: %d %d\n", nImages, this.imageURLs.size());
		out.printf("nTweets: %d\n", nTweets);
		for (final URL url : this.imageURLs) {
			out.println(url);
		}
	}

	/**
	 * @param other
	 *            add two {@link StatusConsumption} instances
	 */
	public void incr(StatusConsumption other) {
		this.nImages += other.nImages;
		this.nURLs += other.nURLs;
		this.nTweets += other.nTweets;
		this.imageURLs.addAll(other.imageURLs);
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		this.nURLs = in.readInt();
		this.nImages = in.readInt();
		final int savedURLs = in.readInt();
		this.nTweets = in.readInt();
		for (int i = 0; i < savedURLs; i++) {
			this.imageURLs.add(new URL(in.readUTF()));
		}
	}

	@Override
	public byte[] binaryHeader() {
		return "BSTAT".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(nURLs);
		out.writeInt(nImages);
		out.writeInt(this.imageURLs.size());
		out.writeInt(nTweets);
		for (final URL url : this.imageURLs) {
			out.writeUTF(url.toString());
		}
	}
}
