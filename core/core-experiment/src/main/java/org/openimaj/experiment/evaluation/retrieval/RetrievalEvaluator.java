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
package org.openimaj.experiment.evaluation.retrieval;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.evaluation.Evaluator;

public class RetrievalEvaluator<R, D extends Identifiable, Q> implements Evaluator<Map<Q, List<D>>, R> {
	protected RetrievalEngine<D, Q> engine;
	protected Collection<Q> queries;
	protected Map<Q, Set<D>> relevant; //in the future we might want a model more like trec qrels with relevance levels
	protected RetrievalAnalyser<R, Q, D> analyser;
	
	public RetrievalEvaluator(RetrievalEngine<D, Q> engine, Collection<Q> queries, Map<Q, Set<D>> relevant, RetrievalAnalyser<R, Q, D> analyser) {
		this.engine = engine;
		this.queries = queries;
		this.relevant = relevant;
		this.analyser = analyser;
	}
	
	@Override
	public Map<Q, List<D>> evaluate() {
		Map<Q, List<D>> results = new HashMap<Q, List<D>>();
		
		for (Q query : queries) {
			results.put(query, engine.search(query));
		}
		
		return results;
	}
	
	@Override
	public R analyse(Map<Q, List<D>> results) {
		return analyser.analyse(results, relevant);
	}
	
	/**
	 * Write the ground-truth data in TREC QRELS format.
	 * @param os stream to write to
	 */
	public void writeQRELS(OutputStream os) {
		writeQRELS(new PrintStream(os));
	}
	
	/**
	 * Write the ground-truth data in TREC QRELS format.
	 * @param os stream to write to
	 */
	public void writeQRELS(PrintStream os) {
		int qnum = 0;
		for (Q query : queries) {
			String qid = "q" + qnum;
			
			for (D doc : relevant.get(query)) {
				os.format("%s %d %s %d", qid, 0, doc.getID(), 1);
			}
		}
	}
}
