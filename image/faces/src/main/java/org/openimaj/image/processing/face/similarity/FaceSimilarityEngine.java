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
package org.openimaj.image.processing.face.similarity;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.image.Image;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.FaceDetector;
import org.openimaj.image.processing.face.feature.FacialFeature;
import org.openimaj.image.processing.face.feature.FacialFeatureExtractor;
import org.openimaj.image.processing.face.feature.comparison.FacialFeatureComparator;
import org.openimaj.math.geometry.shape.Rectangle;
import org.openimaj.math.matrix.similarity.SimilarityMatrix;
import org.openimaj.math.matrix.similarity.processor.InvertData;

/**
 * The {@link FaceSimilarityEngine} allows computation of the similarity
 * between faces in two images.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <D> The type of {@link DetectedFace} 
 * @param <F> the type of {@link FacialFeature} 
 * @param <I> The type of {@link Image}
 */
public class FaceSimilarityEngine<D extends DetectedFace, F extends FacialFeature, I extends Image<?, I>> {
	private FaceDetector<D, I> detector;
	private FacialFeatureExtractor<F, D> extractor;
	private FacialFeatureComparator<F> comparator;
	private Map<String, Rectangle> boundingBoxes;
	private Map<String, F> featureCache;
	private Map<String, List<D>> detectedFaceCache;
	private LinkedHashMap<String, Map<String, Double>> similarityMatrix;
	private List<D> queryfaces;
	private List<D> testfaces;
	private String queryId;
	private String testId;
	private boolean cache;

	/**
	 * Construct a new {@link FaceSimilarityEngine} from the
	 * specified detector, extractor and comparator.
	 * 
	 * @param detector The face detector
	 * @param extractor The feature extractor
	 * @param comparator The feature comparator 
	 */
	public FaceSimilarityEngine(FaceDetector<D, I> detector,
			FacialFeatureExtractor<F, D> extractor,
			FacialFeatureComparator<F> comparator) {
		this.detector = detector;
		this.extractor = extractor;
		this.comparator = comparator;
		this.similarityMatrix = new LinkedHashMap<String, Map<String,Double>>();
		this.boundingBoxes = new HashMap<String, Rectangle>();
		featureCache = new HashMap<String,F>();
		detectedFaceCache = new HashMap<String,List<D>>();
	}

	/**
	 * @return the detector
	 */
	public FaceDetector<D, I> detector() {
		return detector;
	}

	/**
	 * @return the featureFactory
	 */
	public FacialFeatureExtractor<F, D> extractor() {
		return extractor;
	}

	/**
	 * @return the comparator
	 */
	public FacialFeatureComparator<F> comparator() {
		return comparator;
	}

	/**
	 * Create a new {@link FaceSimilarityEngine} from the
	 * specified detector, extractor and comparator.
	 * 
	 * @param <D> The type of {@link DetectedFace} 
	 * @param <F> the type of {@link FacialFeature} 
	 * @param <I> The type of {@link Image}
	 * 
	 * @param detector The face detector
	 * @param extractor The feature extractor
	 * @param comparator The feature comparator 
	 * @return the new {@link FaceSimilarityEngine}
	 */
	public static <D extends DetectedFace, F extends FacialFeature, I extends Image<?, I>> 
		FaceSimilarityEngine<D, F, I> create(
			FaceDetector<D, I> detector,
			FacialFeatureExtractor<F, D> extractor,
			FacialFeatureComparator<F> comparator) 
	{
		return new FaceSimilarityEngine<D, F, I>(detector, extractor,
				comparator);
	}

	/**
	 * Set the query image.
	 * @param queryImage the query image
	 * @param queryId the identifier of the query image 
	 */
	public void setQuery(I queryImage, String queryId) {
		this.queryfaces = getDetectedFaces(queryId,queryImage);
		this.queryId = queryId;
		updateBoundingBox(this.queryfaces, queryId);
	}

	private List<D> getDetectedFaces(String faceId, I faceImage) {
		List<D> toRet = null;
		if(!this.cache){
			toRet = this.detector.detectFaces(faceImage);
		}
		else{
			toRet = this.detectedFaceCache.get(faceId);
			if(toRet == null){
//				System.out.println("Redetected face: " + faceId);
				toRet = this.detector.detectFaces(faceImage);;
				this.detectedFaceCache.put(faceId, toRet);
			}
		}
		return toRet;
	}

	private void updateBoundingBox(List<D> faces, String imageId) {
		// We need to store the first one if we're running withFirst = true
		if (boundingBoxes != null)
			for (int ff = 0; ff < faces.size(); ff++)
				if (boundingBoxes.get(imageId + ":" + ff) == null)
					boundingBoxes.put(imageId + ":" + ff, faces.get(ff)
							.getBounds());
	}

	/**
	 * Set the image against which the query will be compared to next
	 * 
	 * @param testImage
	 * @param testId
	 */
	public void setTest(I testImage, String testId) {
		this.testId = testId;
		this.testfaces = getDetectedFaces(testId,testImage);
		updateBoundingBox(this.testfaces, testId);
	}

	/**
	 * Compare the query to itself for the next test
	 */
	public void setQueryTest() {
		this.testfaces = this.queryfaces;
		this.testId = this.queryId;
	}

	/**
	 * Compute the similarities between faces in the query and target
	 */
	public void performTest() {
		// Now compare all the faces in the first image
		// with all the faces in the second image.
		for (int ii = 0; ii < queryfaces.size(); ii++) {
			String face1id = queryId + ":" + ii;
			D f1f = queryfaces.get(ii);
			
			F f1fv = getFeature(face1id, f1f);
			// 
			// NOTE that the distance matrix will be symmetrical
			// so we only have to do half the comparisons.
			for (int jj = 0; jj < testfaces.size(); jj++) {
				double d = 0;
				String face2id = null;

				// If we're comparing the same face in the same image
				// we can assume the distance is zero. Saves doing a match.
				if (queryfaces == testfaces && ii == jj) {
					d = 0;
					face2id = face1id;
				} else {
					// Compare the two feature vectors using the chosen
					// distance metric.
					D f2f = testfaces.get(jj);
					face2id = testId + ":" + jj;
					
					// F f2fv = featureFactory.createFeature(f2f, false);
					F f2fv = getFeature(face2id, f2f);

					d = comparator.compare(f1fv, f2fv);
				}

				// Put the result in the result map
				Map<String, Double> mm = this.similarityMatrix.get(face1id);
				if (mm == null)
					this.similarityMatrix.put(face1id, mm = new HashMap<String, Double>());
				mm.put(face2id, d);
			}
		}
	}

	private F getFeature(String id, D face) {
		F toRet = null;
		
		if (!cache) {
			toRet = extractor.extractFeature(face);
		} else {
			String combinedID = String.format("%s:%b", id);
			toRet = this.featureCache.get(combinedID);
			
			if(toRet == null){
				toRet = extractor.extractFeature(face);
				this.featureCache.put(combinedID, toRet);
			}
		}
		return toRet;
	}

	/**
	 * @return The similarity dictionary structured as: {image0:face0 => {image0:face0 => DISTANCE,...},...,}
	 */
	public Map<String, Map<String, Double>> getSimilarityDictionary() {
		return this.similarityMatrix;
	}
	
	/**
	 * Get the similarity matrix computed by {@link #performTest()}.
	 * @param invertIfRequired invert distances into similarities if required.
	 * @return the similarity matrix
	 */
	public SimilarityMatrix getSimilarityMatrix(boolean invertIfRequired) {
		Set<String> keys = this.similarityMatrix.keySet();
		String[] indexArr = keys.toArray(new String[keys.size()]);
		SimilarityMatrix simMatrix = new SimilarityMatrix(indexArr);
		for (int i = 0; i < indexArr.length; i++) {
			String x = indexArr[i];
			for (int j = 0; j < indexArr.length; j++) {
				String y = indexArr[j];
				simMatrix.set(i, j, this.similarityMatrix.get(x).get(y));
			}
		}
		
		if(this.comparator.isDistance() && invertIfRequired) {
			simMatrix.processInplace(new InvertData());
		}
		return simMatrix;
	}

	/**
	 * @return the bounding boxes of the detected faces 
	 */
	public Map<String,Rectangle> getBoundingBoxes() {
		return this.boundingBoxes;
	}

	/**
	 * Set whether detections should be cached
	 * @param cache enable cache if true
	 */
	public void setCache(boolean cache) {
		this.cache = cache;
	}
}
