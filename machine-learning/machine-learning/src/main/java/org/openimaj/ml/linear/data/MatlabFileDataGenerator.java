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
package org.openimaj.ml.linear.data;

import gov.sandia.cognition.math.matrix.Matrix;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.openimaj.math.matrix.CFMatrixUtils;
import org.openimaj.util.pair.Pair;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;

public class MatlabFileDataGenerator implements MatrixDataGenerator<Matrix>{
	private MLCell data;
	private int index;

	public MatlabFileDataGenerator(File matfile) throws IOException {
		MatFileReader reader = new MatFileReader(matfile);
		Map<String, MLArray> content = reader.getContent();
		this.data= (MLCell) content.get("XYs");
		this.index = 0;
	}

	@Override
	public Pair<Matrix> generate() {
		if(index>=this.data.getM())return null;
		Pair<Matrix> XY = new Pair<Matrix>(
				CFMatrixUtils.asMat(this.data.get(index, 0)),
				CFMatrixUtils.asMat(this.data.get(index, 1))
		);
		index++;
		return XY;
	}

	

	public int size() {
		return this.data.getM();
	}
	
}
