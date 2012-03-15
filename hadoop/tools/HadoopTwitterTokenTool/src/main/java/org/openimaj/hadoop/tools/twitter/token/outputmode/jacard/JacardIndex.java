package org.openimaj.hadoop.tools.twitter.token.outputmode.jacard;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Scanner;

import org.openimaj.io.ReadWriteableASCII;

import com.Ostermiller.util.CSVParser;
import com.Ostermiller.util.CSVPrinter;

/**
 * An index encoding the difference between two sets
 * @author ss
 *
 */
public class JacardIndex implements ReadWriteableASCII{

	/**
	 * Number of unique words in this time period
	 */
	public long timePeriodWords;
	/**
	 * Number of unique words before this time period
	 */
	public long historicWords;
	/**
	 * The number of words forming the intersection between now and historic words
	 */
	public long intersection;
	/**
	 * The number of words forming the union between now and historic words
	 */
	public long union;
	/**
	 * current time period
	 */
	public long time;
	/**
	 * The jacard index is: J(A,B) = |intersection(A,B)| / |union(A,B)| for this time period
	 */
	public double jacardIndex;

	/**
	 * @param time
	 * @param timePeriodWords
	 * @param historicWords
	 * @param intersection
	 * @param union
	 */
	public JacardIndex(long time, long timePeriodWords, long historicWords, long intersection,long union) {
		this.time = time;
		this.timePeriodWords = timePeriodWords;
		this.historicWords = historicWords;
		this.intersection = intersection;
		this.union = union;
		this.jacardIndex = (double)intersection/(double)union;
	}

	private JacardIndex() {
	}

	@Override
	public void readASCII(Scanner in) throws IOException {
		fromString(in.nextLine(),this);
	}

	private static void fromString(String nextLine, JacardIndex i) throws IOException {
		StringReader reader = new StringReader(nextLine);
		CSVParser csvreader = new CSVParser(reader);
		String[] line = csvreader.getLine();
		i.time = Long.parseLong(line[0]);
		i.timePeriodWords = Long.parseLong(line[1]);
		i.historicWords = Long.parseLong(line[2]);
		i.intersection = Long.parseLong(line[3]);
		i.union = Long.parseLong(line[4]);
		i.jacardIndex = (double)i.intersection/(double)i.union;
		
	}

	@Override
	public String asciiHeader() {
		return "";
	}

	@Override
	public void writeASCII(PrintWriter out) throws IOException {
		CSVPrinter writer = new CSVPrinter(out);
		writer.write(new String[]{
				"" + this.time,
				"" + this.timePeriodWords,
				"" + this.historicWords,
				"" + intersection,
				"" + union
		});
	}

	/**
	 * Read a new jacard index from a comma separated line
	 * @param next
	 * @return new JacardIndex
	 * @throws IOException
	 */
	public static JacardIndex fromString(String next) throws IOException {
		JacardIndex ind = new JacardIndex();
		fromString(next,ind);
		return ind;
	}

}
