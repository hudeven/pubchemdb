package edu.scripps.fl.pubchem.test;

import java.io.FileInputStream;
import java.net.URL;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;

import edu.scripps.fl.pubchem.PubChemXMLParserFactory;
import edu.scripps.fl.pubchem.db.PCAssay;

public class PubChemXMLParserFactoryTest {
	
	public static void main(String[] args) throws Exception {
		URL url = PubChemXMLParserFactoryTest.class.getClassLoader().getResource("log4j.config.xml");
		DOMConfigurator.configure(url);
		PubChemXMLParserFactory pc = PubChemXMLParserFactory.getInstance();
//		InputStream is = PubChemFactory.getInstance().getXmlDescr(Long.parseLong(args[0]));
		FileInputStream is = new FileInputStream("c:\\home\\temp\\1544.descr.xml.in");
		List<PCAssay> assays = pc.populateAssayFromXML(is, true);
		System.out.println(assays.size());
	}
}
