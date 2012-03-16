package org.openimaj.hadoop.tools.twitter.token.outputmode.jacard;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.io.wrappers.ReadWritableIndependentPair;

final class ReadWritableStringLong extends ReadWritableIndependentPair<String, Long> {
	
	public ReadWritableStringLong(){
		super(null, null);
	}
	ReadWritableStringLong(String obj1,Long obj2) {
		super(obj1, obj2);
	}

	@Override
	public String readFirst(DataInput in) throws IOException {
		return in.readUTF();
	}

	@Override
	public Long readSecond(DataInput in) throws IOException {
		return in.readLong();
	}

	@Override
	public void writeFirst(DataOutput out,String firstObject) throws IOException {
		out.writeUTF(firstObject);
	}

	@Override
	public void writeSecond(DataOutput out,Long secondObject) throws IOException {
		out.writeLong(secondObject);
	}
}