package org.openimaj.twitter.collection;

import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;

import org.openimaj.io.Writeable;
import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.USMFStatus;

/**
 * This wrapper allows the writing of a list such that each element is converted using a conversion function
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), 
 *
 */
public class ConvertUSMFList implements Writeable{
	
	private TwitterStatusList<? extends USMFStatus> list;
	private Class<? extends GeneralJSON> convert;

	/**
	 * @param list the list being written
	 * @param convertType type to convert to
	 */
	public ConvertUSMFList(TwitterStatusList<? extends USMFStatus> list, Class<? extends GeneralJSON> convertType) {
		this.list = list;
		this.convert = convertType;
	}
	
	@Override
	public void writeASCII(PrintWriter writer) throws IOException {
		for (USMFStatus k : this.list) {
			GeneralJSON newInstance = TwitterStatusListUtils.newInstance(convert);
			newInstance.fromUSMF(k);
			newInstance.writeASCII(writer);
			writer.println();
			
		}
	}

	@Override
	public String asciiHeader() {
		return list.asciiHeader();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		list.writeBinary(out);// not supported
	}

	@Override
	public byte[] binaryHeader() {
		return list.binaryHeader();// not supported;
	}
	

}
