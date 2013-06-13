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
