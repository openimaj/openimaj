package org.openimaj.hadoop.tools.twitter.token.outputmode.sparsecsv;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * Writes each word,count
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class ReduceValuesByWord extends Reducer<NullWritable,Text,NullWritable,Text>{
	/**
	 * construct the reduce instance, do nothing
	 */
	public ReduceValuesByWord() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public void reduce(NullWritable timeslot, Iterable<Text> manylines, Reducer<NullWritable,Text,NullWritable,Text>.Context context){
		try {
			for (Text lines : manylines) {
				context.write(NullWritable.get(), new Text(lines.toString() ));
			}
			
		} catch (Exception e) {
			System.err.println("Couldn't reduce to final file");
		}
	}
}