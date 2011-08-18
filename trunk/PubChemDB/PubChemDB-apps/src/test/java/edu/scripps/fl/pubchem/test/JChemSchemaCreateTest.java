package edu.scripps.fl.pubchem.test;

import java.io.File;
import java.net.URL;

import org.apache.log4j.xml.DOMConfigurator;

import edu.scripps.fl.pubchem.PubChemDBwJChem;


public class JChemSchemaCreateTest {
	
	public static void main(String[] args) throws Exception {
		URL url = JChemSchemaCreateTest.class.getClassLoader().getResource("log4j.config.xml");
		DOMConfigurator.configure(url);
		url = new File(args[0]).toURI().toURL();
		PubChemDBwJChem.setUp(url);
	}
}
