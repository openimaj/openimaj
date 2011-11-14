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
package org.openimaj.hadoop.tools.fastkmeans;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;

public class TextBytesSequenceMemoryUtility extends SequenceFileUtility<Text, BytesWritable>{

	public TextBytesSequenceMemoryUtility(String uriOrPath, boolean read)
			throws IOException {
		super(uriOrPath, read);
		// TODO Auto-generated constructor stub
	}

	public TextBytesSequenceMemoryUtility(String uriOrPath,CompressionType compressionType, Map<String, String> metadata) throws IOException {
		super(uriOrPath, compressionType, metadata);
	}

	public TextBytesSequenceMemoryUtility(String uriOrPath, CompressionType compressionType) throws IOException {
		super(uriOrPath, compressionType);
	}

	public TextBytesSequenceMemoryUtility(URI uri, boolean read) throws IOException {
		super(uri, read);
	}

	public TextBytesSequenceMemoryUtility(URI uri, CompressionType compressionType, Map<String, String> metadata) throws IOException {
		super(uri, compressionType, metadata);
	}

	public TextBytesSequenceMemoryUtility(URI uri, CompressionType compressionType) throws IOException {
		super(uri, compressionType);
	}

	@Override
	protected BytesWritable readFile(FileSystem fs, Path path) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void writeFile(FileSystem fs, Path path, BytesWritable value) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void printFile(BytesWritable value) throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeZipData(ZipOutputStream zos, BytesWritable value) throws IOException {
		throw new UnsupportedOperationException();
	}

}
