package aima.test.core.unit.probability.bayes.util;


import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import cpsc470.bayes.Utils;

import aima.core.probability.CategoricalDistribution;
import aima.core.probability.ProbabilityModel;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesInference;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.approx.BayesInferenceApproxAdapter;
import aima.core.probability.bayes.approx.BayesSampleInference;
import aima.core.probability.bayes.approx.GibbsAsk;
import aima.core.probability.bayes.approx.LikelihoodWeighting;
import aima.core.probability.bayes.approx.RejectionSampling;
import aima.core.probability.bayes.exact.EliminationAsk;
import aima.core.probability.bayes.exact.EnumerationAsk;
import aima.core.probability.bayes.util.XMLBIFSAXParser;
import aima.core.probability.example.ExampleRV;
import aima.core.probability.proposition.AssignmentProposition;

/**
 * 
 * @author pedrito maynard-zhang
 */
public class XMLBIFSAXParserTest {

	@Test
	public void testLoadBurglaryAlarmNetwork() {
		String bnXMLBIFPath = "inputs\\alarm-burglary-earthquake.xml";
		BayesianNetwork bn;
		try {
			bn = XMLBIFSAXParser.loadBNFromXMLBIF(bnXMLBIFPath);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		BayesInference bayesInference =
			//new EnumerationAsk();
			new EliminationAsk();

		// AIMA3e. pg. 514
		// P(Alarm | Burglary = false, Earthquake = false, JohnCalls = true, MaryCalls = true) = <0.558, 0.442>
		CategoricalDistribution d = bayesInference
			.ask(new RandomVariable[] { ExampleRV.ALARM_RV },
				new AssignmentProposition[] {
					new AssignmentProposition(
						ExampleRV.BURGLARY_RV, false),
					new AssignmentProposition(
						ExampleRV.EARTHQUAKE_RV, false),
					new AssignmentProposition(
						ExampleRV.JOHN_CALLS_RV, true),
					new AssignmentProposition(
						ExampleRV.MARY_CALLS_RV, true) }, bn);

		// System.out.println("P(Alarm | ~b, ~e, j, m)=" + d);
		Assert.assertEquals(2, d.getValues().length);
		Assert.assertEquals(0.5577689243027888, d.getValues()[0],
			ProbabilityModel.DEFAULT_ROUNDING_THRESHOLD);
		Assert.assertEquals(0.44223107569721115, d.getValues()[1],
			ProbabilityModel.DEFAULT_ROUNDING_THRESHOLD);

		// AIMA3e pg. 523
		// P(Burglary | JohnCalls = true, MaryCalls = true) = <0.284, 0.716>
		d = bayesInference
			.ask(new RandomVariable[] { ExampleRV.BURGLARY_RV },
				new AssignmentProposition[] {
					new AssignmentProposition(
						ExampleRV.JOHN_CALLS_RV, true),
					new AssignmentProposition(
						ExampleRV.MARY_CALLS_RV, true) }, bn);

		// System.out.println("P(Burglary | j, m)=" + d);
		Assert.assertEquals(2, d.getValues().length);
		Assert.assertEquals(0.2841718353643929, d.getValues()[0],
			ProbabilityModel.DEFAULT_ROUNDING_THRESHOLD);
		Assert.assertEquals(0.7158281646356071, d.getValues()[1],
			ProbabilityModel.DEFAULT_ROUNDING_THRESHOLD);

		// AIMA3e pg. 528
		// P(JohnCalls | Burglary = true) = <0.849, 0.151>
		d = bayesInference.ask(
			new RandomVariable[] { ExampleRV.JOHN_CALLS_RV },
			new AssignmentProposition[] { new AssignmentProposition(
				ExampleRV.BURGLARY_RV, true) }, bn);
		// System.out.println("P(JohnCalls | b)=" + d);
		Assert.assertEquals(2, d.getValues().length);
		Assert.assertEquals(0.8490169999999999, d.getValues()[0],
			ProbabilityModel.DEFAULT_ROUNDING_THRESHOLD);
		Assert.assertEquals(0.15098299999999998, d.getValues()[1],
			ProbabilityModel.DEFAULT_ROUNDING_THRESHOLD);
	}

	@Test
	public void testLoadAsiaNetwork() {
		String bnXMLBIFPath = "inputs\\asia.xml";
		BayesianNetwork bn;
		try {
			bn = XMLBIFSAXParser.loadBNFromXMLBIF(bnXMLBIFPath);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// TODO
	}

	@Test
	public void testLoadAlarmNetwork() {
		String bnXMLBIFPath = "inputs\\alarm.xml";
		BayesianNetwork bn;
		try {
			bn = XMLBIFSAXParser.loadBNFromXMLBIF(bnXMLBIFPath);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// test inference
		RandomVariable[] query =
			new RandomVariable[] {
				Utils.getRV("Intubation", bn)};
		
		AssignmentProposition[] evidence =
			new AssignmentProposition[] {
				new AssignmentProposition(Utils.getRV("BP", bn), "High")};

		BayesInference exactInferenceEngine = new EliminationAsk();
		long startTime = System.currentTimeMillis();
		CategoricalDistribution targetDistrib = exactInferenceEngine.ask(query, evidence, bn);
		long runTimeMillis = System.currentTimeMillis() - startTime;
		System.out.println("Target distribution: " + targetDistrib);
		System.out.println("Exact run time using " + exactInferenceEngine.getClass().getSimpleName() + ": " + runTimeMillis + "s");
		System.out.println();
		
		int N = 100000; //10000; // 1000;
		BayesInference approxInferenceEngine =
				//new EnumerationAsk();
				//new EliminationAsk();
				new BayesInferenceApproxAdapter(
					new RejectionSampling(),
					//new LikelihoodWeighting(),
					//new GibbsAsk(),
					N);
		
		System.out.println("Approximate inference using " + approxInferenceEngine.getClass().getSimpleName() + ":");
		startTime = System.currentTimeMillis();
		CategoricalDistribution actualDistrib = approxInferenceEngine.ask(query, evidence, bn);
		double rmse = Utils.computeDistributionRMSE(targetDistrib, actualDistrib);
		runTimeMillis = System.currentTimeMillis() - startTime;
		
		System.out.println("Computed distribution: " + actualDistrib);
		System.out.println("RMSE: " + rmse);
		System.out.println("Run time: " + runTimeMillis + "ms");
		System.out.println();

		// TODO assertions
	}

	@Test
	public void testLoadBarleyNetwork() {
		String bnXMLBIFPath = "inputs\\barley.xml";
		BayesianNetwork bn;
		try {
			bn = XMLBIFSAXParser.loadBNFromXMLBIF(bnXMLBIFPath);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// TODO
	}

	@Test
	public void testLoadCPCSNetwork() {
		String bnXMLBIFPath = "inputs\\cpcs-179.xml";
		BayesianNetwork bn;
		try {
			bn = XMLBIFSAXParser.loadBNFromXMLBIF(bnXMLBIFPath);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// TODO
	}
}
