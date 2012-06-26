package org.openimaj.hadoop.tools.twitter.token.outputmode.timeseries;

import java.io.StringWriter;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDFTimeSeries;
import org.openimaj.io.IOUtils;

/**
 * Given a stream of wordDFIDF as input, reads each DFIDF, constructs a time series and emits the time series
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class WordDFIDFTimeSeriesReducer extends Reducer<Text, BytesWritable, NullWritable, Text> {
	@Override
	protected void reduce(Text word, java.lang.Iterable<BytesWritable> dfidfs, Reducer<Text,BytesWritable,NullWritable,Text>.Context context) throws java.io.IOException ,InterruptedException {
		WordDFIDFTimeSeries dts = new WordDFIDFTimeSeries();
		for (BytesWritable bytesWritable : dfidfs) {
			WordDFIDF wd = IOUtils.deserialize(bytesWritable.getBytes(), WordDFIDF.class);
			dts.add(wd.timeperiod, wd);
		}
		StringWriter writer = new StringWriter();
		writer.write(word + " ");
		IOUtils.writeASCII(writer, dts);
		context.write(NullWritable.get(), new Text(writer .toString()));
	};
}
