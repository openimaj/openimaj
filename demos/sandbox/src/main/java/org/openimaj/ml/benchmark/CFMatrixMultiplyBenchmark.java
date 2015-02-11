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
package org.openimaj.ml.benchmark;

import java.util.Random;

import org.openimaj.math.matrix.CFMatrixUtils;
import org.openimaj.math.matrix.MeanVector;
import org.openimaj.time.Timer;

import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseColumnMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;
import gov.sandia.cognition.math.matrix.mtj.SparseRowMatrix;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class CFMatrixMultiplyBenchmark {
	
	public static void main(String[] args) {
		SparseMatrix a = SparseMatrixFactoryMTJ.INSTANCE.copyMatrix(SparseMatrixFactoryMTJ.INSTANCE.createWrapper(new FlexCompRowMatrix(4, 1118)));
		CFMatrixUtils.plusInplace(a, 1);
		SparseRowMatrix xtrow = CFMatrixUtils.randomSparseRow(1118,22917,0d,1d,1 - 0.9998818947086253, new Random(1));
		SparseColumnMatrix xtcol = CFMatrixUtils.randomSparseCol(1118,22917,0d,1d,1 - 0.9998818947086253, new Random(1));
		
		System.out.println("xtrow sparsity: " + CFMatrixUtils.sparsity(xtrow));
		System.out.println("xtcol sparsity: " + CFMatrixUtils.sparsity(xtcol));
		System.out.println("Equal: " + CFMatrixUtils.fastsparsedot(a,xtcol).equals(a.times(xtcol), 0));
		MeanVector mv = new MeanVector();
		System.out.println("doing: a . xtcol");
		for (int i = 0; i < 10; i++) {
			Timer t = Timer.timer();
			CFMatrixUtils.fastsparsedot(a,xtcol);
			mv.update(new double[]{t.duration()});
			System.out.println("time: " + mv.vec()[0]);
		}
		
		
		mv.reset();
		System.out.println("doing: a . xtcol");
		for (int i = 0; i < 10; i++) {
			Timer t = Timer.timer();
			a.times(xtcol);
			mv.update(new double[]{t.duration()});
			System.out.println("time: " + mv.vec()[0]);
		}
		mv.reset();
		System.out.println("doing: a . xtrow");
		for (int i = 0; i < 10; i++) {
			Timer t = Timer.timer();
			a.times(xtrow);
			mv.update(new double[]{t.duration()});
			System.out.println("time: " + mv.vec()[0]);
		}
	}
}
