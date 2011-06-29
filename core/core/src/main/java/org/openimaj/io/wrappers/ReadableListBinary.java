package org.openimaj.io.wrappers;

import java.io.DataInput;
import java.io.IOException;
import java.util.List;

import org.openimaj.io.ReadableBinary;

public abstract class ReadableListBinary<V> implements ReadableBinary {
	public List<V> value;
	
	public ReadableListBinary(List<V> list) {
		this.value = list;
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		int sz = in.readInt();
		
		for (int i=0; i<sz; i++) {
			value.add( readValue(in) );
		}
	}

	protected abstract V readValue(DataInput in) throws IOException;

	@Override
	public byte[] binaryHeader() {
		return value.getClass().getName().getBytes();
	}
}
