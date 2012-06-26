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
package org.openimaj.io;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;



/**
 * This helper class is used to wrap ReadWriteable objects
 * and write them with the information needed to instantiate
 * them without knowing the class.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 */
class ObjectWrapper implements ReadWriteable {
	Object object; 
	
	public ObjectWrapper() {
	}
	
	public ObjectWrapper(Object o) {
		this.object = o;
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		String className = in.next();
		object = IOUtils.newInstance(className);
		((ReadableASCII)object).readASCII(in);
	}

	@Override
	public String asciiHeader() {
		return "Class: ";
	}

	@Override
	public void readBinary(DataInput in) throws IOException {
		String className = in.readUTF();
		object = IOUtils.newInstance(className);
		((ReadableBinary)object).readBinary(in);
	}

	@Override
	public byte[] binaryHeader() {
		return "CLS:".getBytes();
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		out.println(object.getClass().getName());
		((WriteableASCII) object).writeASCII(out);
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(object.getClass().getName());
		((WriteableBinary) object).writeBinary(out);
	}
}
