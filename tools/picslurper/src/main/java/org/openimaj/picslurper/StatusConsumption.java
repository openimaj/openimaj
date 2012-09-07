package org.openimaj.picslurper;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.io.ReadWriteable;

/**
 * Statistics about status consumption
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class StatusConsumption implements ReadWriteable{
	int nURLs;
	int nImages;
	int nTweets;
	@Override
	public void readASCII(Scanner in) throws IOException {
		in.next();
		nURLs = in.nextInt();
		in.next();
		nImages = in.nextInt();
		in.next();
		nTweets = in.nextInt();
	}
	@Override
	public String asciiHeader() {
		return "";
	}
	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.printf("nURLS: %d\n",nURLs);
		out.printf("nImages: %d\n",nImages);
		out.printf("nTweets: %d\n",nTweets);
	}
	/**
	 * @param other add two {@link StatusConsumption} instances
	 */
	public void incr(StatusConsumption other){
		this.nImages += other.nImages;
		this.nURLs += other.nURLs;
		this.nTweets += other.nTweets;
	}
	@Override
	public void readBinary(DataInput in) throws IOException {
		this.nURLs = in.readInt();
		this.nImages = in.readInt();
		this.nTweets = in.readInt();
	}
	@Override
	public byte[] binaryHeader() {
		return "BSTAT".getBytes();
	}
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(nURLs);
		out.writeInt(nImages);
		out.writeInt(nTweets);
	}
}