package org.openimaj.io.wrappers;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.io.WriteableBinary;

public abstract class WriteableMapBinary<K, V> implements WriteableBinary {
	public Map<K, V> value;
	
	public WriteableMapBinary(Map<K,V> map) {
		this.value = map;
	}
	
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeInt(value.size());
		
		for (Entry<K,V> entry : value.entrySet()) {
			writeKey(entry.getKey(), out);
			writeValue(entry.getValue(), out);
		}
	}

	protected abstract void writeKey(K key, DataOutput out) throws IOException;

	protected abstract void writeValue(V value2, DataOutput out) throws IOException;

	@Override
	public byte[] binaryHeader() {
		return value.getClass().getName().getBytes();
	}

}
