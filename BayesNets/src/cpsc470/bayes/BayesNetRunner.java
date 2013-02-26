package cpsc470.bayes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import aima.core.probability.CategoricalDistribution;
import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesInference;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.approx.BayesInferenceApproxAdapter;
import aima.core.probability.bayes.approx.GibbsAsk;
import aima.core.probability.bayes.approx.LikelihoodWeighting;
import aima.core.probability.bayes.approx.RejectionSampling;
import aima.core.probability.bayes.exact.EliminationAsk;
import aima.core.probability.bayes.exact.EnumerationAsk;
import aima.core.probability.bayes.impl.BayesNet;
import aima.core.probability.bayes.util.XMLBIFSAXParser;
import aima.core.probability.domain.Domain;
import aima.core.probability.proposition.AssignmentProposition;
import aima.test.core.unit.probability.bayes.util.XMLBIFSAXParserTest;

public class BayesNetRunner {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// TODO PROBLEM 2: FILL THIS IN
		
		
		if (args.length < 1) {
			throw new IllegalArgumentException("Missing environment file path.");
		}
		String filePath = args[0];
		BayesNet bayesNet = importNetwork(filePath);
		
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		List<CategoricalDistribution> distributionList = new LinkedList<CategoricalDistribution>();
		boolean loop = true;
		//XMLBIFSAXParserTest test = new XMLBIFSAXParserTest();
		//test.testLoadAlarmNetwork();
		while (loop){
			
			System.out.println("1. Run Inference" );
			System.out.println("2. Check RMSE" );
			System.out.println("3. Quit" );
			System.out.println("Input selection of choice you want to run" );
					
			String input = null;
			int choice = 0;
			try {
				input = bufferRead.readLine();
				choice = Integer.parseInt(input);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
			if(choice == 1){
				distributionList.add(runInference(bayesNet) );
			}else if(choice== 2){
				runRMSE(distributionList);
					
				
			}else if(choice== 3){
				loop = false;
			}
			
		}

		
		
		
	}
	
	private static void runRMSE(List<CategoricalDistribution> distributionList){
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("");
		for(int i=0; i<distributionList.size(); i++){
			CategoricalDistribution temp = distributionList.get(i);
			System.out.println(i + " " +  temp.toString()   );			
		}
		System.out.println(distributionList.size() + " Quit");
		
		
		System.out.println("Separated by commas, input indexes of distributions you want to run RMSE on");
		String delims = "[,]";
		String input = null;
		
		try {
			input = bufferRead.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] tokens = input.split(delims);
		int distribLocation1 = Integer.parseInt(tokens[0]);
		if(distribLocation1 != distributionList.size() && tokens.length >= 1 ){
			
			int distribLocation2 = Integer.parseInt(tokens[1]);
			
			double rmse = Utils.computeDistributionRMSE(distributionList.get(distribLocation1),
															distributionList.get(distribLocation2) );

			System.out.println("RMSE: " + rmse);
			System.out.println();
		
		}
		
	}
	private static CategoricalDistribution runInference(BayesNet bayesNet){
		List<RandomVariable> vars = new ArrayList<RandomVariable>();
		
		vars = bayesNet.getVariablesInTopologicalOrder();
		List<RandomVariable> queryVars =  getQueryVars( vars );
		List<AssignmentProposition> evidenceList = getEvidence(vars); 
		
		RandomVariable[] queryVariables = convertQuerry(queryVars);
		AssignmentProposition[] observedEvidence = convertEvidence(evidenceList);
		
		BayesInference bayesInference = getAlgoType();
		
		long startTime = System.currentTimeMillis();
		CategoricalDistribution targetDistrib = bayesInference.ask(queryVariables, observedEvidence, bayesNet);
		long runTimeMillis = System.currentTimeMillis() - startTime;
		System.out.println("Target distribution: " + targetDistrib);
		System.out.println("Exact run time using " + bayesInference.getClass().getSimpleName() + ": " + runTimeMillis + "s");
		System.out.println();
		
		return targetDistrib;
	}
	private static RandomVariable[]  convertQuerry(List<RandomVariable> queryVars) {
		RandomVariable[] queryVariables = new RandomVariable[queryVars.size()];
		for (int i =0; i< queryVars.size(); i++){
			queryVariables[i] = queryVars.get(i);
		}
		return queryVariables;
	}
	private static AssignmentProposition[]  convertEvidence(List<AssignmentProposition> evidence) {
		AssignmentProposition[] evidenceList = new AssignmentProposition[evidence.size()];
		for (int i =0; i< evidence.size(); i++){
			evidenceList[i] = evidence.get(i);
		}
		return evidenceList;
	}
	private static BayesInference getAlgoType(){
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		BayesInference algorithm = null;
		System.out.println("1. Enumeration" );
		System.out.println("2. Variable elimination" );
		System.out.println("3. Rejection sampling" );
		System.out.println("4. Likelihood weighing" );
		System.out.println("5. Gibbs sampling" );
		
		System.out.println("" );
		System.out.println("Input selection of alogrithm you want to run" );
				
		int sampleCount = 0;
		String input = null;
		int choice = 0;
		try {
			input = bufferRead.readLine();
			choice = Integer.parseInt(input);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		if (choice >=3){
			System.out.println("Input number of samples to approximate" );
			try {
				input = bufferRead.readLine();
				sampleCount = Integer.parseInt(input);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		if(choice == 1){
			algorithm = new EnumerationAsk();
		}
		else if(choice == 2){
			algorithm = new EliminationAsk();
		}
		else if(choice == 3){
			algorithm = new BayesInferenceApproxAdapter(new RejectionSampling(), sampleCount);
		}
		else if(choice == 4){
			algorithm = new BayesInferenceApproxAdapter(new LikelihoodWeighting(), sampleCount);
		}
		else if(choice == 5){
			algorithm = new BayesInferenceApproxAdapter(new GibbsAsk(), sampleCount);
		}
		
		return algorithm;
	}
	private static void printList(List<RandomVariable> varList){
		for (int i=0; i<varList.size(); i++){
			RandomVariable temp = varList.get(i);
			System.out.println(i + " " + temp.toString() );
		}
	}
	
	private static void printEvidenceList(List<RandomVariable> varList){
		for (int i=0; i<varList.size(); i++){
			RandomVariable temp = varList.get(i);
			Domain type = temp.getDomain();
			System.out.println(i + " " + temp.toString() + ", " + type.toString() );
		}
	}
	
	private static List<AssignmentProposition> getEvidence(List<RandomVariable> varList){
		List<AssignmentProposition> evidenceList = new ArrayList<AssignmentProposition>();
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		printEvidenceList(varList);
		System.out.println("Separated by commas, input indexes and truth values for all the evidence variables");
		
		String delims = "[,]";
		String input = null;
		
		try {
			input = bufferRead.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] tokens = input.split(delims);

		
		for(int i=0; i<tokens.length; i+=2){
			int temp =  Integer.parseInt(tokens[i]);
			String testValue = tokens[i+1];
			
			AssignmentProposition assignment = new 	AssignmentProposition(varList.get(temp), testValue);
			evidenceList.add(assignment);
		}
		//
		//for(int i=0; i<tokens.length; i++){
		//	int temp =  Integer.parseInt(tokens[i]);
		//	varList.remove(temp);
		//}
		return evidenceList;
	}
	
	
	private static List<RandomVariable> getQueryVars(List<RandomVariable> varList ){
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		List<RandomVariable> queryVars = new ArrayList<RandomVariable>();
		
		printList(varList);
		System.out.println("Separated by commas, input indexes for all the query variables");
			
		String delims = "[,]";
		String input = null;
		
		try {
			input = bufferRead.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String[] tokens = input.split(delims);

		//add query variables into the query list
		for(int i=0; i<tokens.length; i++){
			int temp =  Integer.parseInt(tokens[i]);
			queryVars.add(varList.get(temp) );
		}
		//remove query variables from main list so they cannot be used as evidence variables
		//for(int i=0; i<tokens.length; i++){
		//	int temp =  Integer.parseInt(tokens[i]);
		//	varList.
		//}
		return queryVars;
	}
	
	
	private static BayesNet importNetwork(String path){
		BayesNet bayesNet = null; 
		try {
			bayesNet =  (BayesNet) XMLBIFSAXParser.loadBNFromXMLBIF(path);

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
			throw new IllegalArgumentException("Missing environment file path.");			
		}
		
		return bayesNet;
	}

}
