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
package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
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
import org.apache.hadoop.mapreduce.Reducer;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.util.pair.IndependentPair;

import com.Ostermiller.util.CSVPrinter;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLInt64;
import com.jmatio.types.MLSparse;

/**
 * Writes each word,count
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReduceValuesByTime extends Reducer<LongWritable, BytesWritable, NullWritable, Text> {
	/**
	 * construct the reduce instance, do nothing
	 */
	public ReduceValuesByTime() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setup(Reducer<LongWritable, BytesWritable, NullWritable, Text>.Context context) throws IOException, InterruptedException {
		loadOptions(context);
	}

	private static String[] options;
	private static HashMap<String, IndependentPair<Long, Long>> wordIndex;
	private static HashMap<Long, IndependentPair<Long, Long>> timeIndex;
	private static String valuesLocation;
	private static boolean matlabOut;

	protected static synchronized void loadOptions(Reducer<LongWritable, BytesWritable, NullWritable, Text>.Context context) throws IOException {
		if (options == null) {
			try {
				options = context.getConfiguration().getStrings(Values.ARGS_KEY);
				matlabOut = context.getConfiguration().getBoolean(Values.MATLAB_OUT, false);
				timeIndex = TimeIndex.readTimeCountLines(options[0]);
				if (matlabOut) {
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
	public void reduce(LongWritable timeslot, Iterable<BytesWritable> manylines, Reducer<LongWritable, BytesWritable, NullWritable, Text>.Context context) throws IOException, InterruptedException {
		try {
			if (matlabOut) {
				System.out.println("Creating matlab file for timeslot: " + timeslot);
				createWriteToMatlab(timeslot, manylines);
			}
			else {
				final StringWriter swriter = new StringWriter();
				final CSVPrinter writer = new CSVPrinter(swriter);
				for (BytesWritable word : manylines) {
					ByteArrayInputStream bais = new ByteArrayInputStream(word.getBytes());
					DataInputStream dis = new DataInputStream(bais);
					WordDFIDF idf = new WordDFIDF();
					idf.readBinary(dis);
					int timeI = (int) ((long) timeIndex.get(idf.timeperiod).secondObject());
					int wordI = dis.readInt();
					writer.writeln(new String[] { wordI + "", timeI + "", idf.wf + "", idf.tf + "", idf.Twf + "", idf.Ttf + "" });
					writer.flush();
					swriter.flush();
				}
				context.write(NullWritable.get(), new Text(swriter.toString()));
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Couldn't reduce to final file");
			throw new IOException(e);
		}
	}

	private void createWriteToMatlab(LongWritable timeslot, Iterable<BytesWritable> manylines) throws IOException {
		System.out.println("Creating matlab file for timeslot: " + timeslot);
		MLSparse matarr = new MLSparse(String.format("values_%d", timeslot.get()), new int[] { wordIndex.size(), 2 }, 0, wordIndex.size() * 2);
		long Ttf = 0;
		long tf = 0;
		boolean set = false;
		for (BytesWritable word : manylines) {
			ByteArrayInputStream bais = new ByteArrayInputStream(word.getBytes());
			DataInputStream dis = new DataInputStream(bais);
			WordDFIDF idf = new WordDFIDF();
			idf.readBinary(dis);
			int wordI = dis.readInt();
			// writer.writeln(new String[]{wordI + "",timeI + "",idf.wf +
			// "",idf.tf + "",idf.Twf + "", idf.Ttf + ""});
			// writer.flush();
			// swriter.flush();
			if (!set) {
				tf = idf.tf;
				Ttf = idf.Ttf;
				set = true;
			}
			else {
				if (tf != idf.tf)
					throw new IOException("Error writing matlab file, tf doesn't match");
				if (Ttf != idf.Ttf)
					throw new IOException("Error writing matlab file, Ttf doesn't match");
			}
			matarr.set((double) idf.wf, wordI, 0);
			matarr.set((double) idf.Twf, wordI, 1);
		}
		MLInt64 tfMat = new MLInt64(String.format("tf_%d", timeslot.get()), new long[][] { new long[] { tf } });
		MLInt64 TtfMat = new MLInt64(String.format("Ttf_%d", timeslot.get()), new long[][] { new long[] { Ttf } });
		ArrayList<MLArray> list = new ArrayList<MLArray>();
		list.add(tfMat);
		list.add(TtfMat);
		list.add(matarr);
		Path outLoc = new Path(String.format(valuesLocation, timeslot.get()));
		FileSystem fs = HadoopToolsUtil.getFileSystem(outLoc);
		FSDataOutputStream os = fs.create(outLoc);
		new MatFileWriter(Channels.newChannel(os), list);
	}
}