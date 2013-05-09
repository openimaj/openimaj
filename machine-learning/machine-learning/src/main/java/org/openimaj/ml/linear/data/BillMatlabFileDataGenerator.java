package org.openimaj.ml.linear.data;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.util.filter.FilterUtils;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.pair.Pair;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLSparse;

public class BillMatlabFileDataGenerator implements DataGenerator<Matrix> {
	private static class Fold {
		public Fold(int[] training, int[] test, int[] validation) {
			this.training = training;
			this.test = test;
			this.validation = validation;
		}

		int[] training;
		int[] test;
		int[] validation;
	}

	public enum Mode {
		TRAINING {
			@Override
			public int[] indexes(Fold fold) {
				return fold.training;
			}
		},
		TEST {
			@Override
			public int[] indexes(Fold fold) {
				return fold.test;
			}
		},
		VALIDATION {
			@Override
			public int[] indexes(Fold fold) {
				return fold.validation;
			}
		},
		ALL {

			@Override
			public int[] indexes(Fold fold) {
				return null;
			}

		};
		public abstract int[] indexes(Fold fold);
	}

	private Map<String, MLArray> content;
	private List<Fold> folds;
	private int ndays;
	private int nusers;
	private int nwords;
	private List<Matrix> dayWords;
	private List<Matrix> dayPolls;
	private int currentIndex;
	private int ntasks;
	private int[] indexes;
	private Map<Integer, String> voc;
	private String[] tasks;
	private Set<Integer> keepIndex;
	private Map<Integer, Integer> indexToVoc = new HashMap<Integer, Integer>();
	private boolean filter;

	public BillMatlabFileDataGenerator(File matfile, int ndays, boolean filter)
			throws IOException
	{
		final MatFileReader reader = new MatFileReader(matfile);
		this.ndays = ndays;
		this.content = reader.getContent();
		this.currentIndex = 0;
		this.filter = filter;
		prepareVocabulary();
		prepareFolds();
		prepareDayUserWords();
		prepareDayPolls();

	}

	public Map<Integer, String> getVocabulary() {
		return voc;
	}

	private void prepareVocabulary() {

		final MLCell vocLoaded = (MLCell) this.content.get("voc");
		final MLDouble keepIndex = (MLDouble) this.content.get("voc_keep_terms_index");
		final double[] filterIndexArr = keepIndex.getArray()[0];
		this.keepIndex = new HashSet<Integer>();

		final ArrayList<MLArray> vocArr = vocLoaded.cells();
		for (final double d : filterIndexArr) {
			this.keepIndex.add((int) d - 1);
		}

		int index = 0;
		int vocIndex = 0;
		this.voc = new HashMap<Integer, String>();
		for (final MLArray vocArrItem : vocArr) {
			final MLChar vocChar = (MLChar) vocArrItem;
			final String vocString = vocChar.getString(0);
			if (filter && this.keepIndex.contains(index)) {
				this.voc.put(vocIndex, vocString);
				this.indexToVoc.put(index, vocIndex);
				vocIndex++;
			}
			index++;
		}
	}

	public void setFold(int fold, Mode mode) {
		if (fold == -1) {
			this.indexes = new int[this.dayWords.size()];
			for (int i = 0; i < indexes.length; i++) {
				indexes[i] = i;
			}
		}
		else {
			final Fold f = this.folds.get(fold);
			this.indexes = mode.indexes(f);
		}
		this.currentIndex = 0;
	}

	private void prepareDayPolls() {
		final ArrayList<String> pollKeys = FilterUtils.filter(this.content.keySet(),
				new Predicate<String>() {

					@Override
					public boolean test(String object) {
						return object.endsWith("per_unique_extended");
					}
				});
		this.ntasks = pollKeys.size();
		dayPolls = new ArrayList<Matrix>();
		for (int i = 0; i < this.ndays; i++) {
			dayPolls.add(SparseMatrixFactoryMTJ.INSTANCE.createMatrix(1,
					this.ntasks));
		}

		this.tasks = new String[this.ntasks];

		for (int t = 0; t < this.ntasks; t++) {
			final String pollKey = pollKeys.get(t);
			this.tasks[t] = pollKey;
			final MLDouble arr = (MLDouble) this.content.get(pollKey);
			for (int i = 0; i < this.ndays; i++) {
				final Matrix dayPoll = dayPolls.get(i);
				dayPoll.setElement(0, t, arr.get(i, 0));
			}
		}
	}

	public String[] getTasks() {
		return this.tasks;
	}

	private void prepareDayUserWords() {
		final MLSparse arr = (MLSparse) this.content.get("user_vsr_for_polls");
		final Double[] realVals = arr.exportReal();
		final int[] rows = arr.getIR();
		final int[] cols = arr.getJC();
		this.nwords = this.voc.size();
		this.nusers = arr.getM() / this.ndays;
		dayWords = new ArrayList<Matrix>();
		for (int i = 0; i < ndays; i++) {
			final Matrix userWord = SparseMatrixFactoryMTJ.INSTANCE.createMatrix(this.nwords, this.nusers);
			dayWords.add(userWord);
		}
		for (int i = 0; i < rows.length; i++) {
			if (filter && !this.keepIndex.contains(cols[i]))
				continue;
			final int wordIndex = this.indexToVoc.get(cols[i]);
			final int dayIndex = rows[i] / this.nusers;
			final int userIndex = rows[i] - (dayIndex * this.nusers);

			dayWords.get(dayIndex).setElement(wordIndex, userIndex, realVals[i]);

		}
	}

	private void prepareFolds() {

		final MLArray setfolds = this.content.get("set_fold");
		if (setfolds.isCell()) {
			this.folds = new ArrayList<Fold>();
			final MLCell foldcells = (MLCell) setfolds;
			final int nfolds = foldcells.getM();
			System.out.println(String.format("Found %d folds", nfolds));
			for (int i = 0; i < nfolds; i++) {
				final MLDouble training = (MLDouble) foldcells.get(i, 0);
				final MLDouble test = (MLDouble) foldcells.get(i, 1);
				final MLDouble validation = (MLDouble) foldcells.get(i, 2);
				final Fold f = new Fold(toIntArray(training), toIntArray(test),
						toIntArray(validation));
				folds.add(f);
			}
		} else {
			throw new RuntimeException(
					"Can't find set_folds in expected format");
		}
	}

	private int[] toIntArray(MLDouble training) {
		final int[] arr = new int[training.getN()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = training.get(0, i).intValue();
		}
		return arr;
	}

	@Override
	public Pair<Matrix> generate() {
		if (currentIndex >= this.indexes.length)
			return null;
		final int dayIndex = this.indexes[currentIndex];
		final Pair<Matrix> pair = new Pair<Matrix>(this.dayWords.get(dayIndex), this.dayPolls.get(dayIndex));
		currentIndex++;
		return pair;
	}

	public int nFolds() {
		return this.folds.size();
	}
}
