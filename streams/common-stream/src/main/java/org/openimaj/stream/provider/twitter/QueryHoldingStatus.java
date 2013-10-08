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
