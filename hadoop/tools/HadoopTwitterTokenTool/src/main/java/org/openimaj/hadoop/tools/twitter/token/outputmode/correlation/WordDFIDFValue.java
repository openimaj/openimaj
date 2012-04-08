package org.openimaj.hadoop.tools.twitter.token.outputmode.correlation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;


/**
 * @author ss
 *
 */
public class WordDFIDFValue extends WordDFIDF {
	
	public double value;
	public String label;
	
	public WordDFIDFValue(WordDFIDF idf, String label, double d) {
		this.label = label;
		this.value = d;
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		super.writeBinary(out);
		out.writeUTF(label);
		out.writeDouble(value);
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		super.readBinary(in);
		this.label = in.readUTF();
		this.value = in.readDouble();
	}
	
	@Override
	public int compareTo(WordDFIDF other) {
		int supertrue = super.compareTo(other);
		return supertrue;
	}
}
