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
package org.openimaj.hadoop.tools.sequencefile.index;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.SequenceFile.Reader;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.MapContext;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileRecordReader;

public class RecordIndexMapper extends Mapper<Text, BytesWritable, Text, Text>  {
	
	
	private SequenceFile.Reader reader;

	@Override
	protected void setup(Context context)  throws IOException, InterruptedException {
		try {
			Field readerField = MapContext.class.getDeclaredField("reader");
			readerField.setAccessible(true);
			@SuppressWarnings("unchecked")
			RecordReader<Text, BytesWritable> reader = (RecordReader<Text, BytesWritable>) readerField.get(context);
			
			Field realField = reader.getClass().getDeclaredField("real");
			realField.setAccessible(true);
			@SuppressWarnings("unchecked")
			SequenceFileRecordReader<Text,BytesWritable> realReader = (SequenceFileRecordReader<Text, BytesWritable>) realField.get(reader);
			
			Field inField = realReader.getClass().getDeclaredField("in");
			inField.setAccessible(true);
			this.reader = (Reader) inField.get(realReader);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void map(Text index, BytesWritable urlLine, Context context) {
		
		InputSplit inputSplit = context.getInputSplit();
		if(inputSplit instanceof FileSplit){
			FileSplit fInputSplit = (FileSplit)inputSplit;
			HashMap<String,String> metaInfo = new HashMap<String,String>();
			metaInfo.put("location", fInputSplit.getPath().toString());
//			metaInfo.put("mimetype", ""+fInputSplit.getStart());
			try {
				metaInfo.put("offset", ""+this.reader.getPosition());
				context.write(index, new Text(hashToString(metaInfo)));
			}
			catch (IOException e) {} 
			catch (InterruptedException e) {}
		}
	}

	private String hashToString(HashMap<String, String> metaInfo) {
		String returnString = "{";
		for(String key : metaInfo.keySet()){
			returnString += "\"" + key + "\"" + ": " + "\"" + metaInfo.get(key) + "\"" + ","; 
		}
		returnString += "}";
		return returnString;
	}
	
	
//	public String getMimeType(BytesWritable data){
////		MagicMatch match;
////		try {
////			match = Magic.getMagicMatch(((BytesWritable)value).getBytes());
////			String ext = match.getExtension();
////			if(ext.trim().length()!=0) name += "." + ext;
////		} catch(Exception e){
////			System.out.println("Failed!");
////		}
//	}
	
}
