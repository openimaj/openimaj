package org.openimaj.ml.linear.learner;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * @author Jonathan Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk), David Duplaw (dpd@ecs.soton.ac.uk)
 *
 */
public class IncrementalBilinearSparseOnlineLearner implements OnlineLearner<Map<String, Map<String, Double>>,Map<String,Double>>{
	static class IncrementalBilinearSparseOnlineLearnerParams extends BilinearLearnerParameters{

	/**
	 *
	 */
	private static final long serialVersionUID = -1847045895118918210L;

	}
	private BiMap<String, Integer> vocabulary;
	private BiMap<String, Integer> users;
	private BiMap<String, Integer> values;
	private BilinearSparseOnlineLearner bilinearLearner;
	private BilinearLearnerParameters params;

	/**
	 * Instantiates with the default params
	 */
	public IncrementalBilinearSparseOnlineLearner() {
		init(new IncrementalBilinearSparseOnlineLearnerParams());
	}

	/**
	 * @param params
	 */
	public IncrementalBilinearSparseOnlineLearner(BilinearLearnerParameters params){
		init(params);
	}

	/**
	 *
	 */
	public void reinitParams() {
		init(this.params);

	}

	private void init(BilinearLearnerParameters params) {
		vocabulary = HashBiMap.create();
		users = HashBiMap.create();
		values = HashBiMap.create();
		this.params = params;
		bilinearLearner = new BilinearSparseOnlineLearner(params);
	}

	/**
	 * @return the current parameters
	 */
	public BilinearLearnerParameters getParams(){
		return this.params;
	}

	@Override
	public void process(Map<String, Map<String, Double>> x, Map<String, Double> y) {
		updateUserValues(x,y);
		Matrix yMat = constructYMatrix(y);
		Matrix xMat = constructXMatrix(x);

		this.bilinearLearner.process(xMat, yMat);
	}

	/**
	 * Update the incremental learner and underlying weight matricies to reflect potentially novel
	 * users, words and values to learn against
	 * @param x
	 * @param y
	 */
	public void updateUserValues(Map<String, Map<String, Double>> x,Map<String, Double> y) {
		updateUserWords(x);
		updateValues(y);
	}

	private void updateValues(Map<String, Double> y) {
		for (String value : y.keySet()) {
			if(!values.containsKey(value)){
				values.put(value,values.size());
			}
		}
	}

	private Matrix constructYMatrix(Map<String, Double> y) {
		Matrix mat = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(1, values.size());
		for (Entry<String, Double> ent : y.entrySet()) {
			mat.setElement(0, values.get(ent.getKey()), ent.getValue());
		}
		return mat;
	}

	private Map<String,Double> constructYMap(Matrix y){
	 	Map<String, Double> ret = new HashMap<String, Double>();
		for (String key : values.keySet()) {
			Integer index = values.get(key);
			double yvalue = y.getElement(0, index);
			ret.put(key, yvalue);
		}
		return ret;
	}

	private Matrix constructXMatrix(Map<String,Map<String, Double>> x) {
		Matrix mat = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(vocabulary.size(), users.size());
		for (Entry<String, Map<String, Double>> userwords : x.entrySet()) {
			int userindex = this.users.get(userwords.getKey());
			for (Entry<String, Double> ent : userwords.getValue().entrySet()) {
				mat.setElement(vocabulary.get(ent.getKey()), userindex, ent.getValue());
			}
		}
		return mat;
	}

	private void updateUserWords(Map<String, Map<String, Double>> x) {
		int newUsers = 0;
		int newWords = 0;
		for (Entry<String, Map<String, Double>> userWords: x.entrySet()) {
			String user = userWords.getKey();
			if(!users.containsKey(user)){
				users.put(user, users.size());
				newUsers++;
			}
			newWords += updateWords(userWords.getValue());
		}

		this.bilinearLearner.addU(newUsers);
		this.bilinearLearner.addW(newWords);
	}

	private int updateWords(Map<String, Double> value) {
		int newWords = 0;
		for (String word : value.keySet()) {
			if(!vocabulary.containsKey(word)){
				vocabulary.put(word, vocabulary.size());
				newWords++;
			}
		}
		return newWords;
	}

	/**
	 * Construct a learner with the desired number of users and words.
	 * If users and words beyond those in the current model are asked for their parameters
	 * are set to 0
	 * @param nusers
	 * @param nwords
	 * @return a new {@link BilinearSparseOnlineLearner}
	 */
	public BilinearSparseOnlineLearner getBilinearLearner(int nusers, int nwords) {
		BilinearSparseOnlineLearner ret = this.bilinearLearner.clone();

		Matrix u = ret.getU();
		Matrix w = ret.getW();
		Matrix newu = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(nusers, u.getNumColumns());
		Matrix neww = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(nwords, w.getNumColumns());

		newu.setSubMatrix(0, 0, u);
		neww.setSubMatrix(0, 0, w);

		ret.setU(newu);
		ret.setW(neww);
		return ret;
	}

	/**
	 * @return the underlying {@link BilinearSparseOnlineLearner} with the current number of users and words
	 */
	public BilinearSparseOnlineLearner getBilinearLearner() {
		return this.bilinearLearner.clone();
	}

	/**
	 * Given a sparse pair of user/words and value construct a pair of matricies
	 * using the current mappings of words and users to matrix rows.
	 *
	 * Note: if users or words which have not yet be
	 * @param xy
	 * @param nfeatures the number of words total in the returned X matrix
	 * @param nusers the number of users total in the returned X matrix
	 * @param ntasks the number of tasks in the returned Y matrix
	 * @return the matrix pair representing the X and Y input
	 */
	public Pair<Matrix> asMatrixPair(
			IndependentPair<Map<String, Map<String, Double>>, Map<String, Double>> xy,
			int nfeatures, int nusers, int ntasks) {
		Matrix y = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(1, ntasks);
		Matrix x = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(nfeatures, nusers);
		Map<String, Double> ymap = xy.secondObject();
		Map<String, Map<String, Double>> userFeatureMap = xy.firstObject();
		for (Entry<String, Double> yent : ymap.entrySet()) {
			y.setElement(0, this.values.get(yent.getKey()), yent.getValue());
		}
		for (Entry<String, Map<String, Double>> xent : userFeatureMap.entrySet()) {
			int userind = this.users.get(xent.getKey());
			for (Entry<String, Double> fent : xent.getValue().entrySet()) {
				x.setElement(this.vocabulary.get(fent.getKey()), userind, fent.getValue());
			}
		}
		return new Pair<Matrix>(x, y);
	}

	@Override
	public Map<String, Double> predict(Map<String, Map<String, Double>> x) {
		Matrix xMat = constructXMatrix(x);
		Matrix yMat = this.bilinearLearner.predict(xMat);
		return this.constructYMap(yMat);
	}

	/**
	 * @return the vocabulary
	 */
	public BiMap<String, Integer> getVocabulary() {
		return vocabulary;
	}

	/**
	 * @param in
	 * @return calls {@link #asMatrixPair(IndependentPair, int, int, int)} with the current number of words, users and value
	 */
	public Pair<Matrix> asMatrixPair(IndependentPair<Map<String, Map<String, Double>>, Map<String, Double>> in) {
		return this.asMatrixPair(in, this.vocabulary.size(), this.users.size(), this.values.size());
	}

	/**
	 * @return the current map of dependant values to indexes
	 */
	public BiMap<String, Integer> getDependantValues() {
		return this.values;
	}

	public BiMap<String,Integer> getUsers() {
		return this.users;
	}




}
