package org.openimaj.text.nlp.namedentity;

import org.openimaj.text.nlp.namedentity.YagoCompanyAnnotatorEvaluator.FileEntityLocation;

public class FETest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String file ="file";
		int start = 1;
		int finish = 3;
		FileEntityLocation a = new FileEntityLocation();
		a.file=file;
		a.start=start;
		a.stop=finish;
		FileEntityLocation b = new FileEntityLocation();
		b.file=file;
		b.start=start;
		b.stop=finish;
		if(b.equals(a))System.out.println("They are equal");
	}

}
