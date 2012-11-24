package org.openimaj.rdf.storm.tool.lang;

import backtype.storm.Config;

/**
 * @author Jon Hare (jsh2@ecs.soton.ac.uk), Sina Samangooei (ss@ecs.soton.ac.uk)
 * 
 */
public abstract class BaseRuleLanguageHandler implements RuleLanguageHandler {
	@Override
	public void initConfig(Config preparedConfig) {
		// does nothing to the config
	}
}
