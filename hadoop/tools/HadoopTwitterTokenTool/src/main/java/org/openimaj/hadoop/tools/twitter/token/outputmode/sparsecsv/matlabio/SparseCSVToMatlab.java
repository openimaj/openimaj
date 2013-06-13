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
package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.matlabio;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.TimeIndex;
import org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv.WordIndex;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.util.pair.IndependentPair;

import com.Ostermiller.util.CSVParser;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLCell;
import com.jmatio.types.MLChar;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLSparse;

public class SparseCSVToMatlab {
	static class WordTimeDFIDF {
		int word;
		int time;
		WordDFIDF idf;
	}

	public static void main(String[] args) throws IOException {

		String sparseCSVRoot = "/Users/ss/Development/data/TrendMiner/sheffield/2010/09/tweets.2010-09.24hours.top100k.sparsecsv";
		String outfileName = "mat_file.mat";
		if (args.length > 0) {
			sparseCSVRoot = args[0];
			if (args.length > 1) {
				outfileName = args[1];
			}
		}

		final LinkedHashMap<String, IndependentPair<Long, Long>> wordIndex = WordIndex.readWordCountLines(sparseCSVRoot);
		final LinkedHashMap<Long, IndependentPair<Long, Long>> timeIndex = TimeIndex.readTimeCountLines(sparseCSVRoot);
		System.out.println("Preparing matlab files");

		final MLCell wordCell = new MLCell("words", new int[] { wordIndex.size(), 2 });
		final MLCell timeCell = new MLCell("times", new int[] { timeIndex.size(), 2 });

		System.out.println("... reading times");
		for (final Entry<Long, IndependentPair<Long, Long>> ent : timeIndex.entrySet()) {
			final long time = ent.getKey();
			final int timeCellIndex = (int) (long) ent.getValue().secondObject();
			final long count = ent.getValue().firstObject();
			timeCell.set(new MLDouble(null, new double[][] { new double[] { time } }), timeCellIndex, 0);
			timeCell.set(new MLDouble(null, new double[][] { new double[] { count } }), timeCellIndex, 1);
		}

		System.out.println("... reading words");
		for (final Entry<String, IndependentPair<Long, Long>> ent : wordIndex.entrySet()) {
			final String word = ent.getKey();
			final int wordCellIndex = (int) (long) ent.getValue().secondObject();
			final long count = ent.getValue().firstObject();
			wordCell.set(new MLChar(null, word), wordCellIndex, 0);
			wordCell.set(new MLDouble(null, new double[][] { new double[] { count } }), wordCellIndex, 1);
		}

		System.out.println("... preapring values array");
		final File valuesIn = new File(sparseCSVRoot, "values/part-r-00000");
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(valuesIn), "UTF-8"));
			final int nValues = wordIndex.size() * timeIndex.size();
			final MLSparse matarr = new MLSparse("values", new int[] { wordIndex.size(), timeIndex.size() }, 0, nValues);
			System.out.println("... reading values");
			String wholeLine = null;
			while ((wholeLine = reader.readLine()) != null) {
				final StringReader strReader = new StringReader(wholeLine);
				final CSVParser parser = new CSVParser(strReader);
				final String[] line = parser.getLine();
				if (line == null) {
					continue;
				}
				final WordTimeDFIDF wtd = new WordTimeDFIDF();
				wtd.word = Integer.parseInt(line[0]);
				wtd.time = Integer.parseInt(line[1]);
				wtd.idf = new WordDFIDF();
				wtd.idf.timeperiod = timeCell.getIndex(wtd.time, 0);
				wtd.idf.wf = Integer.parseInt(line[2]);
				wtd.idf.tf = Integer.parseInt(line[3]);
				wtd.idf.Twf = Integer.parseInt(line[4]);
				wtd.idf.Ttf = Integer.parseInt(line[5]);

				matarr.set(wtd.idf.dfidf(), wtd.word, wtd.time);
			}
			System.out.println("writing!");
			final ArrayList<MLArray> list = new ArrayList<MLArray>();
			list.add(wordCell);
			list.add(timeCell);
			list.add(matarr);
			new MatFileWriter(sparseCSVRoot + File.separator + outfileName, list);
		} finally {
			reader.close();
		}
	}
}
