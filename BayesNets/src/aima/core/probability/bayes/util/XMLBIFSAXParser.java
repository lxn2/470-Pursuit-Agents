package aima.core.probability.bayes.util;

import java.io.FileReader;
import java.io.Reader;
import java.io.IOException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import cpsc470.bayes.Utils;

import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.impl.BayesNet;
import aima.core.probability.bayes.impl.FullCPTNode;
import aima.core.probability.bayes.Node;
import aima.core.probability.domain.ArbitraryTokenDomain;
import aima.core.probability.domain.BooleanDomain;
import aima.core.probability.domain.Domain;
import aima.core.probability.RandomVariable;
import aima.core.probability.util.RandVar;

/**
 * Imports a Bayesian network from a file in
 * <a href="http://www.cs.cmu.edu/~fgcozman/Research/InterchangeFormat/">XMLBIF format</a>.
 * 
 * TODO (p2) add support for decision and utility nodes
 * TODO (p2) generalize to pre-normalize unnormalized CPDs
 * 
 * @author pedrito maynard-zhang
 */
public class XMLBIFSAXParser extends DefaultHandler {

	public static BayesianNetwork loadBNFromXMLBIF(String filePath)
			throws SAXException, ParserConfigurationException, IOException {
		
		FileReader reader = null;
		try {
			reader = new FileReader(filePath);
			return loadBayesianNetwork(reader);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}

	public static BayesianNetwork loadBayesianNetwork(Reader reader)
			throws SAXException, ParserConfigurationException, IOException {
		
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setNamespaceAware(true);
	    SAXParser saxParser = spf.newSAXParser();
		XMLReader xmlReader = saxParser.getXMLReader();
		XMLBIFSAXParser xmlBIFParser = new XMLBIFSAXParser();
		xmlReader.setContentHandler(xmlBIFParser);
		xmlReader.parse(new InputSource(reader));
		
		return new BayesNet(xmlBIFParser.bnRootNodes.toArray(new Node[]{}));
	}
	
	/**
	 * Determine if random variable's domain is boolean.
	 * NOTE: Only recognizes cases where 'true' outcome declared first since this is how BooleanDomain is defined.
	 *       Recognize cases where 'false' comes first requires transposing cpt values.
	 *       
	 * TODO (p2) generalize to recognize boolean domains where false value is declared first
	 * 
	 * @param outcomes
	 * @return
	 */
	protected static boolean isBooleanDomain(List<String> outcomes) {
		HashSet<String> trueVals = new HashSet<String>(Arrays.asList(new String[]{"true", "t", "1"}));
		HashSet<String> falseVals = new HashSet<String>(Arrays.asList(new String[]{"false", "f", "0"}));
		return outcomes.size() == 2
				&& trueVals.contains(outcomes.get(0).toLowerCase())
				&& falseVals.contains(outcomes.get(1).toLowerCase());
	}
	
	protected StringBuffer curXMLNodeVal;
	protected String curRVName;
	protected List<String> curRVOutcomes;
	protected String curBNNodeName; // use name rather than RV since "DEFINITION" may come before "VARIABLE" for an RV
	protected List<String> curBNNodeParentNames; // ditto
	protected List<Double> curBNNodeCPT;

	protected Map<String, RandomVariable> rvs;
	protected Map<String, List<String>> parentLists;
	protected Map<String, List<Double>> cpts;
	
	protected Map<String, Node> bnNodes;
	protected List<Node> bnRootNodes;

	public void startDocument() throws SAXException {
		curXMLNodeVal = new StringBuffer();
		
		curRVName = null;
		curRVOutcomes = null;
		curBNNodeName = null;
		curBNNodeParentNames = null;
		curBNNodeCPT = null;

		rvs = new HashMap<String, RandomVariable>();
		parentLists = new HashMap<String, List<String>>();
		cpts = new HashMap<String, List<Double>>();
		
		bnNodes = new HashMap<String, Node>();
		bnRootNodes = new LinkedList<Node>();
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes attributes) throws SAXException {
		curXMLNodeVal.setLength(0);
		switch (qName) {
			case "VARIABLE":
				// Check that random variable type is supported.
				String type = attributes.getValue("TYPE");
				switch (type) {
					case "nature":
						break;
					case "decision":
					case "utility":
						throw new SAXException(
								"Unsupported XMLBIF.",
								new UnsupportedOperationException("'" + type + "' random variable types not currently supported."));
					default:
						throw new SAXException("Invalid XMLBIF: Unknown random variable type '" + type + "'.");
				}
				curRVName = null;
				curRVOutcomes =  new LinkedList<String>();
				break;

			case "DEFINITION":
				curBNNodeName = null;
				curBNNodeParentNames = new LinkedList<String>();
				curBNNodeCPT = new LinkedList<Double>();
				break;

			default:
				// nothing to do
		}
	}
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		curXMLNodeVal.append(ch,start,length); // QUESTION necessary to use StringBuffer?
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		switch (qName) {
			// VARIABLE section
			case "VARIABLE":
				Domain domain;
				if (isBooleanDomain(curRVOutcomes)) {
					domain = new BooleanDomain();
				} else {
					domain = new ArbitraryTokenDomain(curRVOutcomes.toArray());
				}

				try {
					rvs.put(curRVName, new RandVar(curRVName, domain));
				} catch (IllegalArgumentException iax) {
					throw new SAXException("Illegal XMLBIF for random variable '" + curRVName + "'.", iax);
				}
				break;
			
			case "NAME":
				// note: this will incorrectly process the bn name element as well, but it gets ignored, so harmless for now.
				curRVName = Utils.normalizeRVName(curXMLNodeVal.toString());
				break;
			
			case "OUTCOME":
				curRVOutcomes.add(curXMLNodeVal.toString().trim());
				break;

			// DEFINITION section
			case "DEFINITION":
				if (!curBNNodeParentNames.isEmpty()) {
					parentLists.put(curBNNodeName, curBNNodeParentNames);
				}
				cpts.put(curBNNodeName, curBNNodeCPT);
				break;

			case "FOR":
				curBNNodeName = Utils.normalizeRVName(curXMLNodeVal.toString());
				break;
			
			case "GIVEN":
				curBNNodeParentNames.add(Utils.normalizeRVName(curXMLNodeVal.toString()));
				break;
			
			case "TABLE":
				String[] cptStrs = curXMLNodeVal.toString().trim().split(" ");
				for (String probabStr : cptStrs) {
					try {
						curBNNodeCPT.add(Double.parseDouble(probabStr));
					} catch (NumberFormatException nfx) {
						throw new SAXException("Invalid XMLBIF: " + probabStr + " is not a valid probability value.", nfx);
					}
				}
				break;
			
			default:
				// nothing to do
		}
	}
	
	public void endDocument() throws SAXException {
		if (rvs.size() != cpts.size()) {
			throw new SAXException("Invalid XMLBIF: The sets of VARIABLE and DEFINITION sections don't match.");
		}
		for (String rvName : rvs.keySet()) {
			if (bnNodes.containsKey(rvName)) {
				// node already built while building one of its descendants
				continue;
			}
			buildNode(rvName);
		}
	}

	protected Node buildNode(String rvName) throws SAXException {
		RandomVariable rv = rvs.get(rvName);
		
		List<String> parentNames = parentLists.get(rvName);
		boolean isRoot = parentNames == null;

		List<Double> cpt = cpts.get(rvName);
		if (cpt == null) {
			throw new SAXException("Invalid XMLBIF: No TABLE section defined for variable " + rvName + ".");
		}
		double[] cptArray = new double[cpt.size()];
		int i = 0;
		double rowSum = 0;
		int rvDomainSize = rv.getDomain().size();
		for (double probab : cpt) { // unbox cpt values
			cptArray[i] = probab;
			rowSum += probab;
			if ((i + 1) % rv.getDomain().size() == 0) {
				// normalize row
				for (int j = i - rvDomainSize + 1; j < i + 1; ++j) {
					cptArray[j] /= rowSum;
				}
				rowSum = 0;
			}
			++i;
		}

		// Build node.
		Node bnNode;
		try {
			if (isRoot) {
				bnNode = new FullCPTNode(rv, cptArray);
				bnRootNodes.add(bnNode);
			} else {
				List<Node> parents = new LinkedList<Node>();
				for (String parentName : parentNames) {
					Node parent = bnNodes.get(parentName);
					if (parent == null) {
						parent = buildNode(parentName);
					}
					parents.add(parent);
				}
				bnNode = new FullCPTNode(rv, cptArray, parents.toArray(new Node[]{}));
			}
		} catch (IllegalArgumentException iax) {
			throw new SAXException("Invalid XMLBIF for node " + rvName + ".", iax);
		}
		
		bnNodes.put(rvName, bnNode);
		return bnNode;
	}
}