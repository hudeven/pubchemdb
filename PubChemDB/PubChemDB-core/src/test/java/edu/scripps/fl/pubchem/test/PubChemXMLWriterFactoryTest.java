package edu.scripps.fl.pubchem.test;

import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.PubChemFactory;
import edu.scripps.fl.pubchem.PubChemXMLParserFactory;
import edu.scripps.fl.pubchem.PubChemXMLWriterFactory;
import edu.scripps.fl.pubchem.db.PCAssay;

public class PubChemXMLWriterFactoryTest {
	
	private static final Logger log = LoggerFactory.getLogger(PubChemXMLWriterFactoryTest.class);
	
	public static void main(String[] args) throws Exception {
		URL url = PubChemXMLWriterFactoryTest.class.getClassLoader().getResource("edu/scripps/fl/pubchem/log4j.config.xml");
		DOMConfigurator.configure(url);
		
		PubChemXMLParserFactory pc = PubChemXMLParserFactory.getInstance();
		InputStream is = PubChemFactory.getInstance().getXmlDescr(Long.parseLong(args[0]));
		List<PCAssay> assays = pc.populateAssayFromXML(is, false);
		PCAssay assay = assays.get(0);
		
		PubChemXMLWriterFactory w = new PubChemXMLWriterFactory();
		File file = File.createTempFile("PubChemXMLWriter", ".xml");
		log.info("Writing xml file " + file);
		w.write(assay, file);
		Desktop.getDesktop().open(file);
	}

}
