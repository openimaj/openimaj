package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import java.io.DataInput;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;
import org.openimaj.util.pair.IndependentPair;

import com.Ostermiller.util.CSVPrinter;

/**
 * Emits each word with the total number of times the word was seen
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class MapValuesByWord extends Mapper<Text,BytesWritable,NullWritable,Text>{
	
	private static String[] options;
	private static HashMap<String, IndependentPair<Long, Long>> wordIndex;
	private static HashMap<Long, IndependentPair<Long, Long>> timeIndex;

	/**
	 * construct the map instance (do nothing)
	 */
	public MapValuesByWord() {
		// TODO Auto-generated constructor stub
	}
	
	protected static synchronized void loadOptions(Mapper<Text,BytesWritable,NullWritable,Text>.Context context) throws IOException {
		if (options == null) {
			try {
				options = context.getConfiguration().getStrings(Values.ARGS_KEY);
				wordIndex = WordIndex.readWordCountLines(options[0]);
				timeIndex = TimeIndex.readTimeCountLines(options[0]);
				System.out.println("Wordindex loaded: " + wordIndex.size());
				System.out.println("timeindex loaded: " + timeIndex.size());
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	protected void setup(Mapper<Text,BytesWritable,NullWritable,Text>.Context context) throws IOException, InterruptedException {
		loadOptions(context);
	}

	@Override
	public void map(final Text key, BytesWritable value, final Mapper<Text,BytesWritable,NullWritable,Text>.Context context) throws IOException, InterruptedException{
		final StringWriter swriter = new StringWriter();
		final CSVPrinter writer = new CSVPrinter(swriter);
		try {
			IndependentPair<Long, Long> wordIndexPair = wordIndex.get(key.toString());
			if(key.toString().equals("!")){
				System.out.println("The string was: " + key);
				System.out.println("The string's pair was" + wordIndexPair);
				System.out.println("But the map's value for ! is: " + wordIndex.get("!"));
			}
			if(wordIndexPair == null) {
				
				return;
			}
			final long wordI = wordIndexPair.secondObject();
			IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()){
				@Override
				protected Object readValue(DataInput in) throws IOException {
					WordDFIDF idf = new WordDFIDF();
					idf.readBinary(in);
					IndependentPair<Long, Long> timePeriod = timeIndex.get(idf.timeperiod);
					if(timePeriod == null) return new Object();
					long timeI = timeIndex.get(idf.timeperiod).secondObject();
					writer.writeln(new String[]{wordI + "",timeI + "",idf.wf + "",idf.tf + "",idf.Twf + "", idf.Ttf + ""});
					writer.flush();
					swriter.flush();
					return new Object();
				}
			});
			context.write(NullWritable.get(), new Text(swriter.toString()));
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldnt read word or timeperiod from word: " + key);
		}
		
	}
}