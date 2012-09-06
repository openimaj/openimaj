package org.openimaj.picslurper.output;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.picslurper.StatusConsumption;
import org.openimaj.twitter.collection.StreamJSONStatusList.ReadableWritableJSON;

/**
 * Can serialise itself as bytes or a json string
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WriteableImageOutput implements ReadWriteableBinary{
	private static final String IMAGE_OUTPUT_HEADER = "IMGOUT";
	/**
	 *
	 */
	public ReadableWritableJSON status;
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
	public WriteableImageOutput(ReadableWritableJSON status, URL url, File file, StatusConsumption stats) {
		this.status = status;
		this.url = url;
		this.file = file;
		this.stats = stats;
	}
	@Override
	public void readBinary(DataInput in) throws IOException {
		this.status = new ReadableWritableJSON();
		this.status.readBinary(in);
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
		status.writeBinary(out);
		out.writeUTF(url.toString());
		out.writeUTF(file.toString());
		stats.writeBinary(out);
	}


}
