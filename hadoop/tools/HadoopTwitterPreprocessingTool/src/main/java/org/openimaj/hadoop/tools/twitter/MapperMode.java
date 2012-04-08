package org.openimaj.hadoop.tools.twitter;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.map.MultithreadedMapper;
import org.kohsuke.args4j.CmdLineOptionsProvider;
import org.kohsuke.args4j.Option;

enum MapperMode  implements CmdLineOptionsProvider{
	STANDARD{

		@Override
		public void prepareJobMapper(Job job, Class<SimpleTwitterPreprocessingMapper> mapperClass) {
			job.setMapperClass(mapperClass);
		}
	},
	MULTITHREAD{
		
		@Option(name = "--threads", aliases = "-j", required = false, usage = "Use NUMBER threads per mapper. defaults n processors.", metaVar = "NUMBER")
		private int concurrency = Runtime.getRuntime().availableProcessors();
		
		@Override
		public void prepareJobMapper(Job job, Class<SimpleTwitterPreprocessingMapper> mapperClass) {
			if(concurrency <= 0 ) concurrency = Runtime.getRuntime().availableProcessors();
			
			job.setMapperClass(MultithreadedMapper.class);
			MultithreadedMapper.setNumberOfThreads(job, concurrency);
			MultithreadedMapper.setMapperClass(job, mapperClass);
			System.out.println("NThreads = " + MultithreadedMapper.getNumberOfThreads(job));
		}	
	};
	
	public abstract void prepareJobMapper(Job job, Class<SimpleTwitterPreprocessingMapper> mapperClass);
	@Override
	public Object getOptions() {
		return this;
	}
}