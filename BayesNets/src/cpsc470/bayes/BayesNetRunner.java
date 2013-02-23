package cpsc470.bayes;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.impl.BayesNet;
import aima.core.probability.bayes.util.XMLBIFSAXParser;

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
		XMLBIFSAXParser parser;
		BayesianNetwork bayesNet;
		try {
			bayesNet =  XMLBIFSAXParser.loadBNFromXMLBIF(filePath);

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
		
		
		
	}

}
