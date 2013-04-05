package org.openimaj.ml.linear.data;

import gov.sandia.cognition.math.matrix.Matrix;
import gov.sandia.cognition.math.matrix.MatrixEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

public class BiconvexIncrementalDataGenerator  {
	
	BiconvexDataGenerator dgen ;
	private Map<String, Double> rety;
	private Iterator<Map<String, Map<String, Double>>> retxIter;
	
	/**
	 * 
	 * @param nusers The number of users (U is users x tasks, X is features x users)
	 * @param nfeatures The number of features (W is features x tasks) 
	 * @param ntasks The number of tasks (Y is 1 x tasks)
	 * @param sparcity The chance that a row of U or W is zeros
	 * @param indw If true, there is a column of W per task
	 * @param indu If true, there is a column of U per task
	 * @param seed If greater than or equal to zero, the rng backing this generator is seeded
	 * @param noise The random noise added to Y has random values for each Y ranging from -noise to noise
	 */
	public BiconvexIncrementalDataGenerator(
			int nusers, int nfeatures,int ntasks, 
			double sparcity, double xsparcity,
			boolean indw, boolean indu,
			int seed,double noise
			
	){
		dgen = new BiconvexDataGenerator(nusers, nfeatures, ntasks, sparcity, xsparcity,indw, indu, seed, noise);
	}
	
	public IndependentPair<Map<String,Map<String,Double>>,Map<String,Double>> generate() {
		// We generate a batch, and output incrementally per user
		// For each user we assume the state Y is the same for each, while X is a sparse matrix where only the user's
		// message is set (which itself might be sparse)
		while(this.retxIter == null || !this.retxIter.hasNext()){
			prepareXY();
		}
		Map<String, Map<String, Double>> x = retxIter.next();
		return IndependentPair.pair(x, rety);
	}

	private void prepareXY() {
		Pair<Matrix> xy = dgen.generate();
		this.retxIter = prepareX(xy.getFirstObject()).iterator();
		this.rety = prepareY(xy.getSecondObject());
	}

	private Map<String, Double> prepareY(Matrix secondObject) {
		Map<String, Double> ret = new HashMap<String, Double>();
		for (MatrixEntry me : secondObject) {
			ret.put(me.getColumnIndex() + "", me.getValue());
		}
		return ret ;
	}

	private List<Map<String, Map<String, Double>>> prepareX(Matrix firstObject) {
		HashMap<String, Map<String, Double>> ret = new HashMap<String,Map<String,Double>>();
		for (MatrixEntry matrixEntry : firstObject) {
			int user = matrixEntry.getColumnIndex();
			int word = matrixEntry.getRowIndex();
			double v = matrixEntry.getValue();
			if(v!=0){
				String userName = userName(user);
				Map<String, Double> userMap = ret.get(userName);
				if(userMap == null){
					ret.put(userName, userMap = new HashMap<String, Double>());
				}
				String wordName = wordName(word);
				userMap.put(wordName, v);
			}
		}
		List<Map<String, Map<String, Double>>> retList = new ArrayList<Map<String,Map<String,Double>>>();
		for (Entry<String, Map<String, Double>> map : ret.entrySet()) {
			Map<String, Map<String, Double>> userMap = new HashMap<String, Map<String,Double>>();
			userMap.put(map.getKey(), map.getValue());
			retList.add(userMap);
		}
		return retList;
	}

	private String wordName(int word) {
		return String.format("word_%d",word);
	}

	private String userName(int user) {
		return String.format("user_%d",user);
	}
	

}
