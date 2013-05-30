package org.openimaj.demos.sandbox.ml.linear.learner.stream.twitter;

import org.openimaj.twitter.USMFStatus;
import org.openimaj.util.data.Context;
import org.openimaj.util.function.Function;

import twitter4j.Status;

/**
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ContextTwitterStatusAsUSMFStatus implements Function<Context, Context> {
	@Override
	public Context apply(Context sstatus) {
		USMFStatus status = new USMFStatus();
		Status typedStatus = sstatus.getTyped("status");
		if(typedStatus == null) return sstatus; // don't create the USMFStatus if it isn't there!
		new GeneralJSONTweet4jStatus(typedStatus).fillUSMF(status);
		sstatus.put("usmfstatus", status);
		return sstatus;
	}
}