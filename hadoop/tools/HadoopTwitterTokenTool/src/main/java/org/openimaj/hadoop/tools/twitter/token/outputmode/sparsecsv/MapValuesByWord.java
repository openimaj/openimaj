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