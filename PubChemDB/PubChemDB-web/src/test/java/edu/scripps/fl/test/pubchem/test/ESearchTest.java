package edu.scripps.fl.test.pubchem.test;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.xml.DOMConfigurator;

import edu.scripps.fl.pubchem.web.entrez.EUtilsFactory;

public class ESearchTest {

	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure(ELinkTest.class.getClassLoader().getResource("log4j.config.xml"));
		Collection<Long> ids = EUtilsFactory.getInstance().getIds("chembl[sourcename]", "pcassay", new ArrayList(), 500000);
		System.out.println("Size: " + ids.size());
	}
}
