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
import java.io.PrintWriter;
import java.util.Scanner;

import org.openimaj.io.ReadWriteable;

/**
 * A wrapper around a String that can be read and written.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReadWriteableString implements ReadWriteable {
	/**
	 * The underlying value
	 */
	public String value;
	
	/**
	 * Construct a ReadWriteableString with the given value
	 * @param value the value
	 */
	public ReadWriteableString(String value) {
		this.value = value;
	}
	
	/**
	 * Construct a ReadWriteableString with an empty string
	 */
	public ReadWriteableString() {
		this.value = "";
	}

	/* (non-Javadoc)
	 * @see org.openimaj.utils.Readable#readBinary(java.io.DataInput)
	 */
	@Override
	public void readBinary(DataInput in) throws IOException {
		value = in.readUTF();
	}

	/* (non-Javadoc)
	 * @see org.openimaj.utils.Readable#readASCII(java.util.Scanner)
	 */
	@Override
	public void readASCII(Scanner in) throws IOException {
		int len = in.nextInt();
		value = in.next(".{"+len+"}");
	}

	/* (non-Javadoc)
	 * @see org.openimaj.utils.Readable#binaryHeader()
	 */
	@Override
	public byte[] binaryHeader() {
		return new byte[0];
	}

	/* (non-Javadoc)
	 * @see org.openimaj.utils.Readable#asciiHeader()
	 */
	@Override
	public String asciiHeader() {
		return "";
	}

	/* (non-Javadoc)
	 * @see org.openimaj.utils.Writeable#writeBinary(java.io.DataOutput)
	 */
	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(value);
	}

	/* (non-Javadoc)
	 * @see org.openimaj.utils.Writeable#writeASCII(java.io.PrintWriter)
	 */
	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.format("%d %s\n", value.length(), value);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o){
		return o instanceof ReadWriteableString && ((ReadWriteableString)o).value.equals(this.value);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode(){
		return this.value.hashCode();
	}
}
