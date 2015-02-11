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
package org.openimaj.demos.twitter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.MultiFunction;
import twitter4j.HashtagEntity;
import twitter4j.Status;

public class HashTagMatch implements MultiFunction<Status, Context> {

	public static final String HASHTAG_KEY = "hastagmatch.hashtag";
	private String[] hashtags;
	private HashSet<String> hashes = new HashSet<String>();

	public HashTagMatch(String[] hashtags) {
		this.hashtags = hashtags;
		for (String string : hashtags) {
			hashes .add(string);
		}
	}
	@Override
	public List<Context> apply(Status in) {
		HashtagEntity[] tags = in.getHashtagEntities();
		List<Context> ret = new ArrayList<Context>();
		for (HashtagEntity hashtagEntity : tags) {
			String withHash = "#" + hashtagEntity.getText();
			
			if(hashes.contains(withHash)){
				Context ctx = new Context();
				ctx.put(HASHTAG_KEY, withHash);
				ctx.put("status", in);
				ret.add(ctx);
			}
		}
		return ret;
	}

}
