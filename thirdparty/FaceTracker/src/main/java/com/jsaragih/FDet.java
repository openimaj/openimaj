/**
 * FaceTracker Licence
 * -------------------
 * (Academic, non-commercial, not-for-profit licence)
 *
 * Copyright (c) 2010 Jason Mora Saragih
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * The software is provided under the terms of this licence stricly for
 *       academic, non-commercial, not-for-profit purposes.
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions (licence) and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions (licence) and the following disclaimer
 *       in the documentation and/or other materials provided with the
 *       distribution.
 *     * The name of the author may not be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *     * As this software depends on other libraries, the user must adhere to and
 *       keep in place any licencing terms of those libraries.
 *     * Any publications arising from the use of this software, including but
 *       not limited to academic journal and conference publications, technical
 *       reports and manuals, must cite the following work:
 *
 *       J. M. Saragih, S. Lucey, and J. F. Cohn. Face Alignment through Subspace
 *       Constrained Mean-Shifts. International Journal of Computer Vision
 *       (ICCV), September, 2009.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
import org.openimaj.image.objectdetection.filtering.OpenCVGrouping;
import org.openimaj.image.objectdetection.haar.Detector;
import org.openimaj.image.objectdetection.haar.OCVHaarLoader;
import org.openimaj.image.objectdetection.haar.StageTreeClassifier;
import org.openimaj.image.processing.algorithm.EqualisationProcessor;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.util.pair.ObjectIntPair;

/**
 * Face detector.
 * <p>
 * Note: the face detector in any input file is ignored and is replaced by the
 * haarcascade_frontalface_alt2 cascade.
 *
 * @author Jason Mora Saragih
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 */
public class FDet {
	static {
		Tracker.init();
	}

	private static final int CV_HAAR_FEATURE_MAX = 3;

	int _min_neighbours;
	int _min_size;
	float _img_scale;
	float _scale_factor;
	StageTreeClassifier _cascade;

	FImage small_img_;

	private Detector detector;

	private OpenCVGrouping grouping;

	FDet(final String fname, final float img_scale, final float scale_factor,
			final int min_neighbours, final int min_size) throws IOException,
			XMLStreamException
	{
		final FileInputStream fis = new FileInputStream(fname);
		this._cascade = OCVHaarLoader.read(fis);
		fis.close();

		this._img_scale = img_scale;
		this._scale_factor = scale_factor;
		this._min_neighbours = min_neighbours;
		this._min_size = min_size;

		this.setupDetector();
	}

	FDet() {
		try {
			this._cascade = OCVHaarLoader.read(OCVHaarLoader.class.getResourceAsStream("haarcascade_frontalface_alt2.xml"));
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Detect faces in an image
	 *
	 * @param im
	 *            the image
	 * @return the detected faces
	 */
	public List<Rectangle> detect(final FImage im) {
		final int w = Math.round(im.width / this._img_scale);
		final int h = Math.round(im.height / this._img_scale);

		this.small_img_ = ResizeProcessor.resample(im, w, h).processInplace(
				new EqualisationProcessor());

		List<Rectangle> rects = this.detector.detect(this.small_img_);
		rects = ObjectIntPair.getFirst(this.grouping.apply(rects));
		for (final Rectangle r : rects) {
			r.scale(this._img_scale);
		}

		return rects;
	}

	private void setupDetector() {
		this.detector = new Detector(this._cascade, this._scale_factor);
		this.grouping = new OpenCVGrouping(this._min_neighbours);
		this.detector.setMinimumDetectionSize(this._min_size);
	}

	static FDet load(final String fname) throws FileNotFoundException {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fname));
			final Scanner sc = new Scanner(br);
			return FDet.read(sc, true);
		} finally {
			try {
				br.close();
			} catch (final IOException e) {
			}
		}
	}

	void save(final String fname) throws IOException {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fname));

			this.write(bw);
		} finally {
			try {
				if (bw != null)
					bw.close();
			} catch (final IOException e) {
			}
		}
	}

	void write(final BufferedWriter s) {
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
	 *
	 * @param s
	 * @param readType
	 * @return the face detector
	 */
	public static FDet read(final Scanner s, final boolean readType) {
		// FIXME: maybe this should actually read the cascade!!
		if (readType) {
			final int type = s.nextInt();
			assert (type == IO.Types.FDET.ordinal());
		}

		final FDet fdet = new FDet();
		fdet._min_neighbours = s.nextInt();
		fdet._min_size = s.nextInt();
		fdet._img_scale = s.nextFloat();
		fdet._scale_factor = s.nextFloat();
		final int n = s.nextInt();

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
			final int count = s.nextInt();

			// _cascade->stage_classifier[i].classifier =
			// (CvHaarClassifier*)cvAlloc(_cascade->stage_classifier[i].count*
			// sizeof(CvHaarClassifier));
			for (int j = 0; j < count; j++) {
				// CvHaarClassifier* classifier =
				// &_cascade->stage_classifier[i].classifier[j];
				// s >> classifier->count;
				final int ccount = s.nextInt();

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
					for (int l = 0; l < FDet.CV_HAAR_FEATURE_MAX; l++) {
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

		fdet.setupDetector();

		return fdet;
	}

	/**
	 *	@return the _min_size
	 */
	public int get_min_size()
	{
		return this._min_size;
	}

	/**
	 *	@param _min_size the _min_size to set
	 */
	public void set_min_size( final int _min_size )
	{
		this._min_size = _min_size;
		this.setupDetector();
	}
}
