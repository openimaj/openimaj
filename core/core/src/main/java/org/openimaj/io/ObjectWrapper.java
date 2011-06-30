package org.openimaj.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;


/**
 * This helper class is used to wrap ReadWriteable objects
 * and write them with the information needed to instantiate
 * them without knowing the class.
 * 
 * @author Jonathon Hare <jsh2@ecs.soton.ac.uk>
 *
 */
class ObjectWrapper implements ReadWriteable {
	Object object; 
	
	public ObjectWrapper() {
	}
	
	public ObjectWrapper(Object o) {
		this.object = o;
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		String className = in.next();
		object = IOUtils.newInstance(className);
		((ReadableASCII)object).readASCII(in);
	}

	@Override
	public String asciiHeader() {
		return "Class: ";
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		String className = in.readUTF();
		object = IOUtils.newInstance(className);
		((ReadableBinary)object).readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return "CLS:".getBytes();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println(object.getClass().getName());
		((WriteableASCII) object).writeASCII(out);
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(object.getClass().getName());
		((WriteableBinary) object).writeBinary(out);
	}
}
