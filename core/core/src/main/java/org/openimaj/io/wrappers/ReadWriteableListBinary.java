package org.openimaj.io.wrappers;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;
import org.openimaj.io.ReadableBinary;
import org.openimaj.io.WriteableBinary;

public abstract class ReadWriteableListBinary<V> implements ReadableBinary, WriteableBinary{
	
	public List<V> value;
	
	public ReadWriteableListBinary(List<V> list) {
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
