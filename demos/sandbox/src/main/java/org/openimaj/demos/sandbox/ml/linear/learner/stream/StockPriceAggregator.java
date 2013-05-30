package org.openimaj.demos.sandbox.ml.linear.learner.stream;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openimaj.util.data.Context;
import org.openimaj.util.function.Operation;
import org.openimaj.util.stream.CollectionStream;
import org.openimaj.util.stream.Stream;
import org.openimaj.util.stream.window.SequentialStreamAggregator;

/**
 * Given a paired stream of user-word-counts and ticker-price, implement a {@link SequentialStreamAggregator}
 * with a comparator which tests stock prices. Identical stock prices (i.e. the same for each ticker) are combined
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public final class StockPriceAggregator
		extends
		SequentialStreamAggregator<Context>
{
	private static final class StockPriceComparator
			implements
			Comparator<Context>
	{
		private double thresh;

		public StockPriceComparator(double thresh) {
			this.thresh = thresh;
		}

		@Override
		public int compare(Context o1,Context o2)
		{
			Map<String, Double> a = o1.getTyped("averageticks");
			Map<String, Double> b = o2.getTyped("averageticks");

			Set<String> sharedKeys = new HashSet<String>(a.keySet());
			sharedKeys.addAll(b.keySet());
			double diff = 0;
			for (String ticker : sharedKeys) {
				diff += a.get(ticker) - b.get(ticker);
			}
			if(Math.abs(diff) < this.thresh) return 0;
			else if(diff<0)return -1;
			else if(diff>0)return 1;
			else return 0;
		}
	}


	public StockPriceAggregator(double thresh)
	{
		super(new StockPriceComparator(thresh));
	}

	@Override
	public Context combine(List<Context> window)
	{
		Map<String, Map<String, Double>> combinedUserWords = new HashMap<String, Map<String,Double>>();
		int nItems = window.size();
		Map<String, Double> stocks = null;
		long timestamp = window.get(0).getTyped("timestamp");
		for (Context itemAggr : window) {
			Map<String, Map<String, Double>> bagofwords = itemAggr.getTyped("bagofwords");
			stocks = itemAggr.getTyped("averageticks"); // They should all be the same
			for (Entry<String, Map<String, Double>> es : bagofwords.entrySet()) {
				addUsers(combinedUserWords,es,nItems);
			}
		}
		Context newC = window.get(0).clone();
		newC.put("timestamp", timestamp);
		newC.put("bagofwords", combinedUserWords);
		newC.put("averageticks", stocks);
		return newC;
	}

	private void addUsers(Map<String, Map<String, Double>> combinedUserWords, Entry<String, Map<String, Double>> es, int nItems) {
		Map<String, Double> userWords = combinedUserWords.get(es.getKey());
		if(userWords == null) combinedUserWords.put(es.getKey(), userWords = new HashMap<String, Double>());
		for (Entry<String, Double> wordTotals : es.getValue().entrySet()) {
			addWord(userWords,wordTotals,nItems);
		}
	}

	private void addWord(Map<String, Double> userWords,Entry<String, Double> wordTotals, int nItems) {
		String word = wordTotals.getKey();
		Double wordTotal = userWords.get(word);
		if(wordTotal == null) wordTotal = 0d;
		userWords.put(word, wordTotal + (wordTotals.getValue()/nItems));
	}

	/**
	 * tests
	 * @param args
	 */
	public static void main(String[] args) {
		List<Context> coll = new ArrayList<Context>();

		Map<String,Map<String,Double>> t1 = sparcify("u1: this is cheese","u2: cheese is good","u3: i like cheese");
		Map<String,Map<String,Double>> t2 = sparcify("u1: it is still cheese","u3: cheese is good","u4: i like cheese");
		Map<String,Map<String,Double>> t3 = sparcify("u2: this is cheese");
		Map<String,Map<String,Double>> t4 = sparcify("u1: this is cheese","u2: cheese is good","u3: i like cheese");
		Map<String,Double> price1 = sparcifyPrice("p1: 100","p2: 200");
		Map<String,Double> price2 = sparcifyPrice("p1: 100","p2: 201");


		Context c = new Context();
		c.put("timestamp", 0l); c.put("bagofwords", t1); c.put("averageticks", price1);
		coll.add(c);
		c = new Context();
		c.put("timestamp", 1l); c.put("bagofwords", t2); c.put("averageticks", price1);
		coll.add(c);
		c = new Context();
		c.put("timestamp", 2l); c.put("bagofwords", t3); c.put("averageticks", price2);
		coll.add(c);
		c = new Context();
		c.put("timestamp", 3l); c.put("bagofwords", t4); c.put("averageticks", price1);
		coll.add(c);
		Stream<Context> stream = new CollectionStream<Context>(coll);
		stream.transform(new StockPriceAggregator(0.0001)).forEach(
			new Operation<Context>() {

			@Override
			public void perform(Context object) {
				System.out.println(object.getTyped("timestamp"));
				System.out.println(object.getTyped("bagofwords"));
				System.out.println(object.getTyped("averageticks"));
			}
		});

	}

	private static Map<String, Double> sparcifyPrice(String ...strings ) {

		Map<String, Double> ret = new HashMap<String, Double>();
		for (String string : strings) {
			String[] tickPrice = string.split(":");
			ret.put(tickPrice[0].trim(), Double.parseDouble(tickPrice[1].trim()));
		}
		return ret ;
	}

	private static Map<String, Map<String, Double>> sparcify(String ...strings) {
		Map<String, Map<String, Double>> ret = new HashMap<String, Map<String,Double>>();
		for (String string : strings) {
			String[] userWords = string.split(":");
			String user = userWords[0].trim();
			String[] words = userWords[1].trim().split(" ");

			Map<String,Double> wordCounts = ret.get(user);
			if(wordCounts == null) ret.put(user, wordCounts = new HashMap<String, Double>());
			ret.put(user, wordCounts);
			for (String word : words) {
				Double count = wordCounts.get(word);
				if(count == null) count = 0d;
				wordCounts.put(word, count+1);
			}
		}
		return ret;
	}


}