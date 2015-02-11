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
package org.openimaj.demos.ml.linear.data;

import gov.sandia.cognition.learning.data.DefaultInputOutputPair;
import gov.sandia.cognition.learning.data.InputOutputPair;
import gov.sandia.cognition.learning.function.kernel.LinearKernel;
import gov.sandia.cognition.math.matrix.VectorFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import no.uib.cipr.matrix.Vector;

import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.ColourSpace;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.math.geometry.line.Line2d;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.math.geometry.shape.Circle;
import org.openimaj.ml.linear.data.LinearPerceptronDataGenerator;
import org.openimaj.ml.linear.kernel.LinearVectorKernel;
import org.openimaj.ml.linear.learner.perceptron.DoubleArrayKernelPerceptron;
import org.openimaj.ml.linear.learner.perceptron.MeanCenteredKernelPerceptron;
import org.openimaj.ml.linear.learner.perceptron.MeanCenteredProjectron;
import org.openimaj.ml.linear.learner.perceptron.PerceptronClass;
import org.openimaj.ml.linear.learner.perceptron.SimplePerceptron;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.stream.Stream;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class DrawLinearData {

	private static final int TOTAL_DATA_ITEMS = 1000;
	private static final int SEED = 1;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		final LinearPerceptronDataGenerator dg = dataGen();
		Stream<IndependentPair<double[], PerceptronClass>> dataStream;
		drawData(dg);
		writeData(new File("/Users/ss/Experiments/perceptron/test.data"));
		// dataStream = new
		// LimitedDataStream<double[],PerceptronClass>(dataGen(),TOTAL_DATA_ITEMS);
		dataStream = new RepeatingDataStream<double[], PerceptronClass>(dataGen(), TOTAL_DATA_ITEMS);
		final MeanCenteredKernelPerceptron mkp = new MeanCenteredKernelPerceptron(new LinearVectorKernel());
		// MatrixKernelPerceptron mkp = new MarginMeanCenteredPerceptron(new
		// LinearVectorKernel(),10000d);
		// MatrixKernelPerceptron mkp = new MeanCenteredProjectron(new
		// LinearVectorKernel());
		// MatrixKernelPerceptron mkp = new Projectron(new
		// LinearVectorKernel());
		// MatrixKernelPerceptron mkp = new
		// ThresholdMatrixKernelPerceptron(0.01, 0, new LinearVectorKernel());
		// MatrixKernelPerceptron mkp = new MatrixKernelPerceptron(new
		// LinearVectorKernel());
		// SimplePerceptron mkp = new SimplePerceptron();
		leanrnPoints(mkp, dataStream);
		// leanrnPointsProjectron();
		// leanrnCogFound();
	}

	private static void writeData(File file) throws IOException {
		final LinearPerceptronDataGenerator gen = dataGen();
		final File pf = file.getParentFile();
		if (!pf.exists())
			pf.mkdirs();
		final PrintWriter fw = new PrintWriter(file);
		for (int i = 0; i < TOTAL_DATA_ITEMS; i++) {
			final IndependentPair<double[], PerceptronClass> d = gen.generate();
			fw.println(Arrays.toString(d.firstObject()));
			fw.println(d.secondObject() == PerceptronClass.TRUE ? 1 : 0);
		}
		fw.close();
	}

	private static void drawData(LinearPerceptronDataGenerator dg) {
		final Stream<IndependentPair<double[], PerceptronClass>> dataStream = new LimitedDataStream<double[], PerceptronClass>(
				dg, TOTAL_DATA_ITEMS);
		final Vector origin = dg.getOrigin();

		final Vector dir = dg.getPlane()[0];
		final Point2d lineStart = start(origin, dir);
		final Point2d lineEnd = end(origin, dir);
		final Line2d line = new Line2d(lineStart, lineEnd);

		drawPoints(dataStream, line);
	}

	private static LinearPerceptronDataGenerator dataGen() {
		final LinearPerceptronDataGenerator dg = new LinearPerceptronDataGenerator(300, 2, 0.3, SEED);
		return dg;
	}

	private static void learnCogFound() {
		final LinearPerceptronDataGenerator dg = dataGen();
		final gov.sandia.cognition.learning.algorithm.perceptron.kernel.KernelPerceptron<gov.sandia.cognition.math.matrix.Vector> mkp = new gov.sandia.cognition.learning.algorithm.perceptron.kernel.KernelPerceptron<gov.sandia.cognition.math.matrix.Vector>(
				new LinearKernel());
		mkp.learn(createData());
		// System.out.println(mkp.getErrorCount());

	}

	private static Collection<? extends InputOutputPair<? extends gov.sandia.cognition.math.matrix.Vector, Boolean>>
	createData()
	{
		final List<InputOutputPair<gov.sandia.cognition.math.matrix.Vector, Boolean>> ret = new ArrayList<InputOutputPair<gov.sandia.cognition.math.matrix.Vector, Boolean>>();
		final LinearPerceptronDataGenerator dg = dataGen();
		for (int i = 0; i < TOTAL_DATA_ITEMS; i++) {
			final IndependentPair<double[], PerceptronClass> pointClass = dg.generate();
			final double[] pc = pointClass.firstObject();
			final PerceptronClass pcc = pointClass.secondObject();
			final boolean bool = pcc.equals(PerceptronClass.TRUE);
			final gov.sandia.cognition.math.matrix.Vector vec = VectorFactory.getDenseDefault().copyArray(pc);
			final InputOutputPair<gov.sandia.cognition.math.matrix.Vector, Boolean> item = DefaultInputOutputPair.create(
					vec, bool);
			ret.add(item);
		}
		System.out.println("Data created");
		return ret;
	}

	private static void drawMkpLine(DoubleArrayKernelPerceptron mkp) {
		final MBFImage img = new MBFImage(300, 300, ColourSpace.RGB);

		final List<double[]> sup = mkp.getSupports();
		final List<Double> weights = mkp.getWeights();
		final double bias = mkp.getBias();
		System.out.println("Bias: " + bias);
		double[] startD = null;
		double[] endD = null;

		double[] mean = new double[2];
		if (mkp instanceof MeanCenteredKernelPerceptron) {
			mean = ((MeanCenteredKernelPerceptron) mkp).getMean();
		} else if (mkp instanceof MeanCenteredProjectron) {
			mean = ((MeanCenteredProjectron) mkp).getMean();
		}
		startD = LinearVectorKernel.getPlanePoint(sup, weights, bias, -mean[0], Double.NaN);
		endD = LinearVectorKernel.getPlanePoint(sup, weights, bias, img.getWidth() - mean[0], Double.NaN);
		startD[0] += mean[0];
		startD[1] += mean[1];
		endD[0] += mean[0];
		endD[1] += mean[1];
		drawLine(img, startD, endD);
	}

	private static void drawLine(MBFImage img, double[] startD, double[] endD) {
		final Point2d lineStart = new Point2dImpl((float) startD[0], (float) startD[1]);
		final Point2d lineEnd = new Point2dImpl((float) endD[0], (float) endD[1]);

		final Line2d line = new Line2d(lineStart, lineEnd);
		// System.out.println("Drawing: " + line);
		img.drawLine(line, 3, RGBColour.GREEN);
		// img.drawPoint(new Point2dImpl((float)origin.get(0),(float)
		// origin.get(1)), RGBColour.RED, 5);
		DisplayUtilities.displayName(img, "line");
	}

	private static void leanrnPoints(SimplePerceptron mkp, Iterable<IndependentPair<double[], PerceptronClass>> iter) {
		int errors = 0;
		int i = 0;
		for (final IndependentPair<double[], PerceptronClass> pointClass : iter) {
			i++;
			final double[] pc = pointClass.firstObject();
			final PerceptronClass cls = pointClass.getSecondObject();
			final int correctedClass = cls == PerceptronClass.TRUE ? 1 : 0;
			final IndependentPair<double[], Integer> correctedPair = IndependentPair.pair(pc, correctedClass);
			final boolean errorBefore = mkp.predict(correctedPair.firstObject()) != correctedPair.secondObject();
			mkp.process(pc, correctedClass);
			if (errorBefore) {
				errors++;

			}
			if (i % TOTAL_DATA_ITEMS == 0) {
				if (errors == 0) {
					break;
				} else {
					i = 0;
					errors = 0;
				}
			}
		}
		drawSpLine(mkp);
	}

	private static void drawSpLine(SimplePerceptron mkp) {
		final MBFImage img = new MBFImage(300, 300, ColourSpace.RGB);
		final double[] startD = new double[] { 0, Double.NaN };
		final double[] endD = new double[] { img.getWidth(), Double.NaN };

		drawLine(img, mkp.computeHyperplanePoint(startD), mkp.computeHyperplanePoint(endD));
	}

	private static void
	leanrnPoints(DoubleArrayKernelPerceptron mkp, Iterable<IndependentPair<double[], PerceptronClass>> iter)
	{
		int i = 0;
		int errors = 0;
		for (final IndependentPair<double[], PerceptronClass> pointClass : iter) {
			i++;
			final double[] pc = pointClass.firstObject();
			final PerceptronClass cls = pointClass.getSecondObject();
			final int errorBefore = mkp.getErrors();
			mkp.process(pc, cls);
			System.out.println("b: " + mkp.getBias() + " w: "
					+ Arrays.toString(LinearVectorKernel.getDirection(mkp.getSupports(), mkp.getWeights())));
			if (errorBefore != mkp.getErrors()) {
				errors++;
			}
			if (i % TOTAL_DATA_ITEMS == 0) {
				if (errors == 0) {
					break;
				} else {
					i = 0;
					errors = 0;
				}
			}
		}
		drawMkpLine(mkp);
		System.out.println(mkp.getSupports().size());
	}

	private static void drawPoints(Stream<IndependentPair<double[], PerceptronClass>> dataStream, Line2d line) {
		final MBFImage img = new MBFImage(300, 300, ColourSpace.RGB);

		img.drawLine(line, 3, RGBColour.BLUE);

		for (final IndependentPair<double[], PerceptronClass> pointClass : dataStream) {

			final double[] pc = pointClass.firstObject();
			final Point2dImpl point = new Point2dImpl((float) pc[0], (float) pc[1]);
			final PerceptronClass cls = pointClass.getSecondObject();
			switch (cls) {
			case TRUE:
				img.drawShapeFilled(new Circle(point, 5), RGBColour.GREEN);
				break;
			case FALSE:
				img.drawShape(new Circle(point, 5), 3, RGBColour.RED);
				break;
			case NONE:
				throw new RuntimeException("NOPE");
			}
		}
		DisplayUtilities.displayName(img, "random");
	}

	private static Point2d end(Vector origin, Vector dir) {
		final Vector ret = origin.copy().add(10000, dir);
		return new Point2dImpl((float) ret.get(0), (float) ret.get(1));
	}

	private static Point2d start(Vector origin, Vector dir) {
		final Vector ret = origin.copy().add(-10000, dir);
		return new Point2dImpl((float) ret.get(0), (float) ret.get(1));
	}

}
