package org.openimaj.ml.linear.learner.init;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.openimaj.math.matrix.SandiaMatrixUtils;
import org.openimaj.ml.linear.learner.OnlineLearner;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLDouble;

import gov.sandia.cognition.math.matrix.Matrix;

public class MatlabFileInitStrat implements InitStrategy{

	private Matrix data;

	public MatlabFileInitStrat(File matfile) throws IOException {
		MatFileReader reader = new MatFileReader(matfile);
		Map<String, MLArray> content = reader.getContent();
		this.data= SandiaMatrixUtils.asMat((MLDouble) content.get("arr"));
	}

	@Override
	public Matrix init(int rows, int cols) {
		return data;
	}


}
