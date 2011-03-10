package edu.scripps.fl.pubchem.test;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.log4j.xml.DOMConfigurator;
import org.hibernate.Session;

import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.PubChemFactory;
import edu.scripps.fl.pubchem.PubChemXMLParserFactory;
import edu.scripps.fl.pubchem.db.PCAssay;

public class PubChemDBSaveAssayTest {

	public static void main(String[] args) throws Exception {
		URL url = PubChemDBSaveAssayTest.class.getClassLoader().getResource("edu/scripps/fl/pubchem/log4j.config.xml");
		DOMConfigurator.configure(url);
		
		url = PubChemDBSaveAssayTest.class.getClassLoader().getResource("edu/scripps/fl/pubchem/test/hibernate.cfg.xml");
		PubChemDB.setUp(url);
		
		PubChemXMLParserFactory pc = PubChemXMLParserFactory.getInstance();
		InputStream is = PubChemFactory.getInstance().getXmlDescr(Long.parseLong(args[0]));
		List<PCAssay> assays = pc.populateAssayFromXML(is, false);
		PCAssay assay = assays.get(0);
		assay.getCategorizedComments().put("hello", "world");
		
		Session session = PubChemDB.getSession();
		PubChemDB.saveXRefs(session, assay);
		PubChemDB.saveAssay(session, assay);
		session.getTransaction().commit();
	}
	
}