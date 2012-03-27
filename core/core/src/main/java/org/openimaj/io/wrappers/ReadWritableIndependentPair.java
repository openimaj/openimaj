package org.openimaj.io.wrappers;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.util.pair.IndependentPair;

/**
 * Base class for writing any independent pair.
 * 
 * @author Sina Samangooei <ss@ecs.soton.ac.uk>
 *
 * @param <A> Type of first object
 * @param <B> Type of second object
 */
public abstract class ReadWritableIndependentPair<A, B> extends IndependentPair<A, B> implements ReadWriteableBinary {
	/**
	 * initialise with data
	 * @param obj1
	 * @param obj2
	 */
	public ReadWritableIndependentPair(A obj1, B obj2) {
		super(obj1, obj2);
	}
	
	/**
	 * initialise with an existing pair
	 * @param pair
	 */
	public ReadWritableIndependentPair(IndependentPair<A,B> pair) {
		super(pair.firstObject(), pair.secondObject());
	}
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		this.setFirstObject(readFirst(in));
		this.setSecondObject(readSecond(in));
	}
	
	/**
	 * @param in
	 * @return first object read from in
	 * @throws IOException 
	 */
	public abstract A readFirst(DataInput in) throws IOException;
	
	/**
	 * @param in
	 * @return second object read from in
	 * @throws IOException 
	 */
	public abstract B readSecond(DataInput in) throws IOException;
	
	/**
	 * first object written to out
	 * @param out 
	 * @param firstObject 
	 * @throws IOException 
	 */
	public abstract void writeFirst(DataOutput out, A firstObject) throws IOException;
	
	/**
	 * second object written to out
	 * @param out 
	 * @param secondObject 
	 * @throws IOException 
	 */
	public abstract void writeSecond(DataOutput out, B secondObject) throws IOException;
	
	@Override
	public byte[] binaryHeader() {
		return "RWIP".getBytes();
	}
	
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		writeFirst(out, this.firstObject());
		writeSecond(out, this.secondObject());
	}
}
