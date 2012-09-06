package org.openimaj.tools.twitter.modes.filter;

import org.openimaj.twitter.USMFStatus;

/**
 * Sees whether the {@link USMFStatus#reply_to} contains anything sensible
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class IsReplyFilter extends TwitterPreprocessingFilter {

	@Override
	public boolean filter(USMFStatus twitterStatus) {
		return twitterStatus.reply_to!=null && twitterStatus.reply_to.name!=null;
	}

}
