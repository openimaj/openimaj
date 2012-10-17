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
package org.openimaj.hadoop.tools.twitter.token.outputmode.timeseries;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.io.wrappers.ReadableListBinary;

/**
 * given a list of configured words, emits only those words
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class SpecificWordSelectionMapper extends Mapper<Text, BytesWritable, Text, BytesWritable> {
	
	
	private static List<String> wordlist;

	@Override
	protected void setup(org.apache.hadoop.mapreduce.Mapper<Text,BytesWritable,Text,BytesWritable>.Context context) throws java.io.IOException ,InterruptedException {
		load(context);
	}

	private static void load(Mapper<Text,BytesWritable,Text,BytesWritable>.Context context) {
		if(wordlist == null){
			
			wordlist = Arrays.asList(context.getConfiguration().getStrings(SpecificWordStageProvider.WORD_TIME_SERIES));
		}
	};
	
	@Override
	protected void map(final Text key, BytesWritable value, final Mapper<Text,BytesWritable,Text,BytesWritable>.Context context) throws java.io.IOException ,InterruptedException {
		if(wordlist.contains(key.toString())){
			IOUtils.deserialize(value.getBytes(), new ReadableListBinary<Object>(new ArrayList<Object>()){
				@Override
				protected Object readValue(DataInput in) throws IOException {
					WordDFIDF idf = new WordDFIDF();
					idf.readBinary(in);
					try {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						IOUtils.writeBinary(baos, idf);
						context.write(key, new BytesWritable(baos.toByteArray()));
					} catch (InterruptedException e) {
						throw new IOException("");
					}
					return NullWritable.get();
				}
			});
		}
	};
}
