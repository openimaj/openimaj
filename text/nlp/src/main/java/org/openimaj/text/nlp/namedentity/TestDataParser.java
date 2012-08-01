package org.openimaj.text.nlp.namedentity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TestDataParser {
	private DocumentBuilderFactory docBuilderFactory;
	private DocumentBuilder docBuilder;
	
	public TestDataParser(){
		
	}
	
	public static void main(String[] args){
		TestDataParser tdp = new TestDataParser();
		tdp.getTestCases("/home/laurence/TrendMiner/EntityTestData/business-news/marked");
	}
	
	
	
	public ArrayList<Test> getTestCases(String testDirectory){
		File f = new File(testDirectory);
		ArrayList<Test> tests = new ArrayList<TestDataParser.Test>();
		if(f.isDirectory()){
			//Initialize XML parsing objects
			docBuilderFactory = DocumentBuilderFactory.newInstance();
	        docBuilder = null;
			try {
				docBuilder = docBuilderFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	       
			for(File s:f.listFiles()){
				String name = s.getName();
				if(name.substring(name.lastIndexOf(".")+1).equals("xml")){
					tests.add(getTestFrom(s));
				}
			}
		}
		return tests;
	}
	
	private Test getTestFrom(File s) {
		 Document doc = null;
		try {
			doc = docBuilder.parse(s);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doc.getDocumentElement ().normalize ();
		String raw = doc.getElementsByTagName("TextWithNodes").item(0).getTextContent();
		ArrayList<int[]> entities = new ArrayList<int[]>();
		NodeList anoSets = doc.getElementsByTagName("AnnotationSet");
		for (int i = 0; i < anoSets.getLength(); i++) {
			Node n = anoSets.item(i);
			NamedNodeMap m = n.getAttributes();
			if(m.getNamedItem("Name")!=null && m.getNamedItem("Name").getNodeValue().equals("Key")){
				NodeList anoChildren = n.getChildNodes();
				for (int j = 0; j < anoChildren.getLength(); j++) {
					Node child = anoChildren.item(j);
					if(child.hasAttributes() && child.getAttributes().getNamedItem("Type") != null && child.getAttributes().getNamedItem("Type").getNodeValue().equals("Organization")){
						int startchar = Integer.parseInt(child.getAttributes().getNamedItem("StartNode").getNodeValue());
						int endchar = Integer.parseInt(child.getAttributes().getNamedItem("EndNode").getNodeValue());
						entities.add(new int[]{startchar,endchar});
					}
				}
			}
		}
		Test result = new Test();
		result.rawInput=raw;
		result.annotations=entities;
		return result;
	}

	public class Test{
		String rawInput;
		ArrayList<int[]> annotations;
	}

}
