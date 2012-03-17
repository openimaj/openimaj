package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import java.io.DataInput;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.openimaj.hadoop.mapreduce.stage.Stage;
import org.openimaj.hadoop.mapreduce.stage.StageProvider;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;
import org.openimaj.util.pair.IndependentPair;

import com.Ostermiller.util.CSVPrinter;


public class Values extends StageProvider{
	private String outputPath;
	/**
	 * Assign the output path for the stage
	 * @param outputPath
	 */
	public Values(String outputPath) {
		this.outputPath = outputPath;
	}
	public static final String ARGS_KEY = "INDEX_ARGS";
	/**
	 * Emits each word with the total number of times the word was seen
	 * @author ss
	 *
	 */
	public static class Map extends Mapper<Text,BytesWritable,NullWritable,Text>{
		
		public static String[] options;
		private static HashMap<String, IndependentPair<Long, Long>> wordIndex;
		private static HashMap<Long, IndependentPair<Long, Long>> timeIndex;
		private StringWriter swriter;
		private CSVPrinter writer;

		public Map() {
			// TODO Auto-generated constructor stub
		}
		
		protected static synchronized void loadOptions(Mapper<Text,BytesWritable,NullWritable,Text>.Context context) throws IOException {
			if (options == null) {
				try {
					options = context.getConfiguration().getStrings(ARGS_KEY);
					wordIndex = WordIndex.readWordCountLines(options[0]);
					timeIndex = TimeIndex.readTimeCountLines(options[0]);
				} catch (Exception e) {
					throw new IOException(e);
				}
			}
		}

		@Override
		protected void setup(Mapper<Text,BytesWritable,NullWritable,Text>.Context context) throws IOException, InterruptedException {
			loadOptions(context);
			swriter = new StringWriter();
			writer = new CSVPrinter(swriter);
		}

		public void map(final Text key, BytesWritable value, final Mapper<Text,BytesWritable,NullWritable,Text>.Context context){
			try {
				IndependentPair<Long, Long> wordIndexPair = wordIndex.get(key.toString());
				if(wordIndexPair == null) return;
				final long wordI = wordIndexPair.secondObject();
				IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()){
					@Override
					protected Object readValue(DataInput in) throws IOException {
						WordDFIDF idf = new WordDFIDF();
						idf.readBinary(in);
						long timeI = timeIndex.get(idf.timeperiod).secondObject();
						writer.writeln(new String[]{wordI + "",timeI + "",idf.wf + "",idf.tf + "",idf.Twf + "", idf.Ttf + ""});
						return new Object();
					}
				});
				writer.flush();
			} catch (IOException e) {
				System.err.println("Couldnt read timeperiod from word: " + key);
			}
			
		}
		
		public void cleanup(Mapper<Text,BytesWritable,NullWritable,Text>.Context context){
			try {
				context.write(NullWritable.get(), new Text(this.swriter.toString()));
			} catch (Exception e) {
				System.err.println("Couldn't cleanup!");
			}
		}
	}
	/**
	 * Writes each word,count
	 * @author ss
	 *
	 */
	public static class Reduce extends Reducer<NullWritable,Text,NullWritable,Text>{
		public Reduce() {
			// TODO Auto-generated constructor stub
		}
		public void reduce(NullWritable timeslot, Iterable<Text> manylines, Reducer<NullWritable,Text,NullWritable,Text>.Context context){
			try {
				for (Text lines : manylines) {
					context.write(NullWritable.get(), new Text(lines.toString() ));
					return;
				}
				
			} catch (Exception e) {
				System.err.println("Couldn't reduce to final file");
			}
		}
	}
	@Override
	public Stage stage() {
		return new Stage() {
			@Override
			public Job stage(Path[] inputs, Path output, Configuration conf) throws IOException {
				Job job = new Job(conf);
				
				job.setInputFormatClass(SequenceFileInputFormat.class);
				job.setOutputKeyClass(NullWritable.class);
				job.setOutputValueClass(Text.class);
				job.setOutputFormatClass(TextOutputFormat.class);
				job.setJarByClass(this.getClass());
			
				SequenceFileInputFormat.setInputPaths(job, inputs);
				TextOutputFormat.setOutputPath(job, output);
				TextOutputFormat.setCompressOutput(job, false);
				job.setMapperClass(Values.Map.class);
				job.setReducerClass(Values.Reduce.class);
				job.setNumReduceTasks(1);
				job.getConfiguration().setStrings(Values.ARGS_KEY, new String[]{outputPath.toString()});
				return job;
			}
			
			@Override
			public String outname() {
				return "values";
			}
		};
	}

}
