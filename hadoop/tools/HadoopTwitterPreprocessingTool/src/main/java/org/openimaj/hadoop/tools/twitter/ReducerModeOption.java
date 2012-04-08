package org.openimaj.hadoop.tools.twitter;

import org.kohsuke.args4j.CmdLineOptionsProvider;

/**
 * A style of reducer
 * @author ss
 *
 */
public enum ReducerModeOption implements CmdLineOptionsProvider {
	/**
	 * The null single reducer
	 */
	NULL {
	},
	/**
	 * one output per day 
	 */
	DAY_SPLIT {
	}
	;

	@Override
	public Object getOptions(){
		return this;
	}
}
