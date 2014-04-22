package org.openimaj.demos.twitter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;
import org.openimaj.util.function.MultiFunction;
import org.openimaj.util.function.Predicate;

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
