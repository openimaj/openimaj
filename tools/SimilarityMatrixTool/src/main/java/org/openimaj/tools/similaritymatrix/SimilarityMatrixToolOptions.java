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
package org.openimaj.tools.similaritymatrix;


import java.io.File;
import java.io.IOException;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ProxyOptionHandler;
import org.openimaj.io.IOUtils;
import org.openimaj.math.matrix.similarity.SimilarityMatrix;
import org.openimaj.math.matrix.similarity.processor.InvertData;
import org.openimaj.tools.similaritymatrix.modes.Binarize;
import org.openimaj.tools.similaritymatrix.modes.ConnectedComponents;
import org.openimaj.tools.similaritymatrix.modes.DensestSubgraph;
import org.openimaj.tools.similaritymatrix.modes.MDS;
import org.openimaj.tools.similaritymatrix.modes.PrettyPrint;
import org.openimaj.tools.similaritymatrix.modes.ToolMode;

public class SimilarityMatrixToolOptions {
	public enum Mode implements CmdLineOptionsProvider {
		PRETTY_PRINT {
			@Override
			public ToolMode getOptions() {
				return new PrettyPrint();
			}
		},
		BINARIZE {
			@Override
			public ToolMode getOptions() {
				return new Binarize();
			}
		},
		DENSEST_SUBGRAPH {
			@Override
			public ToolMode getOptions() {
				return new DensestSubgraph();
			}
		},
		CONNECTED_COMPS {
			@Override
			public ToolMode getOptions() {
				return new ConnectedComponents();
			}
		},
		MDS {
			@Override
			public ToolMode getOptions() {
				return new MDS();
			}
		}
	}
	
	@Option(name="--mode", aliases="-m", required=true, usage="Tool mode", handler=ProxyOptionHandler.class)
	Mode mode;
	ToolMode modeOp;
	
	@Option(name="--invert", required=false, usage="invert data")
	boolean invertData = false;
	
	@Option(name="--input", aliases="-i", required=true, usage="input similarity matrix")
	File input;
	
	@Option(name="--output", aliases="-o", required=false, usage="Output file. If not given, then output will be printed to stdout")
	File output = null;
	
	public ToolMode getToolMode() {
		return modeOp;
	}
	
	public SimilarityMatrix getInput() throws IOException {
		SimilarityMatrix matrix = IOUtils.read(input, SimilarityMatrix.class);
		
		if (invertData)
			matrix = matrix.processInplace(new InvertData());
		
		return matrix;
	}
	
	public File getOutput() {
		return output;
	}
}
