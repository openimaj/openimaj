package org.openimaj.hadoop.tools.twitter.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.util.pair.IndependentPair;

class WriteableStringIntPair extends IndependentPair<String, Integer> implements ReadWriteableBinary{
	public WriteableStringIntPair(){
		super(null,null);
	}
	public WriteableStringIntPair(String obj1, Integer obj2) {
		super(obj1, obj2);
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		this.setFirstObject(in.readUTF());
		this.setSecondObject(in.readInt());
	}

	@Override
	public byte[] binaryHeader() {
		return "".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(this.firstObject());
		out.writeInt(this.secondObject());
	}
}