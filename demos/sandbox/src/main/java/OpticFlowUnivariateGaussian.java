import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openimaj.feature.DoubleFV;
import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.io.FileUtils;
import org.openimaj.io.IOUtils;
import org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator;




public class OpticFlowUnivariateGaussian {
	public static void main(String[] args) throws IOException {
		FeatureExtractor<? extends FeatureVector, Double> extractor = new FeatureExtractor<FeatureVector, Double>() {

			@Override
			public FeatureVector extractFeature(Double object) {
				return new DoubleFV(new double[]{object});
			}
		};
		NaiveBayesAnnotator<Double, Direction, FeatureExtractor<? extends FeatureVector,Double>> ann
			= new NaiveBayesAnnotator<Double, Direction, FeatureExtractor<? extends FeatureVector,Double>>(extractor, NaiveBayesAnnotator.Mode.ALL);
		String[] lines = FileUtils.readlines(OpticFlowUnivariateGaussian.class.getResourceAsStream("directions"));
		for (String line : lines) {
			String[] scoreDir = line.split(",");
			double score = Double.parseDouble(scoreDir[0]);
			Direction dir = Direction.valueOf(scoreDir[1]);
			ann.train(new DirectionScore(score, dir));
		}
		IOUtils.write(ann, new DataOutputStream(new FileOutputStream("/Users/ss/.rhino/opticflowann")));
		ann = IOUtils.read(new DataInputStream(new FileInputStream("/Users/ss/.rhino/opticflowann")));
	}
}
