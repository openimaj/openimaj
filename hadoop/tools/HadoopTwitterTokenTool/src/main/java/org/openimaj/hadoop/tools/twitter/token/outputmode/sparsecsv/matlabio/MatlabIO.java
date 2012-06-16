package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.matlabio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;

public class MatlabIO {
	public static void main(String[] args) throws IOException {
		MLCell cell = new MLCell("data", new int[]{100000,1});
		Random r = new Random();
		for (int i = 0; i < 100000; i++) {
			MLCell inner = new MLCell(null, new int[]{2,1});
			inner.set(new MLChar(null,"Dummy String" + r.nextDouble()), 0, 0);
			MLDouble d = new MLDouble(null, new double[][]{new double[]{r.nextDouble()}});
			inner.set(d, 1, 0);
			cell.set(inner, i,0);
		}
		ArrayList<MLArray> arr = new ArrayList<MLArray>();
		arr.add(cell);
		new MatFileWriter( "mat_file.mat", arr);
	}
}
