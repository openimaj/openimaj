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
package org.openimaj.stream.provider.twitter;

import java.util.List;

import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.BlockingDroppingQueue;
import org.openimaj.util.data.Context;

import twitter4j.Query;
import twitter4j.Status;

/**
 * Extends the {@link TwitterSearchDataset} to support multiple queries. All
 * attempts are made to access the Twitter API in a fair way. Each query is
 * handled in turn and backoffs, new results etc. are stored per query.
 * <p>
 * A round robin approach is used to fill the underlying
 * {@link BlockingDroppingQueue}. Each query is given a chance to fill the
 * queue, then all appropriate wait times are respected and the next query is
 * given a chance.
 * <p>
 * Further this round robin dataset passes {@link QueryHoldingStatus} instances
 * due to the variable queue.
 * 
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class ContextRoundRobinTwitterSearchAPIDataset extends AbstractTwitterSearchDataset<Context> {

	private List<Query> queries;
	private int currentQuery;

	/**
	 * @param queries
	 * @param token
	 * @param buffer
	 */
	public ContextRoundRobinTwitterSearchAPIDataset(List<Query> queries, TwitterAPIToken token,
			BlockingDroppingQueue<Context> buffer)
	{
		super(token, buffer, queries.get(0));

		this.queries = queries;
		this.currentQuery = 0;
	}

	@Override
	public Query getQuery() {
		final Query retQuery = this.queries.get(this.currentQuery);
		nextQuery();
		return retQuery;
	}

	private void nextQuery() {
		this.currentQuery++;
		if (this.currentQuery >= this.queries.size())
			this.currentQuery = 0;
	}

	@Override
	public void registerStatus(Query query, Status status, String json) throws InterruptedException {
		final Context c = new Context();
		c.put("query", query);
		c.put("status", status);
		c.put("status_json", json);
		register(c);
	}
}
