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
package org.openimaj.hadoop.sequencefile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IOUtils;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.io.Text;

/**
 * A concrete implementation of a {@link SequenceFileUtility} for
 * {@link SequenceFile}s with {@link Text} keys and {@link BytesWritable}
 * values.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public final class TextBytesSequenceFileUtility extends SequenceFileUtility<Text, BytesWritable> {

	public TextBytesSequenceFileUtility(String uriOrPath, boolean read) throws IOException {
		super(uriOrPath, read);
	}

	public TextBytesSequenceFileUtility(String uriOrPath, CompressionType compressionType, Map<String, String> metadata)
			throws IOException
	{
		super(uriOrPath, compressionType, metadata);
	}

	public TextBytesSequenceFileUtility(String uriOrPath, CompressionType compressionType) throws IOException {
		super(uriOrPath, compressionType);
	}

	public TextBytesSequenceFileUtility(URI uri, boolean read) throws IOException {
		super(uri, read);
	}

	public TextBytesSequenceFileUtility(URI uri, CompressionType compressionType, Map<String, String> metadata)
			throws IOException
	{
		super(uri, compressionType, metadata);
	}

	public TextBytesSequenceFileUtility(URI uri, CompressionType compressionType) throws IOException {
		super(uri, compressionType);
	}

	@Override
	protected BytesWritable readFile(FileSystem fs, Path path) throws IOException {
		FSDataInputStream dis = null;
		ByteArrayOutputStream baos = null;

		try {
			dis = fs.open(path);
			baos = new ByteArrayOutputStream();

			IOUtils.copyBytes(dis, baos, config, false);

			final byte[] bytes = baos.toByteArray();
			return new BytesWritable(bytes);
		} finally {
			if (dis != null)
				try {
					dis.close();
				} catch (final IOException e) {
				}
			;
			if (baos != null)
				try {
					baos.close();
				} catch (final IOException e) {
				}
			;
		}
	}

	@Override
	protected void writeFile(FileSystem fs, Path path, BytesWritable value) throws IOException {
		FSDataOutputStream dos = null;

		try {
			dos = fs.create(path);
			final byte[] bytes = new byte[value.getLength()];
			System.arraycopy(value.getBytes(), 0, bytes, 0, bytes.length);
			dos.write(bytes);
		} finally {
			if (dos != null)
				try {
					dos.close();
				} catch (final IOException e) {
				}
			;
		}
	}

	@Override
	protected void printFile(BytesWritable value) throws IOException {
		System.out.write(value.getBytes());
	}

	@Override
	protected void writeZipData(ZipOutputStream zos, BytesWritable value) throws IOException {
		zos.write(value.getBytes(), 0, value.getLength());
	}
}
