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
package org.openimaj.ml.clustering.rforest;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

import org.openimaj.citation.annotation.Reference;
import org.openimaj.citation.annotation.ReferenceType;
import org.openimaj.data.DataSource;
import org.openimaj.ml.clustering.IndexClusters;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.SpatialClusters;
import org.openimaj.ml.clustering.assignment.HardAssigner;
import org.openimaj.util.hash.HashCodeUtil;
import org.openimaj.util.pair.IntFloatPair;

/**
 * An implementation of the RandomForest clustering algorithm proposed by <a
 * href
 * ="http://users.info.unicaen.fr/~jurie/papers/moosman-nowak-jurie-pami08.pdf"
 * >Jurie et al</a>.
 * <p>
 * In this implementation the training phase is used to identify the limits of
 * the data (for which a very small subset may be provided). Once this is known
 * N decision trees are constructed each with M decisions (see
 * {@link RandomDecisionTree}). In the clustering phase each feature projected
 * is assigned a letter for each decision tree.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
@Reference(
		type = ReferenceType.Article,
		author = { "Frank Moosmann", "Eric Nowak", "Fr{\'e}d{\'e}ric Jurie" },
		title = "Randomized Clustering Forests for Image Classification",
		year = "2008",
		journal = "IEEE PAMI",
		url = "http://dx.doi.org/10.1109/TPAMI.2007.70822")
public class IntRandomForest
		implements
		SpatialClusters<int[]>,
		SpatialClusterer<IntRandomForest, int[]>,
		HardAssigner<int[], float[], IntFloatPair>
{
	private static final String HEADER = SpatialClusters.CLUSTER_HEADER + "RFIC";
	int nDecisions;
	int nTrees;
	int featureLength;
	List<RandomDecisionTree> trees;
	int[] maxVal;
	int[] minVal;
	Map<Letter, Integer> letterToInt;
	private int currentInt = 0;
	private HashMap<Word, Integer> wordToInt;
	private int currentWordInt = 0;
	private int randomSeed = -1;

	private Word wordFromString(String str) {
		final String[] values = str.split("_");
		final Letter[] intValues = new Letter[values.length];
		int i = 0;
		for (final String s : values) {
			intValues[i++] = letterFromString(s);
		}
		return new Word(intValues);
	}

	private Letter letterFromString(String str) {
		final String[] values = str.split("-");
		final boolean[] intValues = new boolean[values.length];
		int i = 0;
		for (int j = 0; j < values.length - 1; j++) {
			intValues[i++] = Boolean.parseBoolean(values[j]);
		}
		return new Letter(intValues, Integer.parseInt(values[values.length - 1]));
	}

	class Word {
		private Letter[] letters;

		Word(Letter[] value) {
			letters = value;
		}

		@Override
		public int hashCode() {
			int result = HashCodeUtil.SEED;
			for (final Letter l : letters) {
				result = HashCodeUtil.hash(result, l);
			}
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Word))
				return false;

			final Word that = (Word) obj;
			boolean same = true;
			for (int i = 0; i < letters.length; i++) {
				same &= letters[i].equals(that.letters[i]);

				if (!same)
					return false;
			}

			return same;
		}

		@Override
		public String toString() {
			String outString = "";
			for (int i = 0; i < this.letters.length; i++) {
				outString += "_" + letters[i];
			}
			return outString.substring(1);
		}

		public int hashedWord() {
			if (!wordToInt.containsKey(this)) {
				wordToInt.put(this, currentWordInt++);
				if (currentWordInt == Integer.MAX_VALUE) {
					System.err.println("Too many words!");
					currentWordInt = 0;
				}
			}
			return wordToInt.get(this);
		}
	}

	class Letter {
		boolean[] value;
		int treeIndex;

		public Letter(boolean[] value, int treeIndex) {
			this.value = value;
			this.treeIndex = treeIndex;
		}

		@Override
		public int hashCode() {
			int result = HashCodeUtil.SEED;
			result = HashCodeUtil.hash(result, this.value);
			result = HashCodeUtil.hash(result, this.treeIndex);
			return result;
		}

		public int getTreeIndex() {
			return this.treeIndex;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Letter))
				return false;
			final Letter that = (Letter) obj;
			boolean same = true;
			for (int i = 0; i < this.value.length; i++) {
				same &= this.value[i] == that.value[i];
				if (!same)
					return false;
			}
			same &= this.treeIndex == that.treeIndex;
			return same;
		}

		@Override
		public String toString() {
			String outString = "";
			for (int i = 0; i < this.value.length; i++) {
				outString += "-" + (value[i] ? 1 : 0);
			}
			return outString.substring(1) + "-" + this.treeIndex;
		}

		public int hashedLetter() {
			if (!letterToInt.containsKey(this)) {
				letterToInt.put(this, currentInt++);
				if (currentInt == Integer.MAX_VALUE) {
					System.err.println("... too many letters!");
					currentInt = 0;
				}
			}
			return letterToInt.get(this);
		}
	}

	/**
	 * Makes a default random forest with 32 trees each with 32 decisions. This
	 * results in 10^47 potential words.
	 */
	public IntRandomForest() {
		this(32, 32);
	}

	/**
	 * Makes a random forest with nTrees each with nDecisions. This will result
	 * in nTrees ^ nDecisions potential words
	 * 
	 * @param nTrees
	 *            number of trees
	 * @param nDecisions
	 *            number of decisions per tree
	 */
	public IntRandomForest(int nTrees, int nDecisions) {
		this.nTrees = nTrees;
		this.nDecisions = nDecisions;
		this.letterToInt = new HashMap<Letter, Integer>();
		this.wordToInt = new HashMap<Word, Integer>();
	}

	private void initMinMax(int[][] data) {
		final int[] min = new int[this.featureLength];
		final int[] max = new int[this.featureLength];
		boolean isSet = false;
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < this.featureLength; j++) {
				final int val = data[i][j];
				if (!isSet) {
					min[j] = val;
					max[j] = val;
				} else {
					if (max[j] < val)
						max[j] = val;
					else if (min[j] > val)
						min[j] = val;
				}
			}
			isSet = true;
		}
		setMinMax(min, max);
	}

	/**
	 * The maximum and minimum values for the various dimentions against which
	 * random decisions will be based.
	 * 
	 * @param min
	 * @param max
	 */
	public void setMinMax(int[] min, int[] max) {
		this.minVal = min;
		this.maxVal = max;

	}

	private void initTrees() {
		this.trees = new LinkedList<RandomDecisionTree>();
		Random r = new Random();
		if (this.randomSeed != -1)
			r = new Random(this.randomSeed);
		for (int i = 0; i < nTrees; i++) {
			final RandomDecisionTree tree = new RandomDecisionTree(this.nDecisions, this.featureLength, this.minVal,
					this.maxVal, r);
			this.trees.add(tree);
		}
	}

	@Override
	public IntRandomForest cluster(int[][] data) {
		this.featureLength = data[0].length;

		initMinMax(data);
		initTrees();

		return this;
	}

	@Override
	public IntRandomForest cluster(DataSource<int[]> data) {
		final int[][] dataArr = new int[data.size()][data.numDimensions()];

		return cluster(dataArr);
	}

	@Override
	public int numClusters() {
		return this.currentInt;
	}

	@Override
	public int numDimensions() {
		return featureLength;
	}

	@Override
	public int[] assign(int[][] data) {
		final int[] proj = new int[data.length];

		for (int i = 0; i < data.length; i++) {
			proj[i] = this.assign(data[i]);
		}
		return proj;
	}

	/**
	 * Push each data point provided to a set of letters, i.e. a word. Each
	 * letter represents a set of decisions made in a single decision tree.
	 * 
	 * @param data
	 * @return A word per data point
	 */
	public Word[] assignLetters(int[][] data) {
		final Word[] pushedLetters = new Word[data.length];

		int i = 0;
		for (final int[] k : data) {
			pushedLetters[i++] = this.assignWord(k);
		}

		return pushedLetters;
	}

	/**
	 * Push a single data point to a set of letters, return the letters as word.
	 * This is achieved by pushing the data point down each decision tree. This
	 * returns the result of the n decisions made for that tree, this is a
	 * single letter. As a letter is seen for the first time, it is assigned a
	 * number. The letters are combined in sequence to construct a word.
	 * 
	 * @param data
	 *            to be projected
	 * @return A single word containing the letters containing the decisions
	 *         made on each tree
	 */
	public Word assignWord(int[] data) {
		final Letter[] pushed = new Letter[this.nTrees];
		for (int i = 0; i < this.nTrees; i++) {
			final boolean[] justLetter = this.trees.get(i).getLetter(data);
			final Letter letter = new Letter(justLetter, i);
			letter.hashedLetter();
			pushed[i] = letter;
		}
		return new Word(pushed);
	}

	/**
	 * Uses the {@link #assignWord(int[])} function to construct the word
	 * representing this data point. If this exact word has been seen before
	 * (i.e. these letters in this order) the same int is used. If not, a new
	 * int is assigned for this word.
	 * 
	 * @param data
	 *            a data point to be clustered to a word
	 * @return a cluster centroid from a word
	 */
	@Override
	public int assign(int[] data) {
		final Word word = this.assignWord(data);
		return word.hashedWord();
	}

	/**
	 * @return The number of decision trees
	 */
	public int getNTrees() {
		return this.nTrees;
	}

	/**
	 * @return the number of decisions per tree
	 */
	public int getNDecisions() {
		return this.nDecisions;
	}

	/**
	 * @return the decision trees
	 */
	public List<RandomDecisionTree> getTrees() {
		return this.trees;
	}

	@Override
	public boolean equals(Object r) {
		if (!(r instanceof IntRandomForest))
			return false;

		final IntRandomForest that = (IntRandomForest) r;

		boolean same = true;

		same &= this.numDimensions() == that.numDimensions();
		same &= this.getNTrees() == that.getNTrees();
		same &= this.getNDecisions() == that.getNDecisions();

		for (int i = 0; i < that.trees.size(); i++) {
			this.trees.get(i).equals(that.trees.get(i));
		}

		for (final Entry<Letter, Integer> a : that.letterToInt.entrySet()) {
			same &= that.letterToInt.get(a.getKey()).equals(this.letterToInt.get(a.getKey()));
		}

		for (final Entry<Word, Integer> a : that.wordToInt.entrySet()) {
			same &= that.wordToInt.get(a.getKey()).equals(this.wordToInt.get(a.getKey()));
		}

		return same;
	}

	@Override
	public String asciiHeader() {
		return "ASCII" + HEADER;
	}

	@Override
	public byte[] binaryHeader() {
		return HEADER.getBytes();
	}

	@Override
	public void readASCII(Scanner br) throws IOException {
		nDecisions = Integer.parseInt(br.nextLine());
		nTrees = Integer.parseInt(br.nextLine());
		this.letterToInt = new HashMap<Letter, Integer>();
		featureLength = Integer.parseInt(br.nextLine());

		if (this.trees == null || this.trees.size() != nTrees) {
			trees = new LinkedList<RandomDecisionTree>();
			for (int i = 0; i < nTrees; i++)
				trees.add(new RandomDecisionTree().readASCII(br));
		} else {
			// We have an existing tree, try to read it!
			for (final RandomDecisionTree rt : trees) {
				rt.readASCII(br);
			}
		}

		// Only rebuild an array of the wrong size
		String[] line = br.nextLine().split(" ");
		if (maxVal == null || maxVal.length != featureLength)
			maxVal = new int[featureLength];
		for (int i = 0; i < featureLength; i++)
			maxVal[i] = Integer.parseInt(line[i]);
		if (minVal == null || minVal.length != featureLength)
			minVal = new int[featureLength];
		line = br.nextLine().split(" ");
		for (int i = 0; i < featureLength; i++)
			minVal[i] = Integer.parseInt(line[i]);
		currentInt = Integer.parseInt(br.nextLine());

		line = br.nextLine().split(" ");
		assert ((line.length - 1) == currentInt);
		for (int i = 0; i < currentInt; i++) {
			final String[] part = line[i].split(",");

			letterToInt.put(letterFromString(part[0]), Integer.parseInt(part[1]));
		}
		currentWordInt = Integer.parseInt(br.nextLine());
		if (currentWordInt != 0) {
			line = br.nextLine().split(" ");
			assert ((line.length - 1) == currentWordInt);
			for (int i = 0; i < currentWordInt; i++) {
				final String[] part = line[i].split(",");

				wordToInt.put(wordFromString(part[0]), Integer.parseInt(part[1]));
			}
		}
	}

	@Override
	public void readBinary(DataInput dis) throws IOException {
		nDecisions = dis.readInt();
		nTrees = dis.readInt();
		this.letterToInt = new HashMap<Letter, Integer>();
		featureLength = dis.readInt();
		if (this.trees == null || this.trees.size() != nTrees) {
			trees = new LinkedList<RandomDecisionTree>();
			for (int i = 0; i < nTrees; i++)
				trees.add(new RandomDecisionTree().readBinary(dis));
		} else {
			// We have an existing tree, try to read it!
			for (final RandomDecisionTree rt : trees) {
				rt.readBinary(dis);
			}
		}

		if (maxVal == null || maxVal.length != featureLength)
			maxVal = new int[featureLength];
		for (int i = 0; i < featureLength; i++)
			maxVal[i] = dis.readInt();

		if (minVal == null || minVal.length != featureLength)
			minVal = new int[featureLength];
		for (int i = 0; i < featureLength; i++)
			minVal[i] = dis.readInt();

		currentInt = dis.readInt();
		for (int i = 0; i < currentInt; i++) {
			final int letterLen = dis.readInt();
			final boolean[] stringBytes = new boolean[letterLen];
			// dis.read(stringBytes, 0, stringBytes .length); // Entry key
			for (int j = 0; j < letterLen; j++)
				stringBytes[j] = dis.readBoolean();
			letterToInt.put(new Letter(stringBytes, dis.readInt()), dis.readInt());
		}

		currentWordInt = dis.readInt();
		if (currentWordInt != 0) {
			for (int i = 0; i < currentWordInt; i++) {
				final Letter[] letters = new Letter[dis.readInt()];
				// dis.read(stringBytes, 0, stringBytes .length); // Entry key
				for (int j = 0; j < letters.length; j++) {
					final int letterLen = dis.readInt();
					final boolean[] stringBytes = new boolean[letterLen];
					for (int k = 0; k < stringBytes.length; k++) {
						stringBytes[k] = dis.readBoolean();
					}
					letters[j] = new Letter(stringBytes, dis.readInt());
				}
				wordToInt.put(new Word(letters), dis.readInt());
			}
		}
	}

	@Override
	public void writeASCII(PrintWriter writer) throws IOException {
		writer.println(this.nDecisions);
		writer.println(this.nTrees);
		writer.println(this.featureLength);
		for (final RandomDecisionTree tree : trees) {
			tree.writeASCII(writer);
			writer.println();
		}
		for (int i = 0; i < maxVal.length; i++)
			writer.print(maxVal[i] + " ");
		writer.println();
		for (int i = 0; i < minVal.length; i++)
			writer.print(minVal[i] + " ");
		writer.println();
		writer.println(currentInt);
		for (final Entry<Letter, Integer> p : letterToInt.entrySet()) {
			writer.print(p.getKey() + "," + p.getValue() + " ");
		}
		writer.println();
		writer.println(currentWordInt);
		for (final Entry<Word, Integer> p : wordToInt.entrySet()) {
			writer.print(p.getKey() + "," + p.getValue() + " ");
		}
		writer.println();
		writer.flush();
	}

	@Override
	public void writeBinary(DataOutput o) throws IOException {
		o.writeInt(nDecisions);
		o.writeInt(nTrees);
		o.writeInt(featureLength);
		for (final RandomDecisionTree tree : trees) {
			tree.write(o);
		}

		for (int i = 0; i < maxVal.length; i++)
			o.writeInt(maxVal[i]);
		for (int i = 0; i < minVal.length; i++)
			o.writeInt(minVal[i]);

		o.writeInt(currentInt);
		for (final Entry<Letter, Integer> p : letterToInt.entrySet()) {
			o.writeInt(p.getKey().value.length);
			for (final boolean i : p.getKey().value)
				o.writeBoolean(i);
			o.writeInt(p.getKey().treeIndex);
			o.writeInt(p.getValue());
		}
		o.writeInt(currentWordInt);
		for (final Entry<Word, Integer> p : wordToInt.entrySet()) {
			o.writeInt(p.getKey().letters.length);
			for (final Letter i : p.getKey().letters) {
				o.writeInt(i.value.length);
				for (final boolean j : i.value)
					o.writeBoolean(j);
				o.writeInt(i.treeIndex);
			}
			o.writeInt(p.getValue());
		}
	}

	/**
	 * @param random
	 *            the seed of the java {@link Random} instance used by the
	 *            decision trees
	 */
	public void setRandomSeed(int random) {
		this.randomSeed = random;
	}

	Letter newLetter(boolean[] bs, int i) {
		return new Letter(bs, i);
	}

	Word newWord(Letter[] letters) {
		return new Word(letters);
	}

	@Override
	public void assignDistance(int[][] data, int[] indices, float[] distances) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IntFloatPair assignDistance(int[] data) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public HardAssigner<int[], ?, ?> defaultHardAssigner() {
		return this;
	}

	@Override
	public int size() {
		return this.currentInt;
	}

	@Override
	public int[][] performClustering(int[][] data) {
		return new IndexClusters(this.cluster(data).defaultHardAssigner().assign(data)).clusters();
	}
}
