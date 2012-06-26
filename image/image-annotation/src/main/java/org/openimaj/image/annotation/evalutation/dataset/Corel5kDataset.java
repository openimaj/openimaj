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
package org.openimaj.image.annotation.evalutation.dataset;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.evaluation.retrieval.RetrievalEvaluator;
import org.openimaj.experiment.evaluation.retrieval.analysers.IREvalAnalyser;
import org.openimaj.experiment.evaluation.retrieval.analysers.IREvalResult;
import org.openimaj.feature.DoubleFV;
import org.openimaj.ml.annotation.FeatureExtractor;
import org.openimaj.ml.annotation.basic.UniformRandomAnnotator;
import org.openimaj.ml.annotation.basic.util.PriorChooser;
import org.openimaj.ml.annotation.evaluation.AnnotationEvaluator;

public class Corel5kDataset extends ListDataset<CorelAnnotatedImage> {
	File baseDir = new File("/Users/jsh2/Data/corel-5k");
	File imageDir = new File(baseDir, "images");
	File metaDir = new File(baseDir, "metadata");

	public Corel5kDataset() throws IOException {
		for (File f : imageDir.listFiles()) {
			if (f.getName().endsWith(".jpeg")) {
				String id = f.getName().replace(".jpeg", "");

				addItem(new CorelAnnotatedImage(id, f, new File(metaDir, id+"_1.txt")));
			}
		}
	}

	public static class HistogramExtractor implements FeatureExtractor<DoubleFV, ImageWrapper> {
		Map<String, DoubleFV> data = new HashMap<String, DoubleFV>();

		public HistogramExtractor() throws IOException {
			BufferedReader br = new BufferedReader(new FileReader("/Users/jsh2/Data/corel-5k/BLOBS_data.txt"));
			String line;
			while ((line = br.readLine()) != null) {
				Scanner sc = new Scanner(line);

				String id = sc.nextInt() + "";
				double[] vec = new double[500];

				while (sc.hasNext()) {
					String token = sc.next();
					double weight = Double.parseDouble(sc.next().replace(",", ""));

					if (token.startsWith("blob")) {
						int blobId = Integer.parseInt(token.replace("blob[", "").replace("]", ""));
						vec[blobId - 1] += weight;
					}
				}

				data.put(id, new DoubleFV(vec));
			}
			br.close();
		}

		@Override
		public DoubleFV extractFeature(ImageWrapper object) {
			//HistogramModel hm = new HistogramModel(4,4,4);
			//hm.estimateModel(object.getImage());
			//return hm.histogram;
			return data.get(object.getID());
		}
	}

	public static void main(String[] args) throws IOException {
		Corel5kDataset alldata = new Corel5kDataset();

		StandardCorel5kSplit split = new StandardCorel5kSplit();
		split.split(alldata);

		ListDataset<CorelAnnotatedImage> training = split.getTrainingDataset();

		UniformRandomAnnotator<ImageWrapper, String> ann = new UniformRandomAnnotator<ImageWrapper, String>(new PriorChooser());
		//		DenseLinearTransformAnnotator<ImageWrapper, String, HistogramExtractor> ann = new DenseLinearTransformAnnotator<ImageWrapper, String, HistogramExtractor>(315, new HistogramExtractor());
		ann.train(training);

		//		for (CorelAnnotatedImage img : split.getTestDataset()) {
		//			List<AutoAnnotation<String>> anns = ann.annotate(img.getObject());
		//			MBFImage imgf = img.getObject();
		//			imgf.processInplace(new ResizeProcessor(400, 400));
		//			imgf.drawText(anns.get(0).toString(), 20, 20, HersheyFont.TIMES_BOLD,20);
		//			DisplayUtilities.display(imgf);
		//		}
		
		
		AnnotationEvaluator<ImageWrapper, String> eval = new AnnotationEvaluator<ImageWrapper, String>(ann, split.getTestDataset());
		
//		ClassificationEvaluator<ROCAnalysisResult<String>, String, ImageWrapper> classEval = eval.newClassificationEvaluator(new ROCAnalyser<ImageWrapper, String>());
//		Map<ImageWrapper, ClassificationResult<String>> classRes = classEval.evaluate();
//		ROCAnalysisResult<String> classAnalysis = classEval.analyse(classRes);
//		System.out.println(classAnalysis);
		
		RetrievalEvaluator<IREvalResult, ImageWrapper, String> retEval = eval.newRetrievalEvaluator(new IREvalAnalyser<String, ImageWrapper>());
		Map<String, List<ImageWrapper>> retRes = retEval.evaluate();
		IREvalResult retAnalysis = retEval.analyse(retRes);
		System.out.println(retAnalysis);
	}
}
