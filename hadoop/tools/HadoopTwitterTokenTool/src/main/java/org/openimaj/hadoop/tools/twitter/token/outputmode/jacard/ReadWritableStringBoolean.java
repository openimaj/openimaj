package org.openimaj.hadoop.tools.twitter.token.outputmode.jacard;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.io.wrappers.ReadWritableIndependentPair;

final class ReadWritableStringBoolean extends ReadWritableIndependentPair<String, Boolean> {

	ReadWritableStringBoolean(CumulativeTimeWord.Map map, String obj1,Boolean obj2) {
		super(obj1, obj2);
	}

	@Override
	public String readFirst(DataInput in) throws IOException {
		return in.readUTF();
	}

	@Override
	public Boolean readSecond(DataInput in) throws IOException {
		return in.readBoolean();
	}

	@Override
	public void writeFirst(DataOutput out,String firstObject) throws IOException {
		out.writeUTF(firstObject);
	}

	@Override
	public void writeSecond(DataOutput out,Boolean secondObject) throws IOException {
		out.writeBoolean(secondObject);
	}
}