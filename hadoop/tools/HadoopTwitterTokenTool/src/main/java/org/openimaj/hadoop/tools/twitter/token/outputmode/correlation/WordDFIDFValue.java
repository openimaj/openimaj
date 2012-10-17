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
package org.openimaj.hadoop.tools.twitter.token.outputmode.correlation;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;


/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
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
