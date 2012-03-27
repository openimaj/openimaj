package org.openimaj.hadoop.tools.twitter.token.outputmode.correlation;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.Array2DRowFieldMatrix;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.RealMatrixImpl;
import org.apache.commons.math.stat.correlation.PearsonsCorrelation;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.hsqldb.util.CSVWriter;
import org.openimaj.hadoop.tools.HadoopToolsUtil;
import org.openimaj.hadoop.tools.twitter.utils.TweetCountWordMap;
import org.openimaj.hadoop.tools.twitter.utils.WordDFIDF;
import org.openimaj.io.IOUtils;
import org.openimaj.twitter.finance.YahooFinanceData;

import com.Ostermiller.util.CSVPrinter;

public class WordValueCorrelationReducer extends Reducer<Text, BytesWritable, NullWritable, Text>{
	
	static YahooFinanceData finance;
	private static long[] financesTimes;
	protected static synchronized void loadOptions(Reducer<Text,BytesWritable,NullWritable,Text>.Context context) throws IOException {
		if (finance == null) {
			Path financeLoc = new Path(context.getConfiguration().getStrings(CorrelateWordTimeSeries.FINANCE_DATA)[0]);
			FileSystem fs = HadoopToolsUtil.getFileSystem(financeLoc);
			finance = IOUtils.read(fs.open(financeLoc),YahooFinanceData.class);
			financesTimes = finance.timeperiods();
		}
	}

	private HashMap<Long, TweetCountWordMap> tweetWordMap;

	@Override
	protected void setup(Reducer<Text,BytesWritable,NullWritable,Text>.Context context) throws IOException, InterruptedException {
		loadOptions(context);
	}
	
	
	/**
	 * For each word,
	 */
	protected void reduce(
		Text word, Iterable<BytesWritable> idfvalues, 
		Reducer<Text,BytesWritable,NullWritable,Text>.Context context) throws IOException ,InterruptedException 
	{
		Map<Integer,WordDFIDF> held = new HashMap<Integer,WordDFIDF>();
		// Prepare for all times, some times may not contain a given word
		for (int i = 0; i < financesTimes.length; i++) {
			held.put(i, new WordDFIDF());
		}
		// Sum tf and wf across for each time period
		for (BytesWritable idfbytes : idfvalues) {
			WordDFIDF idf = IOUtils.deserialize(idfbytes.getBytes(), WordDFIDF.class);
			WordDFIDF current = held.get(idf.timeperiod);
			current.tf += idf.tf;
			current.wf += idf.wf;
			current.Ttf = idf.Ttf;
			current.Twf = idf.Twf;
		}
		// Prepare the contents of the matrix
		double[][] tocorr = new double[2][financesTimes.length];
		for (int i = 0; i < tocorr[0].length; i++) {
			tocorr[0][i] = held.get(i).dfidf();
		}
		
		for (String ticker : finance.labels()) {
			try{
				tocorr[1] = finance.results().get(ticker);
				BlockRealMatrix m = new BlockRealMatrix(tocorr);
				// Calculate and write pearsons correlation
				PearsonsCorrelation pcorr = new PearsonsCorrelation(m.transpose());
				double corr = pcorr.getCorrelationMatrix().getEntry(0, 1);
				double pval = pcorr.getCorrelationPValues().getEntry(0, 1);
				StringWriter swrit = new StringWriter();
				CSVPrinter csvp = new CSVPrinter(swrit);
				csvp.write(new String[]{word.toString(),ticker,""+corr,""+pval});
				csvp.flush();
				context.write(NullWritable.get(), new Text(swrit.toString()));
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	};
}
