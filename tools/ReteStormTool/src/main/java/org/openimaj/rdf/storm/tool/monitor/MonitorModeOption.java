package org.openimaj.rdf.storm.tool.monitor;

import java.io.IOException;

import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.openimaj.rdf.storm.tool.ReteStormOptions;

import backtype.storm.Config;

public enum MonitorModeOption implements CmdLineOptionsProvider {
	KESTREL {

		@Override
		public MonitorMode getOptions() {
			return new KestrelQueueStatsMonitorMode();
		}

	},
	NONE {

		@Override
		public MonitorMode getOptions() {
			return new MonitorMode() {

				@Override
				public void run() {
					// TODO Auto-generated method stub

				}

				@Override
				public void init(ReteStormOptions opts, Config config) throws IOException {
					// TODO Auto-generated method stub

				}
			};
		}

	};

	@Override
	public abstract MonitorMode getOptions();

}
