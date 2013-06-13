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
package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.matlabio;

import java.io.IOException;
import java.util.Map;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;

public class MatlabIO {
	public static void main(String[] args) throws IOException {
		// MLCell cell = new MLCell("data", new int[]{100000,1});
		// Random r = new Random();
		// for (int i = 0; i < 100000; i++) {
		// MLCell inner = new MLCell(null, new int[]{2,1});
		// inner.set(new MLChar(null,"Dummy String" + r.nextDouble()), 0, 0);
		// MLDouble d = new MLDouble(null, new double[][]{new
		// double[]{r.nextDouble()}});
		// inner.set(d, 1, 0);
		// cell.set(inner, i,0);
		// }
		// ArrayList<MLArray> arr = new ArrayList<MLArray>();
		// arr.add(cell);
		// new MatFileWriter( "mat_file.mat", arr);
		final MatFileReader reader = new MatFileReader("/Users/ss/Development/python/storm-spams/XYs.mat");
		final Map<String, MLArray> content = reader.getContent();
		final MLCell cell = (MLCell) content.get("XYs");
		System.out.println(cell.get(0, 0));
		System.out.println(cell.get(0, 1));
	}
}
