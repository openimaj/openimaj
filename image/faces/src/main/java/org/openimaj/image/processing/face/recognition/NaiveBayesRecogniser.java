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
package org.openimaj.image.processing.face.recognition;

import gov.sandia.cognition.io.ObjectSerializationHandler;
import gov.sandia.cognition.learning.algorithm.bayes.VectorNaiveBayesCategorizer;
import gov.sandia.cognition.learning.data.DefaultInputOutputPair;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.math.matrix.Vectorizable;
import gov.sandia.cognition.statistics.distribution.UnivariateGaussian;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.feature.FacialFeature;
import org.openimaj.image.processing.face.feature.FacialFeatureFactory;
import org.openimaj.io.IOUtils;

public class NaiveBayesRecogniser<
				F extends FacialFeature & FeatureVectorProvider<FloatFV>, 
				D extends DetectedFace
	> implements FaceRecogniser<D>
{
	private static float PERTRUBATION = 0.00001f;
	private Random random = new Random();
	protected FacialFeatureFactory<F, D> factory;
	protected List<DefaultInputOutputPair<? extends Vectorizable,String>> database;
	private VectorNaiveBayesCategorizer<String, UnivariateGaussian.PDF> categorizer;
	private VectorNaiveBayesCategorizer.BatchGaussianLearner<String> learner;
	private VectorFactory<? extends Vector> vectorFactory;
	
	
	NaiveBayesRecogniser(){
		this.vectorFactory = VectorFactory.getDefault();
		this.reset();
	}
	public NaiveBayesRecogniser(FacialFeatureFactory<F,D> factory){
		this();
		this.factory = factory;
		
	}
	
	
	@Override
	public void readBinary(DataInput in) throws IOException {
		// read the factory
		String factoryClass = in.readUTF();
		factory = IOUtils.newInstance(factoryClass);
		factory.readBinary(in);
		
		// read the categorizer
		int arrLen = 0;
		byte[] arr = null;
//		arrLen = in.readInt();
//		arr = new byte[arrLen];
//		in.readFully(arr);
		try {
//			this.categorizer = (VectorNaiveBayesCategorizer<String, PDF>) ObjectSerializationHandler.convertFromBytes(arr);
			// read current instances
			int ndb = in.readInt();
			for(int i = 0; i < ndb; i++){
				String ident = in.readUTF();
				arrLen = in.readInt();
				arr = new byte[arrLen];
				in.readFully(arr);
				Vector v = (Vector) ObjectSerializationHandler.convertFromBytes(arr);
				DefaultInputOutputPair<? extends Vectorizable, String> pair = new DefaultInputOutputPair<Vector,String>(v,ident);
				this.database.add(pair);
			}
			this.train();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public byte[] binaryHeader() {
		return "NBFaceRec".getBytes();
	}

	@Override
	public void writeBinary(DataOutput out) throws IOException {
		out.writeUTF(factory.getClass().getName());
		factory.writeBinary(out);
		
//		
		byte[] arr = null;
//		arr = ObjectSerializationHandler.convertToBytes(this.categorizer);
//		out.writeInt(arr.length);
//		out.write(arr);
//		
		
		out.writeInt(database.size());
		for (DefaultInputOutputPair<? extends Vectorizable,String> item : this.database) {
			out.writeUTF(item.getOutput());
			arr = ObjectSerializationHandler.convertToBytes(item.getInput());
			out.writeInt(arr.length);
			out.write(arr);
		}
	}

	@Override
	public void addInstance(String identifier, D face) {
		F feature = factory.createFeature(face, false);
		double[] dVec = feature.getFeatureVector().asDoubleVector();
		for (int i = 0; i < dVec.length; i++) {
			dVec[i] += ((random.nextFloat() - 0.5f) * PERTRUBATION);
			
		}
		Vector vector = this.vectorFactory.copyArray(dVec);
		DefaultInputOutputPair<? extends Vectorizable, String> pair = new DefaultInputOutputPair<Vector,String>(vector,identifier);
		this.database.add(pair);
	}

	@Override
	public void train() {
		this.categorizer = learner.learn(database);
	}

	@Override
	public List<FaceMatchResult> query(D face) {
		return query(face,this.categorizer.getCategories());
	}

	@Override
	public FaceMatchResult queryBestMatch(D face) {
		return query(face).get(0);
	}

	@Override
	public List<FaceMatchResult> query(D face, Collection<String> restrict) {
		F feature = factory.createFeature(face, true);
		Vector vector = this.vectorFactory.copyArray(feature.getFeatureVector().asDoubleVector());
		List<FaceMatchResult> results = new ArrayList<FaceMatchResult>();
		for(String category : restrict){
			FaceMatchResult result = new FaceMatchResult(category,-1 * this.categorizer.computeLogPosterior(vector, category));
			results.add(result);
		}
		Collections.sort(results);
		return results;
	}

	@Override
	public FaceMatchResult queryBestMatch(D face, Collection<String> restrict) {
		return query(face,restrict).get(0);
	}

	@Override
	public void reset() {
		this.categorizer = new VectorNaiveBayesCategorizer<String,UnivariateGaussian.PDF>();
		this.learner = new VectorNaiveBayesCategorizer.BatchGaussianLearner<String>();
		this.database = new ArrayList<DefaultInputOutputPair<? extends Vectorizable,String>>();
	}

	@Override
	public List<String> listPeople() {
		return new ArrayList<String>(this.categorizer.getCategories());
	}

}
