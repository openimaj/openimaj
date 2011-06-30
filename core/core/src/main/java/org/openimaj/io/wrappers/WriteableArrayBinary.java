package org.openimaj.io.wrappers;

import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.io.WriteableBinary;

public abstract class WriteableArrayBinary<V> implements WriteableBinary {
	public V[] value;
	
	public WriteableArrayBinary(V[] list) {
		this.value = list;
	}
	
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		if (value == null) {
			out.writeInt(-1);
		} else {
			out.writeInt(value.length);
			
			for (V v : value) {
				writeValue(v, out);
			}			
		}
	}

	protected abstract void writeValue(V v, DataOutput out) throws IOException;

	@Override
	public byte[] binaryHeader() {
		return value.getClass().getName().getBytes();
	}
}
