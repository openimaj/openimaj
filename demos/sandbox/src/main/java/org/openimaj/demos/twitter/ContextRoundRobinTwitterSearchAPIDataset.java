package org.openimaj.demos.twitter;

import java.util.List;

import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.concurrent.BlockingDroppingQueue;
import org.openimaj.util.data.Context;

import twitter4j.Query;
import twitter4j.Status;

/**
 * Extends the {@link TwitterSearchAPIDataset} to support multiple queries.
 * All attempts are made to access the Twitter API in a fair way. Each query is
 * handled in turn and backoffs, new results etc. are stored per query.
 *
 * A round robin approach is used to fill the underlying {@link BlockingDroppingQueue}.
 * Each query is given a chance to fill the queue, then all appropriate wait times are
 * respected and the next query is given a chance.
 *
 * Further this round robin dataset passes {@link QueryHoldingStatus} instances due to
 * the variable queue
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ContextRoundRobinTwitterSearchAPIDataset extends AbstractTwitterSearchAPIDataset<Context>{

	private List<Query> queries;
	private int currentQuery;

	/**
	 * @param queries
	 * @param token
	 * @param buffer
	 */
	public ContextRoundRobinTwitterSearchAPIDataset(List<Query> queries, TwitterAPIToken token, BlockingDroppingQueue<Context> buffer) {
		super(token, buffer);
		if(queries.size() == 0){
			return;
		}
		this.queries = queries;
		this.currentQuery = 0;
		this.startSearch();
	}

	@Override
	public Query getQuery() {
		Query retQuery = this.queries.get(this.currentQuery);
		nextQuery();
		return retQuery;
	}

	private void nextQuery() {
		this.currentQuery ++;
		if(this.currentQuery>=this.queries.size())this.currentQuery=0;
	}

	@Override
	public void registerStatus(Query query, Status status, String json) throws InterruptedException {
		Context c = new Context();
		c.put("query", query);
		c.put("status", status);
		c.put("status_json", json);
		register(c);
	}

}
