/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.io.wrappers;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.io.ReadWriteableBinary;
import org.openimaj.util.pair.IndependentPair;

/**
 * Base class for writing any independent pair.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
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
