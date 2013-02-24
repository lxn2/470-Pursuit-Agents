package cpsc470.bayes;

import java.util.Set;

import aima.core.probability.CategoricalDistribution;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;

public class Utils {

	/**
	 * Compute root-mean-square-error between two comparable categorical distributions.
	 * 
	 * @param distrib1
	 * @param distrib2
	 * @return RMSE between distributions.
	 * @throws NullPointerException if either distribution is null.
	 * @throws IllegalArgumentException if distributions not over the same random variables.
	 */
	public static double computeDistributionRMSE(CategoricalDistribution distrib1, CategoricalDistribution distrib2) {

		double rmse = 0.0;
		double sumOfSquares = 0.0;
		Set<RandomVariable> dist1RandVars, dist2RandVars;
		
		// TODO PROBLEM 2: FILL THIS IN
		
		if(distrib1.equals(null) || distrib2.equals(null))
			throw new NullPointerException();
		
		dist1RandVars = distrib1.getFor();
		dist2RandVars = distrib2.getFor();
		
		if(!dist1RandVars.equals(dist2RandVars))
			throw new IllegalArgumentException();
		
		int numVariables = dist1RandVars.size();
		for(RandomVariable w : dist1RandVars)
		{
			double probabilityw1 = distrib1.getValue(w);
			double probabilityw2 = distrib2.getValue(w);
			double difference = probabilityw1 - probabilityw2;
			double differenceSquared = Math.pow(difference, 2);
			sumOfSquares += differenceSquared;
		}
		
		double mean = sumOfSquares / (double) numVariables;
		rmse = Math.sqrt(mean);
		
		return rmse;
	}

	/**
	 * Simple O(n) search for random variable in Bayesian network with given name.
	 * @param name
	 * @param bn
	 * @return Random variable with given name if it exists, null otherwise.
	 */
	public static RandomVariable getRV(String name, BayesianNetwork bn) {
		for (RandomVariable rv : bn.getVariablesInTopologicalOrder()) {
			if (rv.getName().equals(name)) {
				return rv;
			}
		}
		return null;
	}

	/**
	 * Convert random variable to meet requirements of ProbUtil#checkValidRandomVariableName().
	 * Specifically, trim and make sure first character is upper case.
	 * 
	 * @param rvName Input name.
	 * @return Normalized name.
	 */
	public static String normalizeRVName(String rvName) {
		rvName = rvName.trim();
		if (rvName.substring(0, 1).toLowerCase().equals(rvName.substring(0, 1))) {
			if (rvName.length() == 1) {
				rvName = rvName.toUpperCase();
			} else if (rvName.length() > 1){
				rvName = rvName.substring(0, 1).toUpperCase() + rvName.substring(1);
			}
		}
		
		return rvName;
	}

}
