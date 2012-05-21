package org.openimaj.hadoop.tools.twitter.token.outputmode.correlation;

import java.io.IOException;
import java.io.StringReader;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import com.Ostermiller.util.CSVParser;

public class PValueWordMapper extends Mapper<LongWritable, Text, DoubleWritable, Text> {
	private static double maxp = -1;
	public PValueWordMapper(){
		
	}
	
	@Override
	public void setup(Mapper<LongWritable,Text,DoubleWritable,Text>.Context context) throws IOException ,InterruptedException {
		load(context);
	};
	private static void load(Mapper<LongWritable,Text,DoubleWritable,Text>.Context context) {
		maxp = context.getConfiguration().getFloat(CorrelateWordSort.MAXP_KEY, -1);
	}

	@Override
	public void map(LongWritable key, Text value, Mapper<LongWritable,Text,DoubleWritable,Text>.Context context) throws IOException ,InterruptedException {
		CSVParser csvp = new CSVParser(new StringReader(value.toString()));
		String[] linevals = csvp.getLine();
		double pvalue = Double.parseDouble(linevals[3]);
		if(new Double(pvalue).equals(Double.NaN)) return;
		if(maxp == -1 || pvalue < maxp){
			context.write(new DoubleWritable(pvalue), value);
		}
	};
}
