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
package org.openimaj.hadoop.tools.twitter.token.outputmode.jacard;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Scanner;

import org.openimaj.io.ReadWriteableASCII;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.CSVPrinter;

/**
 * An index encoding the difference between two sets
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class JacardIndex implements ReadWriteableASCII {

	/**
	 * The number of words forming the intersection between now and historic
	 * words
	 */
	public long intersection;
	/**
	 * The number of words forming the union between now and historic words
	 */
	public long union;
	/**
	 * current time period
	 */
	public long time;
	/**
	 * The jacard index is: J(A,B) = |intersection(A,B)| / |union(A,B)| for this
	 * time period
	 */
	public double jacardIndex;

	/**
	 * @param time
	 * @param intersection
	 * @param union
	 */
	public JacardIndex(long time, long intersection, long union) {
		this.time = time;
		this.intersection = intersection;
		this.union = union;
		this.jacardIndex = (double) intersection / (double) union;
	}

	private JacardIndex() {
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		fromString(in.nextLine(), this);
	}

	private static void fromString(String nextLine, JacardIndex i) throws IOException {
		final StringReader reader = new StringReader(nextLine);
		final CSVParser csvreader = new CSVParser(reader);
		final String[] line = csvreader.getLine();
		i.time = Long.parseLong(line[0]);
		i.intersection = Long.parseLong(line[1]);
		i.union = Long.parseLong(line[2]);
		i.jacardIndex = (double) i.intersection / (double) i.union;

	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		final CSVPrinter writer = new CSVPrinter(out);
		writer.write(new String[] {
				"" + this.time,
				"" + intersection,
				"" + union
		});
	}

	/**
	 * Read a new jacard index from a comma separated line
	 * 
	 * @param next
	 * @return new JacardIndex
	 * @throws IOException
	 */
	public static JacardIndex fromString(String next) throws IOException {
		final JacardIndex ind = new JacardIndex();
		fromString(next, ind);
		return ind;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof JacardIndex))
			return false;
		final JacardIndex that = (JacardIndex) other;
		return that.intersection == this.intersection && that.union == this.union;
	}

}
