package org.openimaj.picslurper.output;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Scanner;

import org.openimaj.io.ReadWriteable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Can serialise itself as bytes or a json string
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WriteableFailedURL implements ReadWriteable{
	private transient Gson gson = new GsonBuilder().create();
	private static final String IMAGE_OUTPUT_HEADER = "FAILOUTB";
	private static final String IMAGE_OUTPUT_HEADER_ASCII = "FAILOUTA";
	private static final String NEWLINE_STR = "NEWLINE";
	/**
	 *
	 */
	public URL url;
	/**
	 *
	 */
	public String reason;
	/**
	 *
	 */
	public WriteableFailedURL() {
		// all remain null
	}
	/**
	 * @param url
	 * @param reason
	 */
	public WriteableFailedURL(URL url, String reason) {
		this.url = url;
		this.reason = reason;
	}
	@Override
	public void readBinary(DataInput in) throws IOException {

		this.url = new URL(in.readUTF());
		this.reason = in.readUTF();


	}
	@Override
	public byte[] binaryHeader() {
		return IMAGE_OUTPUT_HEADER.getBytes();
	}
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(url.toString());
		out.writeUTF(this.reason);
	}
	@Override
	public void readASCII(Scanner in) throws IOException {
		this.url = new URL(in.nextLine());
		this.reason = in.nextLine().replace(NEWLINE_STR, "\n");
	}
	@Override
	public String asciiHeader() {
		return IMAGE_OUTPUT_HEADER_ASCII;
	}
	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println(url.toString());
		out.println(this.reason.replace("\n", NEWLINE_STR));
	}


}
