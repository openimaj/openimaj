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

import org.apache.commons.lang3.StringUtils;
import org.openimaj.logger.LoggerUtils;
import org.openimaj.stream.provider.twitter.TwitterStreamFilterDataset;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.stream.Stream;

import twitter4j.Query;
import twitter4j.Status;

/**
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class TweetHashEmotes {
	public static void main(String[] args) {
		
		String[] hashes = new String[]{
				"#amazed",
				"#angry",
				"#annoyed",
				"#awesome",
				"#awkward",
				"#bored",
				"#calm",
				"#confused",
				"#delighted",
				"#depressed",
				"#elated",
				"#excited",
				"#happy",
				"#helpless",
				"#hopeful",
				"#hurt",
				"#jealous",
				"#joyful",
				"#lonely",
				"#love",
				"#neat",
				"#nervous",
				"#proud",
				"#relaxed",
				"#sad",
				"#scared",
				"#sexy",
				"#sleepy",
				"#sorry",
				"#sweet",
				"#thrilled",
				"#upset"
			
		};
		LoggerUtils.prepareConsoleLogger();
		final PrettyTagRenderer renderer = new PrettyTagRenderer(hashes);
		final TwitterAPIToken token = DefaultTokenFactory.get(TwitterAPIToken.class);
		String q = StringUtils.join(hashes, " OR ");
		Query query = new Query(q);
//		final Stream<Status> stream = new TwitterStreamDataset(token);
//		final Stream<Status> stream = new TwitterSearchDataset(query, token);
		final Stream<Status> stream = new TwitterStreamFilterDataset(hashes, token);
		stream
			.map(new HashTagMatch(hashes ))
			.forEach(renderer);
	}
}
