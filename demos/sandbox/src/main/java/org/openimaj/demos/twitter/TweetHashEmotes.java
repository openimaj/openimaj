package org.openimaj.demos.twitter;

import org.apache.commons.lang3.StringUtils;
import org.openimaj.logger.LoggerUtils;
import org.openimaj.stream.provider.twitter.TwitterSearchDataset;
import org.openimaj.stream.provider.twitter.TwitterStreamDataset;
import org.openimaj.stream.provider.twitter.TwitterStreamFilterDataset;
import org.openimaj.util.api.auth.DefaultTokenFactory;
import org.openimaj.util.api.auth.common.TwitterAPIToken;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;
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
