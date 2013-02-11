import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;

import org.openimaj.feature.FeatureExtractor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.FImage;
import org.openimaj.io.IOUtils;
import org.openimaj.math.geometry.point.Point2d;
import org.openimaj.math.geometry.point.Point2dImpl;
import org.openimaj.ml.annotation.bayes.NaiveBayesAnnotator;
import org.openimaj.video.Video;
import org.openimaj.video.capture.VideoCapture;
import org.openimaj.video.processing.motion.GridMotionEstimator;
import org.openimaj.video.processing.motion.MotionEstimator;
import org.openimaj.video.processing.motion.MotionEstimatorAlgorithm;
import org.openimaj.video.translator.MBFImageToFImageVideoTranslator;


public class OpticFlowPlayground {
	public static Direction direction = Direction.NONE;
	public static void main(String[] args) throws FileNotFoundException, IOException {
//		Video<FImage> capture = new MBFImageToFImageVideoTranslator(new VideoCapture(160,120));
		Video<FImage> capture = new MBFImageToFImageVideoTranslator(new VideoCapture(640,480));
		MotionEstimatorAlgorithm.TEMPLATE_MATCH alg = new MotionEstimatorAlgorithm.TEMPLATE_MATCH();
		MotionEstimator e = new GridMotionEstimator(capture,
//				new MotionEstimatorAlgorithm.PHASE_CORRELATION(),
				alg,
				30, 30,true);
		NaiveBayesAnnotator<Double, Direction, FeatureExtractor<? extends FeatureVector,Double>> dirAnn
			= IOUtils.read(new DataInputStream(new FileInputStream("/Users/ss/.rhino/opticflowann")));

		boolean first = true;
		for (FImage fImage : e) {
			if(first){
				first = false;
				continue;
			}
			Point2dImpl meanMotion = new Point2dImpl(0,0);
			Map<Point2d, Point2d> analysis = e.getMotionVectors();
			for (Entry<Point2d, Point2d> line : analysis.entrySet()) {
				Point2d to = line.getKey().copy();
				to.translate(line.getValue());
				fImage.drawLine(line.getKey(), to, 1f);
				fImage.drawPoint(line.getKey(),1f,3);
				meanMotion.x += line.getValue().getX();
			}
			meanMotion.x /= analysis.size();
			meanMotion.y /= analysis.size();
			JFrame f = DisplayUtilities.displayName(fImage,"frame");

			f.addKeyListener(new KeyAdapter() {
				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyChar() == 'l'){
						direction = Direction.LEFT;
					}
					else if(e.getKeyChar() == 'r'){
						direction = Direction.RIGHT;
					}
					else if(e.getKeyCode() == KeyEvent.VK_SPACE){
						direction = Direction.MIDDLE;
					}
				}
				@Override
				public void keyReleased(KeyEvent e) {
					direction = Direction.NONE;
				}
			});
			if(!(direction == Direction.NONE)){
				System.out.println(String.format("x: %2.2f,%s",meanMotion.x,direction));
				dirAnn.train(new DirectionScore(meanMotion.x, direction));
			}else{
				Iterator<Direction> iterator = dirAnn.classify((double)meanMotion.x).getPredictedClasses().iterator();
				if(iterator.hasNext()){
					Direction dir = iterator.next();
//					System.out.println("Current flow: " + dir);
				}
			}
		}
	}
}
