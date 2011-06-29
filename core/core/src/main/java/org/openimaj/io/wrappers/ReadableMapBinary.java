package org.openimaj.io.wrappers;

import java.io.DataInput;
import java.io.IOException;
import java.util.Map;

import org.openimaj.io.ReadableBinary;

public abstract class ReadableMapBinary<K, V> implements ReadableBinary {
	public Map<K, V> value;
	
	public ReadableMapBinary(Map<K,V> map) {
		this.value = map;
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		int sz = in.readInt();
		
		for (int i=0; i<sz; i++) {
			K key = readKey(in);
			V val = readValue(in);
			value.put(key, val);
		}
	}

	protected abstract K readKey(DataInput in) throws IOException;

	protected abstract V readValue(DataInput in) throws IOException;

	@Override
	public byte[] binaryHeader() {
		return value.getClass().getName().getBytes();
	}
}
