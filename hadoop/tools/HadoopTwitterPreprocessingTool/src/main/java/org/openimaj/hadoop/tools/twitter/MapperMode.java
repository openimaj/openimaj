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
package org.openimaj.hadoop.tools.twitter;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;

enum MapperMode implements CmdLineOptionsProvider {
	STANDARD {
		@Override
		public Mode getOptions() {
			return new Mode();
		}
	},
	MULTITHREAD {
		@Override
		public Mode getOptions() {
			return new MultithreadMode();
		}
	};

	public static class Mode {
		public void prepareJobMapper(Job job, Class<SimpleTwitterPreprocessingMapper> mapperClass) {
			job.setMapperClass(mapperClass);
		}
	}

	public static class MultithreadMode extends Mode {
		@Option(
				name = "--threads",
				aliases = "-j",
				required = false,
				usage = "Use NUMBER threads per mapper. defaults n processors.",
				metaVar = "NUMBER")
		private int concurrency = Runtime.getRuntime().availableProcessors();

		@Override
		public void prepareJobMapper(Job job, Class<SimpleTwitterPreprocessingMapper> mapperClass) {
			if (concurrency <= 0)
				concurrency = Runtime.getRuntime().availableProcessors();

			job.setMapperClass(MultithreadedMapper.class);
			MultithreadedMapper.setNumberOfThreads(job, concurrency);
			MultithreadedMapper.setMapperClass(job, mapperClass);
			System.out.println("NThreads = " + MultithreadedMapper.getNumberOfThreads(job));
		}
	}
}
