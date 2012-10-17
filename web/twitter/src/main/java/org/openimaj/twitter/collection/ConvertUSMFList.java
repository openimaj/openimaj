/**
 * Copyright (c) 2012, The University of Southampton and the individual contributors.
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
package org.openimaj.twitter.collection;

import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;

import org.openimaj.io.Writeable;
import org.openimaj.twitter.GeneralJSON;
import org.openimaj.twitter.USMFStatus;

/**
 * This wrapper allows the writing of a list such that each element is converted using a conversion function
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk), 
 *
 */
public class ConvertUSMFList implements Writeable{
	
	private TwitterStatusList<? extends USMFStatus> list;
	private Class<? extends GeneralJSON> convert;

	/**
	 * @param list the list being written
	 * @param convertType type to convert to
	 */
	public ConvertUSMFList(TwitterStatusList<? extends USMFStatus> list, Class<? extends GeneralJSON> convertType) {
		this.list = list;
		this.convert = convertType;
	}
	
	@Override
	public void writeASCII(PrintWriter writer) throws IOException {
		for (USMFStatus k : this.list) {
			GeneralJSON newInstance = TwitterStatusListUtils.newInstance(convert);
			newInstance.fromUSMF(k);
			newInstance.writeASCII(writer);
			writer.println();
			
		}
	}

	@Override
	public String asciiHeader() {
		return list.asciiHeader();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		list.writeBinary(out);// not supported
	}

	@Override
	public byte[] binaryHeader() {
		return list.binaryHeader();// not supported;
	}
	

}
