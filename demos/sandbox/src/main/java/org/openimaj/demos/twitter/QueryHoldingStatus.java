package org.openimaj.demos.twitter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import twitter4j.Query;
import twitter4j.Status;

/**
 * Holds the query which generated a status and the raw JSON
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class QueryHoldingStatus implements InvocationHandler{
	/**
	 * the status
	 */
	public Status status;
	/**
	 * the raw json of the status
	 */
	public String json;
	/**
	 * the query which generated the status
	 */
	public Query query;
	/**
	 * @param status
	 * @param json
	 * @param query
	 */
	private QueryHoldingStatus(Status status, String json, Query query) {
		this.status = status;
		this.json = json;
		this.query = query;
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		 return method.invoke(status, args);
	}

	/**
	 * @param status
	 * @param json
	 * @param query
	 * @return the status
	 */
	public static Status create(Status status, String json, Query query) {
        return (Status)(Proxy.newProxyInstance(Status.class.getClassLoader(),
            new Class[] {Status.class},
                new QueryHoldingStatus(status,json,query)));
    }

}
