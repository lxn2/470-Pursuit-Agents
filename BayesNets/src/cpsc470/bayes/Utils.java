package cpsc470.bayes;

import java.util.Iterator;
import java.util.Set;

import aima.core.probability.CategoricalDistribution;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.domain.Domain;
import aima.core.probability.domain.FiniteDomain;

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
		int numValues = 1;
		
		// array holds variable names
		Object varArr[] = dist1RandVars.toArray(); //(RandomVariable[])
		// array holds domains corresponding to variables
		FiniteDomain varDomainsArr[] = new FiniteDomain[varArr.length];
		for(int variableI = 0; variableI < varArr.length; variableI++)
		{
			varDomainsArr[variableI] = (FiniteDomain) ((RandomVariable) varArr[variableI]).getDomain();
			numValues *= varDomainsArr[variableI].size();
		}
			
		// initialize values to the first value in domain and calculate
		Object valueArr[] = new Object[varArr.length];
		for(int variableI = 0; variableI < varArr.length; variableI++)
			valueArr[variableI] = varDomainsArr[variableI].getValueAt(0); 

		double probabilityw1 = distrib1.getValue(valueArr);
		double probabilityw2 = distrib2.getValue(valueArr);
		double difference = probabilityw1 - probabilityw2;
		double differenceSquared = Math.pow(difference, 2);
		sumOfSquares += differenceSquared;

		
		// flip values to get every combinations of values possible and calculate
		for(int valueCombo = 1; valueCombo < Math.pow(2, numVariables); valueCombo++) // Gets different combinations
			// of values for the whole set of variables
		{
			byte[] comboInBits = toBytes(valueCombo);
			int bitIndex = 0; // Bit index in each byte (7-0)
			int arrayIndex = comboInBits.length - 1; // Byte index for each set of 8 bits
			int bitValuesCovered = 0; // Keep track of how many values have changed
			while (arrayIndex >= 0 && bitValuesCovered < numVariables) // Changes variable values according to bit values
			{
				if ((comboInBits[arrayIndex] >> (bitIndex) & 1) == 1)
					valueArr[numVariables - bitValuesCovered - 1] = varDomainsArr[numVariables - bitValuesCovered - 1].getValueAt(1); 
				else
					valueArr[numVariables - bitValuesCovered - 1] = varDomainsArr[numVariables - bitValuesCovered - 1].getValueAt(0); 
				bitIndex++;
				bitValuesCovered++;
				if (bitIndex == 8)
				{
					bitIndex = 0;
					arrayIndex--;
				}
			}
			
			probabilityw1 = distrib1.getValue(valueArr);
			probabilityw2 = distrib2.getValue(valueArr);
			difference = probabilityw1 - probabilityw2;
			differenceSquared = Math.pow(difference, 2);
			sumOfSquares += differenceSquared;
		}
		
		double mean = sumOfSquares / (double) numValues;
		rmse = Math.sqrt(mean);
		
		return rmse;
	}

	protected static byte[] toBytes(int n) {
		byte[] intByte = new byte[4];
		
		for(int i = intByte.length-1; i >= 0; i--)
		{
			intByte[i] = (byte) n;
			n >>= 8;
		}
		
		return intByte;
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
