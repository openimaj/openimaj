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
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public class IncrementalBilinearSparseOnlineLearner
		implements
		OnlineLearner<Map<String, Map<String, Double>>, Map<String, Double>>
{
	static class IncrementalBilinearSparseOnlineLearnerParams extends BilinearLearnerParameters {

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
	public IncrementalBilinearSparseOnlineLearner(BilinearLearnerParameters params) {
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
	public BilinearLearnerParameters getParams() {
		return this.params;
	}

	@Override
	public void process(Map<String, Map<String, Double>> x, Map<String, Double> y) {
		updateUserValues(x, y);
		final Matrix yMat = constructYMatrix(y);
		final Matrix xMat = constructXMatrix(x);

		this.bilinearLearner.process(xMat, yMat);
	}

	/**
	 * Update the incremental learner and underlying weight matricies to reflect
	 * potentially novel users, words and values to learn against
	 * 
	 * @param x
	 * @param y
	 */
	public void updateUserValues(Map<String, Map<String, Double>> x, Map<String, Double> y) {
		updateUserWords(x);
		updateValues(y);
	}

	private void updateValues(Map<String, Double> y) {
		for (final String value : y.keySet()) {
			if (!values.containsKey(value)) {
				values.put(value, values.size());
			}
		}
	}

	private Matrix constructYMatrix(Map<String, Double> y) {
		final Matrix mat = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(1, values.size());
		for (final Entry<String, Double> ent : y.entrySet()) {
			mat.setElement(0, values.get(ent.getKey()), ent.getValue());
		}
		return mat;
	}

	private Map<String, Double> constructYMap(Matrix y) {
		final Map<String, Double> ret = new HashMap<String, Double>();
		for (final String key : values.keySet()) {
			final Integer index = values.get(key);
			final double yvalue = y.getElement(0, index);
			ret.put(key, yvalue);
		}
		return ret;
	}

	private Matrix constructXMatrix(Map<String, Map<String, Double>> x) {
		final Matrix mat = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(vocabulary.size(), users.size());
		for (final Entry<String, Map<String, Double>> userwords : x.entrySet()) {
			final int userindex = this.users.get(userwords.getKey());
			for (final Entry<String, Double> ent : userwords.getValue().entrySet()) {
				mat.setElement(vocabulary.get(ent.getKey()), userindex, ent.getValue());
			}
		}
		return mat;
	}

	private void updateUserWords(Map<String, Map<String, Double>> x) {
		int newUsers = 0;
		int newWords = 0;
		for (final Entry<String, Map<String, Double>> userWords : x.entrySet()) {
			final String user = userWords.getKey();
			if (!users.containsKey(user)) {
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
		for (final String word : value.keySet()) {
			if (!vocabulary.containsKey(word)) {
				vocabulary.put(word, vocabulary.size());
				newWords++;
			}
		}
		return newWords;
	}

	/**
	 * Construct a learner with the desired number of users and words. If users
	 * and words beyond those in the current model are asked for their
	 * parameters are set to 0
	 * 
	 * @param nusers
	 * @param nwords
	 * @return a new {@link BilinearSparseOnlineLearner}
	 */
	public BilinearSparseOnlineLearner getBilinearLearner(int nusers, int nwords) {
		final BilinearSparseOnlineLearner ret = this.bilinearLearner.clone();

		final Matrix u = ret.getU();
		final Matrix w = ret.getW();
		final Matrix newu = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(nusers, u.getNumColumns());
		final Matrix neww = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(nwords, w.getNumColumns());

		newu.setSubMatrix(0, 0, u);
		neww.setSubMatrix(0, 0, w);

		ret.setU(newu);
		ret.setW(neww);
		return ret;
	}

	/**
	 * @return the underlying {@link BilinearSparseOnlineLearner} with the
	 *         current number of users and words
	 */
	public BilinearSparseOnlineLearner getBilinearLearner() {
		return this.bilinearLearner.clone();
	}

	/**
	 * Given a sparse pair of user/words and value construct a pair of matricies
	 * using the current mappings of words and users to matrix rows.
	 * 
	 * Note: if users or words which have not yet be
	 * 
	 * @param xy
	 * @param nfeatures
	 *            the number of words total in the returned X matrix
	 * @param nusers
	 *            the number of users total in the returned X matrix
	 * @param ntasks
	 *            the number of tasks in the returned Y matrix
	 * @return the matrix pair representing the X and Y input
	 */
	public Pair<Matrix> asMatrixPair(
			IndependentPair<Map<String, Map<String, Double>>, Map<String, Double>> xy,
			int nfeatures, int nusers, int ntasks)
	{
		final Matrix y = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(1, ntasks);
		final Matrix x = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(nfeatures, nusers);
		final Map<String, Double> ymap = xy.secondObject();
		final Map<String, Map<String, Double>> userFeatureMap = xy.firstObject();
		for (final Entry<String, Double> yent : ymap.entrySet()) {
			y.setElement(0, this.values.get(yent.getKey()), yent.getValue());
		}
		for (final Entry<String, Map<String, Double>> xent : userFeatureMap.entrySet()) {
			final int userind = this.users.get(xent.getKey());
			for (final Entry<String, Double> fent : xent.getValue().entrySet()) {
				x.setElement(this.vocabulary.get(fent.getKey()), userind, fent.getValue());
			}
		}
		return new Pair<Matrix>(x, y);
	}

	@Override
	public Map<String, Double> predict(Map<String, Map<String, Double>> x) {
		final Matrix xMat = constructXMatrix(x);
		final Matrix yMat = this.bilinearLearner.predict(xMat);
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
	 * @return calls {@link #asMatrixPair(IndependentPair, int, int, int)} with
	 *         the current number of words, users and value
	 */
	public Pair<Matrix> asMatrixPair(IndependentPair<Map<String, Map<String, Double>>, Map<String, Double>> in) {
		return this.asMatrixPair(in, this.vocabulary.size(), this.users.size(), this.values.size());
	}

	/**
	 * @param x
	 * @param y
	 * @return calls {@link #asMatrixPair(IndependentPair, int, int, int)} with
	 *         the current number of words, users and value
	 */
	public Pair<Matrix> asMatrixPair(Map<String, Map<String, Double>> x, Map<String, Double> y) {
		return this.asMatrixPair(IndependentPair.pair(x, y), this.vocabulary.size(), this.users.size(),
				this.values.size());
	}

	/**
	 * @return the current map of dependent values to indexes
	 */
	public BiMap<String, Integer> getDependantValues() {
		return this.values;
	}

	public BiMap<String, Integer> getUsers() {
		return this.users;
	}
}
