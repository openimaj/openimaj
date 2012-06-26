package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.mortbay.util.ByteArrayOutputStream2;
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
public class MapValuesByTime extends Mapper<Text,BytesWritable,LongWritable,BytesWritable>{
	
	
	/**
	 * construct the map instance (do nothing)
	 */
	public MapValuesByTime() {
		// TODO Auto-generated constructor stub
	}
	
	public void setup(Mapper<Text,BytesWritable,LongWritable,BytesWritable>.Context context) throws IOException, InterruptedException {
		loadOptions(context);
	}
	private static String[] options;
	private static HashMap<String, IndependentPair<Long, Long>> wordIndex;

	protected static synchronized void loadOptions(Mapper<Text,BytesWritable,LongWritable,BytesWritable>.Context context) throws IOException {
		if (options == null) {
			try {
				options = context.getConfiguration().getStrings(Values.ARGS_KEY);
				wordIndex = WordIndex.readWordCountLines(options[0]);
				System.out.println("Wordindex loaded: " + wordIndex.size());
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}
	
	@Override
	public void map(final Text key, BytesWritable value, final Mapper<Text,BytesWritable,LongWritable,BytesWritable>.Context context) throws IOException, InterruptedException{
		try {
			if(!wordIndex.containsKey(key.toString())) return;
			final int wordI = (int)((long)(wordIndex.get(key.toString()).secondObject()));
			IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()){
				@Override
				protected Object readValue(DataInput in) throws IOException {
					WordDFIDF idf = new WordDFIDF();
					idf.readBinary(in);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					DataOutputStream dos = new DataOutputStream(baos);
					idf.writeBinary(dos);
					dos.writeInt(wordI);
					dos.flush();
					dos.close();
					try {
						context.write(new LongWritable(idf.timeperiod), new BytesWritable(baos.toByteArray()));
					} catch (InterruptedException e) {
						throw new IOException(e);
					}
					return new Object();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Couldnt read word or timeperiod from word: " + key);
		}
		
	}
}