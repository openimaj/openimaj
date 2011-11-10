package org.openimaj.image.processing.face.recognition;

import gov.sandia.cognition.io.ObjectSerializationHandler;
import gov.sandia.cognition.learning.algorithm.bayes.VectorNaiveBayesCategorizer;
import gov.sandia.cognition.learning.data.DefaultInputOutputPair;
import gov.sandia.cognition.math.matrix.Vector;
import gov.sandia.cognition.math.matrix.VectorFactory;
import gov.sandia.cognition.math.matrix.Vectorizable;
import gov.sandia.cognition.statistics.distribution.UnivariateGaussian;
import gov.sandia.cognition.statistics.distribution.UnivariateGaussian.PDF;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectOutputStream;
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

import com.sun.corba.se.impl.orbutil.ObjectWriter;

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
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void readBinary(DataInput in) throws IOException {
		// read the factory
		String factoryClass = in.readUTF();
		factory = IOUtils.newInstance(factoryClass);
		factory.readBinary(in);
		
		// read the categorizer
		int arrLen = in.readInt();
		byte[] arr = new byte[arrLen];
		in.readFully(arr);
		try {
			this.categorizer = (VectorNaiveBayesCategorizer<String, PDF>) ObjectSerializationHandler.convertFromBytes(arr);
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
		byte[] arr = ObjectSerializationHandler.convertToBytes(this.categorizer);
		out.writeInt(arr.length);
		out.write(arr);
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
			FaceMatchResult result = new FaceMatchResult(category,this.categorizer.computePosterior(vector, category));
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
