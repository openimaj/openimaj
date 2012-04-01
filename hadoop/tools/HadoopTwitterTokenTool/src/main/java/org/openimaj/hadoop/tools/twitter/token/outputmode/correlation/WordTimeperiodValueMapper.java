package org.openimaj.hadoop.tools.twitter.token.outputmode.correlation;

import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.kohsuke.args4j.CmdLineException;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.HadoopTwitterTokenToolOptions;
import org.openimaj.hadoop.tools.twitter.utils.TweetCountWordMap;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;
import org.openimaj.twitter.finance.YahooFinanceData;

import com.jayway.jsonpath.JsonPath;

/**
 * Separate WordDFIDF entries for each word
 * @author ss
 *
 */
public class WordTimeperiodValueMapper extends Mapper<Text, BytesWritable, Text, BytesWritable> {
	
	/**
	 * 
	 */
	public WordTimeperiodValueMapper() {
	}
	

	private HashMap<Long, TweetCountWordMap> tweetWordMap;

	@Override
	protected void setup(Mapper<Text, BytesWritable, Text, BytesWritable>.Context context) throws IOException, InterruptedException {
	}
	
	/**
	 * for each word, read its time period and quantised to a finance time period 
	 * emit for each word a quantised time period, the data needed to calculate DF-IDF at that time and the value from finance
	 */
	@Override
	protected void map(final Text key, BytesWritable value, final Mapper<Text,BytesWritable,Text,BytesWritable>.Context context)
		throws IOException ,InterruptedException {
		IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()){
			WordDFIDF idf = new WordDFIDF();
			@Override
			protected Object readValue(DataInput in) throws IOException {
				idf.readBinary(in);
				try {
					context.write(key, new BytesWritable(IOUtils.serialize(idf)));
				} catch (InterruptedException e) {
					throw new IOException("");
				}
				return new Object();
			}
		});
	};
	
}
