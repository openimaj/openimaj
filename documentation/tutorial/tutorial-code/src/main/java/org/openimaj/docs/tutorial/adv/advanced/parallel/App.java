package org.openimaj.docs.tutorial.adv.advanced.parallel;

import org.openimaj.util.function.Operation;
import org.openimaj.util.parallel.Parallel;

/**
 * OpenIMAJ Hello world!
 * 
 */
public class App {
	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Parallel.forIndex(0, 10, 1, new Operation<Integer>() {
			@Override
			public void perform(Integer i) {
				System.out.println(i);
			}
		});
	}
}
