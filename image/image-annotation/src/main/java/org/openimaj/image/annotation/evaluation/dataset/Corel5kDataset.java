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
package org.openimaj.image.annotation.evaluation.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.sf.jasperreports.engine.JRException;

import org.openimaj.data.dataset.ListBackedDataset;
import org.openimaj.data.dataset.ListDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.experiment.evaluation.retrieval.RetrievalEvaluator;
import org.openimaj.experiment.evaluation.retrieval.analysers.IREvalAnalyser;
import org.openimaj.experiment.evaluation.retrieval.analysers.IREvalResult;
import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.ml.annotation.evaluation.AnnotationEvaluator;
import org.openimaj.ml.annotation.linear.DenseLinearTransformAnnotator;

public class Corel5kDataset extends ListBackedDataset<CorelAnnotatedImage> {
	File baseDir = new File("/Users/jsh2/Data/corel-5k");
	File imageDir = new File(baseDir, "images");
	File metaDir = new File(baseDir, "metadata");

	public Corel5kDataset() throws IOException {
		for (final File f : imageDir.listFiles()) {
			if (f.getName().endsWith(".jpeg")) {
				final String id = f.getName().replace(".jpeg", "");

				data.add(new CorelAnnotatedImage(id, f, new File(metaDir, id + "_1.txt")));
			}
		}
	}

	public static class HistogramExtractor implements FeatureExtractor<DoubleFV, ImageWrapper> {
		Map<String, DoubleFV> data = new HashMap<String, DoubleFV>();

		public HistogramExtractor() throws IOException {
			final BufferedReader br = new BufferedReader(new FileReader("/Users/jsh2/Data/corel-5k/BLOBS_data.txt"));
			String line;
			while ((line = br.readLine()) != null) {
				final Scanner sc = new Scanner(line);

				final String id = sc.nextInt() + "";
				final double[] vec = new double[500];

				while (sc.hasNext()) {
					final String token = sc.next();
					final double weight = Double.parseDouble(sc.next().replace(",", ""));

					if (token.startsWith("blob")) {
						final int blobId = Integer.parseInt(token.replace("blob[", "").replace("]", ""));
						vec[blobId - 1] += weight;
					}
				}
				sc.close();

				data.put(id, new DoubleFV(vec));
			}
			br.close();
		}

		@Override
		public DoubleFV extractFeature(ImageWrapper object) {
			// HistogramModel hm = new HistogramModel(4,4,4);
			// hm.estimateModel(object.getImage());
			// return hm.histogram;
			return data.get(object.getID());
		}
	}

	public static void main(String[] args) throws IOException, JRException {
		final Corel5kDataset alldata = new Corel5kDataset();

		final StandardCorel5kSplit split = new StandardCorel5kSplit();
		split.split(alldata);

		final ListDataset<CorelAnnotatedImage> training = split.getTrainingDataset();

		// UniformRandomAnnotator<ImageWrapper, String> ann = new
		// UniformRandomAnnotator<ImageWrapper, String>(new PriorChooser());
		final DenseLinearTransformAnnotator<ImageWrapper, String> ann = new DenseLinearTransformAnnotator<ImageWrapper, String>(
				315, new HistogramExtractor());
		ann.train(DatasetAdaptors.asList(training));

		// for (CorelAnnotatedImage img : split.getTestDataset()) {
		// List<AutoAnnotation<String>> anns = ann.annotate(img.getObject());
		// MBFImage imgf = img.getObject();
		// imgf.processInplace(new ResizeProcessor(400, 400));
		// imgf.drawText(anns.get(0).toString(), 20, 20,
		// HersheyFont.TIMES_BOLD,20);
		// DisplayUtilities.display(imgf);
		// }

		final AnnotationEvaluator<ImageWrapper, String> eval = new AnnotationEvaluator<ImageWrapper, String>(ann,
				split.getTestDataset());

		// ClassificationEvaluator<ROCAnalysisResult<String>, String,
		// ImageWrapper> classEval = eval.newClassificationEvaluator(new
		// ROCAnalyser<ImageWrapper, String>());
		// Map<ImageWrapper, ClassificationResult<String>> classRes =
		// classEval.evaluate();
		// ROCAnalysisResult<String> classAnalysis =
		// classEval.analyse(classRes);
		// System.out.println(classAnalysis);

		final RetrievalEvaluator<IREvalResult, ImageWrapper, String> retEval = eval
				.newRetrievalEvaluator(new IREvalAnalyser<String, ImageWrapper>());
		final Map<String, List<ImageWrapper>> retRes = retEval.evaluate();
		final IREvalResult retAnalysis = retEval.analyse(retRes);
		System.out.println(retAnalysis);

		// final InputStream inputStream =
		// IREvalResult.class.getResourceAsStream("IREvalSummaryReport.jrxml");
		// final ArrayList<IREvalResult> list = new ArrayList<IREvalResult>();
		// list.add(retAnalysis);
		// final JRBeanCollectionDataSource beanColDataSource = new
		// JRBeanCollectionDataSource(list);
		//
		// final Map parameters = new HashMap();
		//
		// final JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
		// final JasperReport jasperReport =
		// JasperCompileManager.compileReport(jasperDesign);
		// final JasperPrint jasperPrint =
		// JasperFillManager.fillReport(jasperReport, parameters,
		// beanColDataSource);
		// JasperExportManager.exportReportToPdfFile(jasperPrint,
		// "test_jasper.pdf");
	}
}
