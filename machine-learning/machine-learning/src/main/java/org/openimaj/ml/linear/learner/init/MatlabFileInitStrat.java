package org.openimaj.ml.linear.learner.init;

import gov.sandia.cognition.math.matrix.Matrix;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.openimaj.math.matrix.CFMatrixUtils;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;

/**
 * Given a matlab file, return its matrix held in the "arr" field as the initialisation matrix
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class MatlabFileInitStrat implements InitStrategy{

	private Matrix data;

	/**
	 * @param matfile the file from which to read
	 * @throws IOException
	 */
	public MatlabFileInitStrat(File matfile) throws IOException {
		MatFileReader reader = new MatFileReader(matfile);
		Map<String, MLArray> content = reader.getContent();
		this.data= CFMatrixUtils.asMat(content.get("arr"));
	}

	@Override
	public Matrix init(int rows, int cols) {
		return data;
	}


}
