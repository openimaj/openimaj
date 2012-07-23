package com.jsaragih;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import javax.xml.stream.XMLStreamException;

import org.openimaj.image.FImage;
import org.openimaj.image.processing.algorithm.EqualisationProcessor;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector.BuiltInCascade;
import org.openimaj.image.processing.haar.Cascades;
import org.openimaj.image.processing.haar.ClassifierCascade;
import org.openimaj.image.processing.haar.GroupingPolicy;
import org.openimaj.image.processing.haar.MultiscaleDetection;
import org.openimaj.image.processing.haar.ObjectDetector;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;

/**
 * Face detector.
 * <p>
 * Note: the face detector in any input file is ignored and is replaced by the
 * {@link BuiltInCascade#frontalface_alt2} cascade.
 * 
 * @author Jason Mora Saragih
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FDet {
	private static final int CV_HAAR_FEATURE_MAX = 3;

	int _min_neighbours;
	int _min_size;
	double _img_scale;
	double _scale_factor;
	ClassifierCascade _cascade;

	FImage small_img_;

	FDet(final String fname, final double img_scale, final double scale_factor,
			final int min_neighbours, final int min_size) throws IOException,
			XMLStreamException {
		FileInputStream fis = new FileInputStream(fname);
		_cascade = Cascades.readFromXML(fis);
		fis.close();

		_img_scale = img_scale;
		_scale_factor = scale_factor;
		_min_neighbours = min_neighbours;
		_min_size = min_size;
	}

	FDet() {
		_cascade = HaarCascadeDetector.BuiltInCascade.frontalface_alt2.load()
				.getCascade();
	}

	/**
	 * Detect faces in an image
	 * 
	 * @param im the image
	 * @return the detected faces
	 */
	public List<Rectangle> detect(FImage im) {
		final int w = (int) Math.round(im.width / _img_scale);
		final int h = (int) Math.round(im.height / _img_scale);

		small_img_ = ResizeProcessor.resample(im, w, h).processInplace(
				new EqualisationProcessor());

		ObjectDetector detector = new MultiscaleDetection(_cascade,
				(float) _scale_factor);
		GroupingPolicy groupingPolicy = new GroupingPolicy(_min_neighbours);
		List<Rectangle> rects = detector.detectObjects(small_img_, _min_size);
		rects = groupingPolicy.reduceAreas(rects);
		for (Rectangle r : rects)
			r.scale((float) _img_scale);

		return rects;
	}

	static FDet load(final String fname) throws FileNotFoundException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
			Scanner sc = new Scanner(br);
			return read(sc, true);
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
	}

	void save(final String fname) throws IOException {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fname));

			write(bw);
		} finally {
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
			}
		}
	}

	void write(BufferedWriter s) {
		// _cascade.
		// int i,j,k,l;
		// s.write(
		// IO.Types.FDET.ordinal() + " "
		// + _min_neighbours + " "
		// + _min_size + " "
		// + _img_scale + " "
		// + _scale_factor + " "
		// + _cascade.count + " "
		// + _cascade.orig_window_size.width + " "
		// + _cascade.orig_window_size.height + " "
		// );
		//
		// for(i = 0; i < _cascade->count; i++){
		// s + _cascade->stage_classifier[i].parent + " "
		// + _cascade->stage_classifier[i].next + " "
		// + _cascade->stage_classifier[i].child + " "
		// + _cascade->stage_classifier[i].threshold + " "
		// + _cascade->stage_classifier[i].count + " ";
		// for(j = 0; j < _cascade->stage_classifier[i].count; j++){
		// CvHaarClassifier* classifier =
		// &_cascade->stage_classifier[i].classifier[j];
		// s + classifier->count + " ";
		// for(k = 0; k < classifier->count; k++){
		// s + classifier->threshold[k] + " "
		// + classifier->left[k] + " "
		// + classifier->right[k] + " "
		// + classifier->alpha[k] + " "
		// + classifier->haar_feature[k].tilted + " ";
		// for(l = 0; l < CV_HAAR_FEATURE_MAX; l++){
		// s + classifier->haar_feature[k].rect[l].weight + " "
		// + classifier->haar_feature[k].rect[l].r.x + " "
		// + classifier->haar_feature[k].rect[l].r.y + " "
		// + classifier->haar_feature[k].rect[l].r.width + " "
		// + classifier->haar_feature[k].rect[l].r.height + " ";
		// }
		// }
		// s + classifier->alpha[classifier->count] + " ";
		// }
		// }
	}

	/**
	 * Read the Face detector.
	 * @param s
	 * @param readType
	 * @return the face detector
	 */
	public static FDet read(Scanner s, boolean readType) {
		// FIXME: maybe this should actually read the cascade!!
		if (readType) {
			int type = s.nextInt();
			assert (type == IO.Types.FDET.ordinal());
		}

		FDet fdet = new FDet();
		fdet._min_neighbours = s.nextInt();
		fdet._min_size = s.nextInt();
		fdet._img_scale = s.nextDouble();
		fdet._scale_factor = s.nextDouble();
		int n = s.nextInt();

		// m = sizeof(CvHaarClassifierCascade)+n*sizeof(CvHaarStageClassifier);
		// _cascade = (CvHaarClassifierCascade*)cvAlloc(m);
		// memset(_cascade,0,m);
		// _cascade->stage_classifier = (CvHaarStageClassifier*)(_cascade + 1);
		// _cascade->flags = CV_HAAR_MAGIC_VAL;
		// _cascade->count = n;

		// s >> _cascade->orig_window_size.width >>
		// _cascade->orig_window_size.height;
		s.next();
		s.next();

		for (int i = 0; i < n; i++) {
			// s >> _cascade->stage_classifier[i].parent
			s.next();
			// >> _cascade->stage_classifier[i].next
			s.next();
			// >> _cascade->stage_classifier[i].child
			s.next();
			// >> _cascade->stage_classifier[i].threshold
			s.next();
			// >> _cascade->stage_classifier[i].count;
			int count = s.nextInt();

			// _cascade->stage_classifier[i].classifier =
			// (CvHaarClassifier*)cvAlloc(_cascade->stage_classifier[i].count*
			// sizeof(CvHaarClassifier));
			for (int j = 0; j < count; j++) {
				// CvHaarClassifier* classifier =
				// &_cascade->stage_classifier[i].classifier[j];
				// s >> classifier->count;
				int ccount = s.nextInt();

				// classifier->haar_feature = (CvHaarFeature*)
				// cvAlloc(classifier->count*(sizeof(CvHaarFeature) +
				// sizeof(float) + sizeof(int) + sizeof(int)) +
				// (classifier->count+1)*sizeof(float));
				// classifier->threshold =
				// (float*)(classifier->haar_feature+classifier->count);
				// classifier->left = (int*)(classifier->threshold +
				// classifier->count);
				// classifier->right = (int*)(classifier->left +
				// classifier->count);
				// classifier->alpha = (float*)(classifier->right +
				// classifier->count);
				for (int k = 0; k < ccount; k++) {
					// s >> classifier->threshold[k]
					s.next();
					// >> classifier->left[k]
					s.next();
					// >> classifier->right[k]
					s.next();
					// >> classifier->alpha[k]
					s.next();
					// >> classifier->haar_feature[k].tilted;
					s.next();
					for (int l = 0; l < CV_HAAR_FEATURE_MAX; l++) {
						// s >> classifier->haar_feature[k].rect[l].weight
						s.next();
						// >> classifier->haar_feature[k].rect[l].r.x
						s.next();
						// >> classifier->haar_feature[k].rect[l].r.y
						s.next();
						// >> classifier->haar_feature[k].rect[l].r.width
						s.next();
						// >> classifier->haar_feature[k].rect[l].r.height;
						s.next();
					}
				}
				// s >> classifier->alpha[classifier->count];
				s.next();
			}
		}

		return fdet;
	}
}
