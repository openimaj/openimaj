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
package org.openimaj.experiment.evaluation.retrieval.analysers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.lemurproject.ireval.RetrievalEvaluator.Document;
import org.lemurproject.ireval.RetrievalEvaluator.Judgment;
import org.openimaj.data.identity.Identifiable;
import org.openimaj.experiment.evaluation.retrieval.RetrievalAnalyser;

/**
 * A {@link RetrievalAnalyser} that uses the trec_eval commandline tool to
 * perform the analysis.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 * 
 * @param <QUERY>
 *            Type of query
 * @param <DOCUMENT>
 *            Type of document
 */
public class TRECEvalAnalyser<QUERY, DOCUMENT extends Identifiable>
		implements
		RetrievalAnalyser<TRECResult, QUERY, DOCUMENT>
{
	String additionalOptions = "-q -c";
	String toolPath;

	/**
	 * Default constructor
	 */
	public TRECEvalAnalyser() {
		if (System.getenv("TREC_EVAL") != null) {
			toolPath = System.getenv("TREC_EVAL");
		} else if (System.getProperty("TRECEval.path") != null) {
			toolPath = System.getProperty("TRECEval.path");
		} else if (new File("/usr/local/bin/trec_eval").exists()) {
			toolPath = "/usr/local/bin/trec_eval";
		} else if (new File("/usr/bin/trec_eval").exists()) {
			toolPath = "/usr/bin/trec_eval";
		} else {
			toolPath = "trec_eval";
		}
	}

	@Override
	public TRECResult analyse(Map<QUERY, List<DOCUMENT>> results, Map<QUERY, Set<DOCUMENT>> relevant) {
		try {
			final File qrels = File.createTempFile("openimaj_trec_eval", ".qrels");
			writeQRELS(relevant, new PrintStream(new FileOutputStream(qrels)));

			final File top = File.createTempFile("trec_eval", ".top");
			writeTop(results, new PrintStream(new FileOutputStream(top)));

			ProcessBuilder pb;
			if (additionalOptions != null)
				pb = new ProcessBuilder(toolPath, additionalOptions, qrels.getAbsolutePath(), top.getAbsolutePath());
			else
				pb = new ProcessBuilder(toolPath, qrels.getAbsolutePath(), top.getAbsolutePath());

			final Process proc = pb.start();

			final StreamReader sysout = new StreamReader(proc.getInputStream());
			final StreamReader syserr = new StreamReader(proc.getErrorStream());

			sysout.start();
			syserr.start();

			final int rc = proc.waitFor();

			final TRECResult analysis = new TRECResult(sysout.builder.toString());

			if (rc != 0) {
				System.err.println(pb.command());
				throw new RuntimeException("An error occurred running trec_eval: " + syserr.builder.toString());
			}

			qrels.delete();
			top.delete();

			return analysis;
		} catch (final Exception e) {
			throw new RuntimeException("An error occurred running trec_eval.", e);
		}
	}

	/**
	 * Write retrieval results in TREC TOP format.
	 * 
	 * @param <Q>
	 *            Type of query
	 * @param <D>
	 *            Type of Document
	 * @param results
	 *            the ranked results.
	 * @param os
	 *            stream to write to
	 */
	public static <Q, D extends Identifiable> void writeTop(Map<Q, List<D>> results, PrintStream os) {
		final TreeMap<String, ArrayList<Document>> converted = IREvalAnalyser.convertResults(results);

		for (final String query : converted.keySet()) {
			for (final Document d : converted.get(query)) {
				// qid iter docno rank sim run_id
				os.format("%s %d %s %d %f %s\n", query, 0, d.documentNumber, d.rank, d.score, "runid");
			}
		}
	}

	/**
	 * Write the ground-truth data in TREC QRELS format.
	 * 
	 * @param <Q>
	 *            Type of query
	 * @param <D>
	 *            Type of Document
	 * @param relevant
	 *            the relevant docs for each query
	 * @param os
	 *            stream to write to
	 */
	public static <Q, D extends Identifiable> void writeQRELS(Map<Q, Set<D>> relevant, PrintStream os) {
		final TreeMap<String, ArrayList<Judgment>> converted = IREvalAnalyser.convertRelevant(relevant);

		for (final String query : converted.keySet()) {
			for (final Judgment j : converted.get(query)) {
				os.format("%s %d %s %d\n", query, 0, j.documentNumber, j.judgment);
			}
		}
	}

	static class StreamReader extends Thread {
		private StringBuilder builder = new StringBuilder();
		private BufferedReader br;

		public StreamReader(InputStream is) {
			br = new BufferedReader(new InputStreamReader(is));
		}

		@Override
		public void run() {
			try {
				String line;
				while ((line = br.readLine()) != null) {
					builder.append(line);
					builder.append("\n");
				}
			} catch (final IOException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					br.close();
				} catch (final IOException e) {
				}
			}
		}
	}
}
