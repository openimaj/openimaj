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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Scanner;

import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Methods for reading Readable objects and writing Writeable objects.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class IOUtils {
	/**
	 * Create a new instance of the given class. The class must have a no-args
	 * constructor. The constructor doesn't have to be public.
	 * 
	 * @param <T>
	 *            The type of object.
	 * @param cls
	 *            The class.
	 * @return a new instance.
	 */
	public static <T extends InternalReadable> T newInstance(Class<T> cls) {
		try {
			return cls.newInstance();
		} catch (final Exception e) {
			try {
				final Constructor<T> constr = cls.getDeclaredConstructor();

				if (constr != null) {
					constr.setAccessible(true);
					return constr.newInstance();
				}
			} catch (final Exception e1) {
				throw new RuntimeException(e);
			}

			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a new instance of the given class. The class must have a no-args
	 * constructor. The constructor doesn't have to be public.
	 * 
	 * @param <T>
	 *            The type of object.
	 * @param className
	 *            The class name.
	 * @return a new instance.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends InternalReadable> T newInstance(String className) {
		try {
			return newInstance(((Class<T>) Class.forName(className)));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Read an object from a file.
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param f
	 *            the file
	 * @return object read from file
	 * @throws IOException
	 *             problem reading file
	 */
	@SuppressWarnings("unchecked")
	public static <T extends InternalReadable> T read(File f) throws IOException {
		final ObjectWrapper ow = IOUtils.read(f, ObjectWrapper.class);

		return (T) ow.object;
	}

	/**
	 * Read an object from a file.
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param f
	 *            the file
	 * @param charset
	 *            the charsetName sent to the reader which reads the file IFF
	 *            the file is not binary
	 * @return object read from file
	 * @throws IOException
	 *             problem reading file
	 */
	@SuppressWarnings("unchecked")
	public static <T extends InternalReadable> T read(File f, String charset) throws IOException {
		final ObjectWrapper ow = IOUtils.read(f, ObjectWrapper.class, charset);

		return (T) ow.object;
	}

	/**
	 * Write an object to a file fully. The object will be saved with class
	 * information so that it can be automatically re-instantiated using
	 * {@link #read(File)} without needing to know the actual type.
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param f
	 *            the file
	 * @param object
	 *            the object to write
	 * @throws IOException
	 *             problem reading file
	 */
	public static <T extends WriteableBinary> void writeBinaryFull(File f, T object) throws IOException {
		IOUtils.writeBinary(f, new ObjectWrapper(object));
	}

	/**
	 * Write an object to a file fully. The object will be saved with class
	 * information so that it can be automatically re-instantiated using
	 * {@link #read(File)} without needing to know the actual type.
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param f
	 *            the file
	 * @param object
	 *            the object to write
	 * @throws IOException
	 *             problem reading file
	 */
	public static <T extends WriteableASCII> void writeASCIIFull(File f, T object) throws IOException {
		IOUtils.writeASCII(f, new ObjectWrapper(object));
	}

	/**
	 * Write an object to a file fully. The object will be saved with class
	 * information so that it can be automatically re-instantiated using
	 * {@link #read(File)} without needing to know the actual type.
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param f
	 *            the file
	 * @param object
	 *            the object to write
	 * @param charset
	 *            the charsetName sent to the internal writer
	 * @throws IOException
	 *             problem reading file
	 */
	public static <T extends WriteableASCII> void writeASCIIFull(File f, T object, String charset) throws IOException {
		IOUtils.writeASCII(f, new ObjectWrapper(object), charset);
	}

	/**
	 * Read a new instance of type class from a file.
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param f
	 *            the file
	 * @param cls
	 *            the class
	 * @return new instance of class instantiated from the file
	 * @throws IOException
	 *             problem reading file
	 */
	public static <T extends InternalReadable> T read(File f, Class<T> cls) throws IOException {
		return read(f, newInstance(cls));
	}

	/**
	 * Read a new instance of type class from a file.
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param f
	 *            the file
	 * @param cls
	 *            the class
	 * @param charset
	 *            the charsetName sent to the reader which reads the file IFF
	 *            the file is not binary
	 * @return new instance of class instantiated from the file
	 * @throws IOException
	 *             problem reading file
	 */
	public static <T extends InternalReadable> T read(File f, Class<T> cls, String charset) throws IOException {
		return read(f, newInstance(cls), charset);
	}

	/**
	 * Read a new instance of type class from an input stream.
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param ios
	 *            the input stream
	 * @param cls
	 *            the class
	 * @return new instance of class instantiated from the stream
	 * @throws IOException
	 *             problem reading stream
	 */
	public static <T extends InternalReadable> T read(InputStream ios, Class<T> cls) throws IOException {
		return read(ios, newInstance(cls));
	}

	/**
	 * Read a new instance of type class from an input stream.
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param ios
	 *            the input stream
	 * @param cls
	 *            the class
	 * @param charset
	 *            the charsetName sent to the internal inputstreamreader
	 * @return new instance of class instantiated from the stream
	 * @throws IOException
	 *             problem reading stream
	 */
	public static <T extends InternalReadable> T read(InputStream ios, Class<T> cls, String charset) throws IOException {
		return read(ios, newInstance(cls), charset);
	}

	/**
	 * Open file input stream and call Readable#read(InputStream,T)
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param f
	 *            the file
	 * @param obj
	 *            the object of type T
	 * @return A new instance of type T
	 * @throws IOException
	 *             an error reading the file
	 */
	public static <T extends InternalReadable> T read(File f, T obj) throws IOException {
		final FileInputStream fos = new FileInputStream(f);
		try {
			return read(fos, obj);
		} finally {
			if (fos != null)
				fos.close();
		}
	}

	/**
	 * Open file input stream and call Readable#read(InputStream,T)
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param f
	 *            the file
	 * @param obj
	 *            the object of type T
	 * @param charset
	 *            the charsetName sent to the reader which reads the file IFF
	 *            the file is not binary
	 * @return A new instance of type T
	 * @throws IOException
	 *             an error reading the file
	 */
	public static <T extends InternalReadable> T read(File f, T obj, String charset) throws IOException {
		final FileInputStream fos = new FileInputStream(f);
		try {
			return read(fos, obj, charset);
		} finally {
			if (fos != null)
				fos.close();
		}
	}

	/**
	 * Read an instance of an object from an input stream. The stream is tested
	 * to contain the ASCII or binary header and the appropriate read instance
	 * is called.
	 * 
	 * @see Readable#binaryHeader
	 * @see Readable#readBinary
	 * @see Readable#readASCII
	 * @param <T>
	 *            instance type expected
	 * @param fis
	 *            the input stream
	 * @param obj
	 *            the object to instantiate
	 * @return the object
	 * 
	 * @throws IOException
	 *             if there is a problem reading the stream from the file
	 */
	public static <T extends InternalReadable> T read(InputStream fis, T obj) throws IOException {
		final BufferedInputStream bis = new BufferedInputStream(fis);
		if (obj instanceof ReadableBinary && isBinary(bis, ((ReadableBinary) obj).binaryHeader())) {
			final byte[] header = new byte[((ReadableBinary) obj).binaryHeader().length];
			bis.read(header, 0, header.length);
			((ReadableBinary) obj).readBinary(new DataInputStream(bis));
			return obj;
		} else {
			final BufferedReader br = new BufferedReader(new InputStreamReader(bis));
			final char[] holder = new char[((ReadableASCII) obj).asciiHeader().length()];
			br.read(holder);
			((ReadableASCII) obj).readASCII(new Scanner(br));
			return obj;
		}
	}

	/**
	 * Read an instance of an object from an input stream. The stream is tested
	 * to contain the ASCII or binary header and the appropriate read instance
	 * is called.
	 * 
	 * @see Readable#binaryHeader
	 * @see Readable#readBinary
	 * @see Readable#readASCII
	 * @param <T>
	 *            instance type expected
	 * @param fis
	 *            the input stream
	 * @param obj
	 *            the object to instantiate
	 * @param charset
	 *            the charsetName sent the to the inputstreamreader
	 * @return the object
	 * 
	 * @throws IOException
	 *             if there is a problem reading the stream from the file
	 */
	public static <T extends InternalReadable> T read(InputStream fis, T obj, String charset) throws IOException {
		final BufferedInputStream bis = new BufferedInputStream(fis);
		if (obj instanceof ReadableBinary && isBinary(bis, ((ReadableBinary) obj).binaryHeader())) {
			final byte[] header = new byte[((ReadableBinary) obj).binaryHeader().length];
			bis.read(header, 0, header.length);
			((ReadableBinary) obj).readBinary(new DataInputStream(bis));
			return obj;
		} else {
			final BufferedReader br = new BufferedReader(new InputStreamReader(bis, charset));
			final char[] holder = new char[((ReadableASCII) obj).asciiHeader().length()];
			br.read(holder);
			((ReadableASCII) obj).readASCII(new Scanner(br));
			return obj;
		}
	}

	/**
	 * Read an instance of an object from a reader. The stream is assumed to be
	 * ascii and the appropriate read instance is called.
	 * 
	 * @see Readable#readASCII
	 * @param <T>
	 *            instance type expected
	 * @param fis
	 *            the input stream
	 * @param obj
	 *            the object to instantiate
	 * @return the object
	 * 
	 * @throws IOException
	 *             if there is a problem reading the stream from the file
	 */
	public static <T extends InternalReadable> T read(Reader fis, T obj) throws IOException {
		final BufferedReader br = new BufferedReader(fis);
		final char[] holder = new char[((ReadableASCII) obj).asciiHeader().length()];
		br.read(holder);
		((ReadableASCII) obj).readASCII(new Scanner(br));
		return obj;
	}

	/**
	 * Read an instance of an object from a reader. The stream is assumed to be
	 * ascii and the appropriate read instance is called.
	 * 
	 * @see Readable#readASCII
	 * @param <T>
	 *            instance type expected
	 * @param fis
	 *            the input stream
	 * @param cls
	 *            the object to instantiate
	 * @return the object
	 * 
	 * @throws IOException
	 *             if there is a problem reading the stream from the file
	 */
	public static <T extends InternalReadable> T read(Reader fis, Class<T> cls) throws IOException {
		return read(fis, newInstance(cls));
	}

	/**
	 * Opens an input stream and calls input stream version
	 * 
	 * @see IOUtils#isBinary(BufferedInputStream,byte[])
	 * 
	 * @param f
	 *            file containing data
	 * @param header
	 *            expected header in binary format
	 * @return is the file in the binary format
	 * @throws IOException
	 *             if there was a problem reading the input stream
	 */
	public static boolean isBinary(File f, byte[] header) throws IOException {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		try {
			fis = new FileInputStream(f);
			bis = new BufferedInputStream(fis);

			return isBinary(bis, header);
		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (final IOException e) {
				}
			if (bis != null)
				try {
					bis.close();
				} catch (final IOException e) {
				}
		}
	}

	/**
	 * Extracts the binary header from object and calls the byte[] version
	 * 
	 * @see IOUtils#isBinary(BufferedInputStream,byte[])
	 * 
	 * @param <T>
	 *            expected data type
	 * @param bis
	 *            stream containing data
	 * @param obj
	 *            instance of expected data type
	 * @return does the stream contain binary information
	 * @throws IOException
	 *             problem reading input stream
	 */
	public static <T extends ReadableBinary> boolean isBinary(BufferedInputStream bis, T obj) throws IOException {
		return isBinary(bis, obj.binaryHeader());
	}

	/**
	 * Checks whether a given input stream contains readable binary information
	 * by checking for the first header.length bytes == header. The stream is
	 * reset to the beginning of the header once checked.
	 * 
	 * @param bis
	 *            stream containing data
	 * @param header
	 *            expected binary header
	 * @return does the stream contain binary information
	 * @throws IOException
	 *             problem reading or reseting the stream
	 */
	public static boolean isBinary(BufferedInputStream bis, byte[] header) throws IOException {
		bis.mark(header.length + 10);
		final byte[] aheader = new byte[header.length];
		bis.read(aheader, 0, aheader.length);
		bis.reset();

		return Arrays.equals(aheader, header);
	}

	/**
	 * Write a Writeable T object instance. Opens a file stream and calls output
	 * stream version
	 * 
	 * @see IOUtils#writeBinary(OutputStream,Writeable)
	 * 
	 * @param <T>
	 *            data type to be written
	 * @param f
	 *            file to write instance to
	 * @param obj
	 *            instance to be written
	 * @throws IOException
	 *             error reading file
	 */
	public static <T extends WriteableBinary> void writeBinary(File f, T obj) throws IOException {
		final FileOutputStream fos = new FileOutputStream(f);
		try {
			writeBinary(fos, obj);
			fos.flush();
		} finally {
			if (fos != null)
				fos.close();
		}
	}

	/**
	 * Write a Writeable T object instance to the provided output stream, calls
	 * BufferedOutputStream version
	 * 
	 * @see IOUtils#writeBinary(BufferedOutputStream,Writeable)
	 * 
	 * @param <T>
	 *            Expected data type
	 * @param fos
	 *            output stream
	 * @param obj
	 *            the object to write
	 * @throws IOException
	 *             error writing to stream
	 */
	public static <T extends WriteableBinary> void writeBinary(OutputStream fos, T obj) throws IOException {
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(fos);
			writeBinary(bos, obj);
		} finally {
			if (bos != null) {
				bos.flush();
			}
		}

	}

	/**
	 * Writeable object is written to the output stream in binary format.
	 * Firstly the binaryHeader is written then the object is handed the output
	 * stream to write it's content.
	 * 
	 * @see Writeable#writeBinary(java.io.DataOutput)
	 * @see Writeable#binaryHeader()
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param bos
	 *            the output stream
	 * @param obj
	 *            the object to write
	 * @throws IOException
	 *             error writing to stream
	 */
	public static <T extends WriteableBinary> void writeBinary(BufferedOutputStream bos, T obj) throws IOException {
		final DataOutputStream dos = new DataOutputStream(bos);
		dos.write(obj.binaryHeader());
		obj.writeBinary(dos);
	}

	/**
	 * Writeable object is written to the a file in ASCII format. File stream is
	 * opened and stream version is called
	 * 
	 * @see IOUtils#writeASCII(OutputStream, Writeable)
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param f
	 *            the file to write to
	 * @param obj
	 *            the object to write
	 * @throws IOException
	 *             error writing to file
	 */
	public static <T extends WriteableASCII> void writeASCII(File f, T obj) throws IOException {
		final FileOutputStream fos = new FileOutputStream(f);
		try {
			writeASCII(fos, obj);
			fos.flush();
		} finally {
			if (fos != null)
				fos.close();
		}
	}

	/**
	 * Writeable object is written to the a file in ASCII format. File stream is
	 * opened and stream version is called
	 * 
	 * @see IOUtils#writeASCII(OutputStream, Writeable)
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param f
	 *            the file to write to
	 * @param obj
	 *            the object to write
	 * @param charset
	 *            the charsetName sent to the internal writer
	 * @throws IOException
	 *             error writing to file
	 */
	public static <T extends WriteableASCII> void writeASCII(File f, T obj, String charset) throws IOException {
		final FileOutputStream fos = new FileOutputStream(f);
		try {
			writeASCII(fos, obj, charset);
			fos.flush();
		} finally {
			if (fos != null)
				fos.close();
		}
	}

	/**
	 * Write the object in ASCII format to the output stream. Construct a
	 * PrintWriter using the outputstream, write the object's ASCII header then
	 * write the object in ASCII format.
	 * 
	 * @see PrintWriter
	 * @see Writeable#asciiHeader()
	 * @see Writeable#writeASCII(PrintWriter)
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param fos
	 *            the output stream
	 * @param obj
	 *            the object
	 * @throws IOException
	 *             error writing to stream
	 */
	public static <T extends WriteableASCII> void writeASCII(OutputStream fos, T obj) throws IOException {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(fos);
			pw.print(obj.asciiHeader());
			obj.writeASCII(pw);
		} finally {
			if (pw != null) {
				pw.flush();
			}
		}
	}

	/**
	 * Write the object in ASCII format to the output stream. Construct a
	 * PrintWriter using the outputstream, write the object's ASCII header then
	 * write the object in ASCII format.
	 * 
	 * @see PrintWriter
	 * @see Writeable#asciiHeader()
	 * @see Writeable#writeASCII(PrintWriter)
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param fos
	 *            the output stream
	 * @param obj
	 *            the object
	 * @param charset
	 *            the charsetName sent to the internal outputstreamwriter
	 * @throws IOException
	 *             error writing to stream
	 */
	public static <T extends WriteableASCII> void writeASCII(OutputStream fos, T obj, String charset) throws IOException {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(fos, charset)));
			pw.print(obj.asciiHeader());
			obj.writeASCII(pw);
		} finally {
			if (pw != null) {
				pw.flush();
			}
		}
	}

	/**
	 * Write the object in ASCII format to the output stream. Construct a
	 * PrintWriter using the outputstream, write the object's ASCII header then
	 * write the object in ASCII format.
	 * 
	 * @see PrintWriter
	 * @see Writeable#asciiHeader()
	 * @see Writeable#writeASCII(PrintWriter)
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param writer
	 *            the output stream
	 * @param obj
	 *            the object
	 * @throws IOException
	 *             error writing to stream
	 */
	public static <T extends WriteableASCII> void writeASCII(Writer writer, T obj) throws IOException {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(writer);
			pw.print(obj.asciiHeader());
			obj.writeASCII(pw);
		} finally {
			if (pw != null) {
				pw.flush();
			}
		}
	}

	/**
	 * Check whether a given file is readable by a given Writeable class.
	 * Instantiates the class to get it's binary and ascii header which is then
	 * passed to the byte[] version of this method
	 * 
	 * @see Readable#asciiHeader()
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param f
	 *            the file to check
	 * @param cls
	 *            the class to instantiate the Readable object
	 * @return is file readable by a given class
	 * @throws IOException
	 *             error reading file
	 */
	public static <T extends InternalReadable> boolean readable(File f, Class<T> cls) throws IOException {
		final InternalReadable obj = newInstance(cls);

		return (obj instanceof ReadableBinary && readable(f, ((ReadableBinary) obj).binaryHeader())) ||
				(obj instanceof ReadableASCII && readable(f, ((ReadableASCII) obj).asciiHeader()));
	}

	/**
	 * Check whether a file is readable by checking it's first bytes contains
	 * the header. Converts the header to a byte[] and calls the byte[] version
	 * of this method
	 * 
	 * @see IOUtils#readable(File, byte[])
	 * 
	 * @param f
	 *            the file to check
	 * @param header
	 *            the header to check with
	 * @return does this file contain this header
	 * @throws IOException
	 *             error reading file
	 */
	public static boolean readable(File f, String header) throws IOException {
		return readable(f, header.getBytes());
	}

	/**
	 * Check readability by checking whether a file starts with a given header.
	 * Instantiate an input stream and check whether the stream isBinary (i.e.
	 * starts with the header)
	 * 
	 * @see IOUtils#isBinary(BufferedInputStream, byte[])
	 * 
	 * @param f
	 *            file to check
	 * @param header
	 *            the expected header
	 * @return does the file start with the header
	 * @throws IOException
	 *             error reading file
	 */
	public static boolean readable(File f, byte[] header) throws IOException {
		FileInputStream fos = null;
		try {
			fos = new FileInputStream(f);
			final BufferedInputStream bis = new BufferedInputStream(fos);
			return isBinary(bis, header);
		} finally {
			try {
				if (fos != null)
					fos.close();
			} catch (final IOException e) {
			}
		}
	}

	/**
	 * Check whether an InputStream can be read by an instantiated class based
	 * on it's binary and ascii headers. Buffered so the stream can be reset
	 * after the check.
	 * 
	 * @param <T>
	 *            instance type expected
	 * @param bis
	 *            the stream
	 * @param cls
	 *            the class to instantiate and check
	 * @return can an object be read from this stream of the class type
	 * @throws IOException
	 *             error reading stream
	 */
	public static <T extends InternalReadable> boolean readable(BufferedInputStream bis, Class<T> cls) throws IOException
	{
		final InternalReadable obj = newInstance(cls);

		return (obj instanceof ReadableBinary && readable(bis, ((ReadableBinary) obj).binaryHeader())) ||
				(obj instanceof ReadableASCII && readable(bis, ((ReadableASCII) obj).asciiHeader()));
	}

	/**
	 * Check whether an input stream starts with a header string. Calls the
	 * byte[] version of this function
	 * 
	 * @see IOUtils#isBinary(BufferedInputStream, byte[])
	 * 
	 * @param bis
	 *            the input stream
	 * @param header
	 *            the header
	 * @return whether the stream starts with the string
	 * @throws IOException
	 *             error reading stream
	 */
	public static boolean readable(BufferedInputStream bis, String header) throws IOException {
		return readable(bis, header.getBytes());
	}

	/**
	 * Check whether a stream starts with a header. Uses isBinary (therefore
	 * resetting the stream after the check)
	 * 
	 * @param bis
	 *            the input stream
	 * @param header
	 *            the byte[] header
	 * @return whether the stream starts with the header
	 * @throws IOException
	 *             error reading stream
	 */
	public static boolean readable(BufferedInputStream bis, byte[] header) throws IOException {
		return isBinary(bis, header);
	}

	/**
	 * Convenience function for serializing a writeable object as a byte array.
	 * Calls {@link IOUtils#writeBinary(OutputStream, WriteableBinary)} on a
	 * {@link ByteArrayOutputStream} then calls
	 * {@link ByteArrayOutputStream#toByteArray()}
	 * 
	 * @param object
	 * @return serialised object
	 * @throws IOException
	 */
	public static byte[] serialize(WriteableBinary object) throws IOException {
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		IOUtils.writeBinary(stream, object);
		return stream.toByteArray();
	}

	/**
	 * Convenience function for deserializing an object from a String. Calls
	 * {@link ReadableASCII#readASCII(Scanner)} with a Scanner initialized from
	 * a StringReader
	 * 
	 * @param source
	 *            where to read from
	 * @param clazz
	 *            the class of the output
	 * @param <T>
	 *            the type to output
	 * @return a new instance of T
	 * @throws IOException
	 */
	public static <T extends ReadableASCII> T fromString(String source, Class<T> clazz) throws IOException {
		final T out = IOUtils.read(new ByteArrayInputStream(source.getBytes()), clazz);
		return out;
	}

	/**
	 * Convenience function for deserializing an object from a byte array. Calls
	 * {@link IOUtils#read(InputStream, Class)} on a
	 * {@link ByteArrayInputStream}.
	 * 
	 * @param source
	 *            where to read from
	 * @param clazz
	 *            the class of the output
	 * @param <T>
	 *            the type to output
	 * @return a new instance of T
	 * @throws IOException
	 */
	public static <T extends ReadableBinary> T deserialize(byte[] source, Class<T> clazz) throws IOException {
		final ByteArrayInputStream stream = new ByteArrayInputStream(source);
		final T out = IOUtils.read(stream, clazz);
		return out;
	}

	/**
	 * Convenience function for deserializing an object from a byte array. Calls
	 * {@link IOUtils#read(InputStream, Class)} on a
	 * {@link ByteArrayInputStream}.
	 * 
	 * @param source
	 *            where to read from
	 * @param clazz
	 *            the class of the output
	 * @param <T>
	 *            the type to output
	 * @param skip
	 *            number of bytes to skip
	 * @return a new instance of T
	 * @throws IOException
	 */
	public static <T extends ReadableBinary> T deserialize(byte[] source, long skip, Class<T> clazz) throws IOException {
		final ByteArrayInputStream stream = new ByteArrayInputStream(source);
		stream.skip(skip);
		final T out = IOUtils.read(stream, clazz);
		return out;
	}

	/**
	 * Convenience function for deserializing an object from a byte array. Calls
	 * {@link IOUtils#read(InputStream, Class)} on a
	 * {@link ByteArrayInputStream}.
	 * 
	 * @param source
	 *            where to read from
	 * @param instance
	 *            a T instance
	 * @param <T>
	 *            the type to output
	 * @return a new instance of T
	 * @throws IOException
	 */
	public static <T extends InternalReadable> T deserialize(byte[] source, T instance) throws IOException {
		final ByteArrayInputStream stream = new ByteArrayInputStream(source);
		final T out = IOUtils.read(stream, instance);
		return out;
	}

	/**
	 * Writes an object to a file using the Kryo serialisation library. The
	 * object doesn't need to have any special serialisation attributes.
	 * 
	 * @param obj
	 *            the object to write
	 * @param out
	 *            the output sink
	 * @throws IOException
	 */
	public static void writeToFile(Object obj, File out) throws IOException {
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(new FileOutputStream(out));

			write(obj, dos);
		} finally {
			if (dos != null)
				try {
					dos.close();
				} catch (final IOException e) {
				}
		}
	}

	/**
	 * Writes an object using the Kryo serialisation library. The object doesn't
	 * need to have any special serialisation attributes.
	 * 
	 * @param obj
	 *            the object to write
	 * @param out
	 *            the output sink
	 * @throws IOException
	 */
	public static void write(Object obj, DataOutput out) throws IOException {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final Output output = new Output(bos);

		final Kryo kryo = new Kryo();
		kryo.writeClassAndObject(output, obj);
		output.flush();

		final byte[] array = bos.toByteArray();

		out.writeInt(array.length);
		out.write(array);
	}

	/**
	 * Utility method to read any object written with
	 * {@link #write(Object, DataOutput)}.
	 * 
	 * @param <T>
	 *            type of object
	 * @param in
	 *            input
	 * @return the object
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T read(DataInput in) throws IOException {
		final int length = in.readInt();

		final byte[] bytes = new byte[length];
		in.readFully(bytes);

		final Kryo kryo = new Kryo();
		Object obj;
		try {
			obj = kryo.readClassAndObject(new Input(bytes));
		} catch (final KryoException e) {
			kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
			obj = kryo.readClassAndObject(new Input(bytes));
		}
		return (T) obj;
	}

	/**
	 * Utility method to read any object written with
	 * {@link #writeToFile(Object, File)}.
	 * 
	 * @param <T>
	 *            type of object
	 * @param in
	 *            input file
	 * @return the object
	 * @throws IOException
	 */
	public static <T> T readFromFile(File in) throws IOException {
		DataInputStream din = null;
		try {
			din = new DataInputStream(new FileInputStream(in));

			// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6302954
			return IOUtils.<T> read(din);
		} finally {
			if (din != null)
				try {
					din.close();
				} catch (final IOException e) {
				}
		}
	}

	/**
	 * Test whether the data in the given {@link InputStream} can be read by the
	 * given {@link InputStreamObjectReader}. This method tries to ensure that
	 * the stream is reset to its initial condition.
	 * 
	 * @param reader
	 *            the {@link InputStreamObjectReader}.
	 * @param is
	 *            the stream
	 * @param name
	 *            the name of the file/object behind the stream (can be null)
	 * @return true if the {@link InputStreamObjectReader} can read from this
	 *         stream; false otherwise.
	 * @throws IOException
	 *             if an error occurs resetting the stream.
	 */
	public static boolean canRead(InputStreamObjectReader<?> reader, BufferedInputStream is, String name)
			throws IOException
	{
		try {
			is.mark(1024 * 1024);
			return reader.canRead(is, name);
		} finally {
			is.reset();
		}
	}

	/**
	 * Test whether the data in the given source can be read by the given
	 * {@link ObjectReader}.
	 * 
	 * @param reader
	 *            the {@link ObjectReader}.
	 * @param source
	 *            the source
	 * @param name
	 *            the name of the file/object behind the stream (can be null)
	 * @return true if the {@link ObjectReader} can read from this stream; false
	 *         otherwise.
	 * @throws IOException
	 *             if an error occurs resetting the stream.
	 */
	public static <SRC> boolean canRead(ObjectReader<?, SRC> reader, SRC source, String name) throws IOException {
		return reader.canRead(source, name);
	}
}
