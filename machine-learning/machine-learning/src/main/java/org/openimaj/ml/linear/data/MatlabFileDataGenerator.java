package org.openimaj.ml.linear.data;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.openimaj.math.matrix.CFMatrixUtils;
import org.openimaj.util.pair.Pair;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLDouble;

public class MatlabFileDataGenerator implements DataGenerator<Matrix>{
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
