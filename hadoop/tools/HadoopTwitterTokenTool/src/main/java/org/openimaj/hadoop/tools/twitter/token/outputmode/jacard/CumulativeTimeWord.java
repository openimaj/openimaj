package org.openimaj.hadoop.tools.twitter.token.outputmode.jacard;

import java.io.DataInput;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadWritableIndependentPair;
import org.openimaj.io.wrappers.ReadableListBinary;

import com.Ostermiller.util.CSVPrinter;

public class CumulativeTimeWord {


	public static class Map extends Mapper<Text,BytesWritable,LongWritable,BytesWritable>{
		private long eldestTime;
		private long deltaTime;
		/**
		 * For every word occurrence, emit <word,true> for its time period, and <word,false> for every time period from
		 * timePeriod + delta until eldestTime. The final time period should be comparing itself to every word ever emitted.
		 * 
		 * This is in the order of 2 million unique words per day which should still be not THAT many words. hopefully.
		 */
		public Map() { }
		protected void setup(Mapper<Text,BytesWritable,LongWritable,BytesWritable>.Context context) throws IOException ,InterruptedException {
			this.eldestTime = context.getConfiguration().getLong(TIME_ELDEST, -1);
			this.deltaTime = context.getConfiguration().getLong(TIME_DELTA, -1);
			if(eldestTime < 0 || deltaTime < 0){
				throw new IOException("Couldn't read reasonable time configurations");
			}
		};
		protected void map(final Text word, BytesWritable value, final Mapper<Text,BytesWritable,LongWritable,BytesWritable>.Context context) throws java.io.IOException ,InterruptedException {
			IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()){
				@Override
				protected Object readValue(DataInput in) throws IOException {
					WordDFIDF idf = new WordDFIDF();
					idf.readBinary(in);
					try {
						String currentword = word.toString();
						ReadWritableStringBoolean timePair = new ReadWritableStringBoolean(Map.this, currentword, true);
						context.write(new LongWritable(idf.timeperiod), new BytesWritable(IOUtils.serialize(timePair)));
						for (long futureTime = idf.timeperiod + deltaTime; futureTime <= eldestTime; futureTime+=deltaTime) {
							ReadWritableStringBoolean futurePair = new ReadWritableStringBoolean(Map.this, currentword, false);
							context.write(new LongWritable(futureTime), new BytesWritable(IOUtils.serialize(futurePair)));
						}
					} catch (InterruptedException e) {
						throw new IOException("");
					}
					return new Object();
				}
			});
		};
	}
	
	/**
	 * Recieve every time period with a list of words either from the current time period or from past time periods.
	 * Construct a union set and intersection set of all words.
	 * 
	 * emit the time period with the length of the union set, the length of the intersection set and the ratio of these two (The Jacard Index)
	 * 
	 * @author ss
	 *
	 */
	public static class Reduce extends Reducer<LongWritable,BytesWritable,LongWritable,Text>{
		public Reduce() {}
		protected void reduce(LongWritable time, Iterable<BytesWritable> wordBools, Reducer<LongWritable,BytesWritable,LongWritable,Text>.Context context) throws IOException ,InterruptedException {
			Set<String> union = new HashSet<String>();
			Set<String> intersection = new HashSet<String>();
			Set<String> historic = new HashSet<String>();
			for (BytesWritable bWordBool : wordBools) {
				ReadWritableStringBoolean wordBool = IOUtils.deserialize(bWordBool.getBytes(), ReadWritableStringBoolean.class);
				union.add(wordBool.firstObject());
				if(!wordBool.secondObject()){
					intersection.add(wordBool.firstObject());
				}
				else{
					historic.add(wordBool.firstObject());
				}
			}
			intersection.retainAll(historic);
			StringWriter swriter = new StringWriter();
			CSVPrinter writer = new CSVPrinter(swriter);
			writer.write(new String[]{"" + intersection.size(),"" + union.size(),"" + (double)intersection.size() / (double)union.size()});
			writer.flush();
			context.write(time, new Text(swriter.toString()));
		};
	}

	protected static final String TIME_DELTA = "org.openimaj.hadoop.tools.twitter.token.time_delta";
	protected static final String TIME_ELDEST = "org.openimaj.hadoop.tools.twitter.token.time_eldest";

}
