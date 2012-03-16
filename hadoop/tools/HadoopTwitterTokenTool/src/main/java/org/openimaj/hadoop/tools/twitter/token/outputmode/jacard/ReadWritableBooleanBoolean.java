package org.openimaj.hadoop.tools.twitter.token.outputmode.jacard;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.io.wrappers.ReadWritableIndependentPair;

final class ReadWritableBooleanBoolean extends ReadWritableIndependentPair<Boolean,Boolean> {
	
	public ReadWritableBooleanBoolean(){
		super(null, null);
	}
	ReadWritableBooleanBoolean(Boolean obj1,Boolean obj2) {
		super(obj1, obj2);
	}

	@Override
	public Boolean readFirst(DataInput in) throws IOException {
		return in.readBoolean();
	}

	@Override
	public Boolean readSecond(DataInput in) throws IOException {
		return in.readBoolean();
	}

	@Override
	public void writeFirst(DataOutput out,Boolean firstObject) throws IOException {
		out.writeBoolean(firstObject);
	}

	@Override
	public void writeSecond(DataOutput out,Boolean secondObject) throws IOException {
		out.writeBoolean(secondObject);
	}
}