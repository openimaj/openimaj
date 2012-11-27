package org.openimaj.picslurper.output;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.openimaj.io.ReadWriteable;
import org.openimaj.picslurper.StatusConsumption;

import twitter4j.Status;
import twitter4j.internal.json.z_T4JInternalJSONImplFactory;
import twitter4j.internal.org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Can serialise itself as bytes or a json string
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class WriteableImageOutput implements ReadWriteable, Cloneable {
	private transient Gson gson = new GsonBuilder().create();
	private static final String IMAGE_OUTPUT_HEADER = "IMGOUTB";
	private static final String IMAGE_OUTPUT_HEADER_ASCII = "IMGOUTA";
	/**
	 *
	 */
	public Status status;
	/**
	 *
	 */
	public URL url;
	/**
	 *
	 */
	public File file;
	/**
	 *
	 */
	public StatusConsumption stats;

	/**
	 *
	 */
	public WriteableImageOutput() {
		// all remain null
	}

	/**
	 * @param status
	 * @param url
	 * @param file
	 * @param stats
	 */
	public WriteableImageOutput(Status status, URL url, File file, StatusConsumption stats) {
		this.status = status;
		this.url = url;
		this.file = file;
		this.stats = stats;
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		final String statusJson = in.readUTF();
		try {
			this.status = new z_T4JInternalJSONImplFactory(null).createStatus(new JSONObject(statusJson));
		} catch (final Exception e) {
			throw new IOException(e);
		}
		this.url = new URL(in.readUTF());
		this.file = new File(in.readUTF());
		this.stats = new StatusConsumption();
		this.stats.readBinary(in);

	}

	@Override
	public byte[] binaryHeader() {
		return IMAGE_OUTPUT_HEADER.getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(gson.toJson(this.status));
		out.writeUTF(url.toString());
		out.writeUTF(file.toString());
		stats.writeBinary(out);
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		final String statusJson = in.nextLine();
		try {
			this.status = new z_T4JInternalJSONImplFactory(null).createStatus(new JSONObject(statusJson));
		} catch (final Exception e) {
			throw new IOException(e);
		}
		this.url = new URL(in.nextLine());
		this.file = new File(in.nextLine());
		this.stats = new StatusConsumption();
		this.stats.readASCII(in);
	}

	@Override
	public String asciiHeader() {
		return IMAGE_OUTPUT_HEADER_ASCII;
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println(gson.toJson(this.status));
		out.println(url.toString());
		out.println(file.toString());
		stats.writeASCII(out);
	}

	/**
	 * @return all the images in this ImageOutput's file
	 */
	public List<File> listImageFiles() {
		final File[] files = this.file.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.matches("image_.*[.](png|gif)");
			}
		});
		return Arrays.asList(files);
	}

	/**
	 * @return all the images in this ImageOutput's file
	 */
	public List<File> listImageFiles(String root) {
		final File[] files = new File(root, this.file.toString()).listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return name.matches("image_.*[.](png|gif)");
			}
		});
		return Arrays.asList(files);
	}

	@Override
	public String toString() {
		return this.url.toString();
	}

	@Override
	public WriteableImageOutput clone() throws CloneNotSupportedException {
		return new WriteableImageOutput(status, url, new File(file.getAbsolutePath()), stats);
	}
}
