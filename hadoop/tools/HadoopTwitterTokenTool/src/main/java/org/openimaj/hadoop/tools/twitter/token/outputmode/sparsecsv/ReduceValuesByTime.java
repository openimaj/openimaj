package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.util.pair.IndependentPair;

import com.Ostermiller.util.CSVPrinter;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLSparse;

/**
 * Writes each word,count
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReduceValuesByTime extends Reducer<LongWritable,BytesWritable,NullWritable,Text>{
	/**
	 * construct the reduce instance, do nothing
	 */
	public ReduceValuesByTime() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void setup(Reducer<LongWritable,BytesWritable,NullWritable,Text>.Context context) throws IOException, InterruptedException {
		loadOptions(context);
	}
	private static String[] options;
	private static HashMap<String, IndependentPair<Long, Long>> wordIndex;
	private static HashMap<Long, IndependentPair<Long, Long>> timeIndex;
	private static String valuesLocation;
	private static boolean matlabOut;

	protected static synchronized void loadOptions(Reducer<LongWritable,BytesWritable,NullWritable,Text>.Context context) throws IOException {
		if (options == null) {
			try {
				options = context.getConfiguration().getStrings(Values.ARGS_KEY);
				matlabOut = context.getConfiguration().getBoolean(Values.MATLAB_OUT, false);
				timeIndex = TimeIndex.readTimeCountLines(options[0]);
				if(matlabOut) {
					wordIndex = WordIndex.readWordCountLines(options[0]);
					valuesLocation = options[0] + "/values/values.%d.mat";
				}
				System.out.println("timeindex loaded: " + timeIndex.size());
			} catch (Exception e) {
				throw new IOException(e);
			}
		}
	}
	
	@Override
	public void reduce(LongWritable timeslot, Iterable<BytesWritable> manylines, Reducer<LongWritable,BytesWritable,NullWritable,Text>.Context context){
		try {
			if(matlabOut) {
				createWriteToMatlab(timeslot,manylines);
			}
			else{			
				final StringWriter swriter = new StringWriter();
				final CSVPrinter writer = new CSVPrinter(swriter);
				for (BytesWritable word : manylines) {
					ByteArrayInputStream bais = new ByteArrayInputStream(word.getBytes());
					DataInputStream dis = new DataInputStream(bais);
					WordDFIDF idf = new WordDFIDF();
					idf.readBinary(dis);
					int timeI = (int)((long)timeIndex.get(idf.timeperiod).secondObject());
					int wordI = dis.readInt();
					writer.writeln(new String[]{wordI + "",timeI + "",idf.wf + "",idf.tf + "",idf.Twf + "", idf.Ttf + ""});
					writer.flush();
					swriter.flush();
				}
				context.write(NullWritable.get(), new Text(swriter.toString()));
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Couldn't reduce to final file");
		}
	}

	private void createWriteToMatlab(LongWritable timeslot,Iterable<BytesWritable> manylines) throws IOException {
		
		
		
		MLSparse matarr = new MLSparse(String.format("values_%d",timeslot.get()), new int[]{wordIndex.size(),4}, 0, wordIndex.size() * 4);
		for (BytesWritable word : manylines) {
			ByteArrayInputStream bais = new ByteArrayInputStream(word.getBytes());
			DataInputStream dis = new DataInputStream(bais);
			WordDFIDF idf = new WordDFIDF();
			idf.readBinary(dis);
			int wordI = dis.readInt();
//			writer.writeln(new String[]{wordI + "",timeI + "",idf.wf + "",idf.tf + "",idf.Twf + "", idf.Ttf + ""});
//			writer.flush();
//			swriter.flush();
			matarr.set((double)idf.wf, wordI, 0);
			matarr.set((double)idf.tf, wordI, 1);
			matarr.set((double)idf.Twf, wordI, 2);
			matarr.set((double)idf.Ttf, wordI, 3);
		}
		
		ArrayList<MLArray> list = new ArrayList<MLArray>();
		list.add(matarr);
		Path outLoc = new Path(String.format(valuesLocation, timeslot.get()));
		FileSystem fs = HadoopToolsUtil.getFileSystem(outLoc);
		FSDataOutputStream os = fs.create(outLoc);
		new MatFileWriter(os,list );
	}
}