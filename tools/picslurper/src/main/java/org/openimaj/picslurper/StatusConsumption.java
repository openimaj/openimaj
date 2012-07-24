package org.openimaj.picslurper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.io.ReadWriteableASCII;

class StatusConsumption implements ReadWriteableASCII{
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
	public void incr(StatusConsumption other){
		this.nImages += other.nImages;
		this.nURLs += other.nURLs;
		this.nTweets += other.nTweets;
	}
}