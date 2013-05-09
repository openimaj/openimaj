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

import java.io.InputStream;

/**
 * Interface for classes capable of reading objects from a {@link InputStream}.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <T>
 *            Type of object being read.
 */
public interface InputStreamObjectReader<T> extends ObjectReader<T, InputStream> {
	/**
	 * Returns true if the stream can be read, or false otherwise.
	 * <p>
	 * Typically implementations will read just the first few bytes from the
	 * stream to determine if the data can be read. This method is not normally
	 * called directly; rather,
	 * {@link IOUtils#canRead(InputStreamObjectReader, java.io.BufferedInputStream, String)}
	 * should be used instead as it is capable of resetting the stream to its
	 * initial condition.
	 * 
	 * @see IOUtils#canRead(InputStreamObjectReader,
	 *      java.io.BufferedInputStream, String)
	 * 
	 * @param stream
	 *            the input stream
	 * @param name
	 *            the name of the file behind the stream (can be null).
	 * @return true if this {@link InputStreamObjectReader} can read the stream;
	 *         false otherwise.
	 */
	@Override
	public boolean canRead(InputStream stream, String name);
}
