package org.openimaj.io.wrappers;

import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

import org.openimaj.io.WriteableBinary;

public abstract class WriteableListBinary<V> implements WriteableBinary {
	public List<V> value;
	
	public WriteableListBinary(List<V> list) {
		this.value = list;
	}
	
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(value.size());
		
		for (V v : value) {
			writeValue(v, out);
		}
	}

	protected abstract void writeValue(V v, DataOutput out) throws IOException;

	@Override
	public byte[] binaryHeader() {
		return value.getClass().getName().getBytes();
	}
}
