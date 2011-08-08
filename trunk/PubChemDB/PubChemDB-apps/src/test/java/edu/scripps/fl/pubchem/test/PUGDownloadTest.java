package edu.scripps.fl.pubchem.test;

import java.net.URL;

import org.apache.log4j.xml.DOMConfigurator;

import java.util.Arrays;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.app.PUGAssayResultDownloader;
import edu.scripps.fl.pubchem.web.pug.PUGSoapFactory;

public class PUGDownloadTest {

	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure(PUGAssayResultDownloader.class.getClassLoader().getResource("log4j.config.xml"));
		URL url = PUGAssayResultDownloader.class.getClassLoader().getResource("hibernate.cfg.xml");
		PubChemDB.setUp(url);
		
		int targetAID = 2551;
		int diffAID = 2546;
		String searchTerm = String.format("%s[ActiveAid] NOT %s[ActiveAid]", targetAID, diffAID);
		
		long start = System.currentTimeMillis();
		
		String listKey = PUGSoapFactory.getInstance().getListKey("pccompound", searchTerm);
		PUGAssayResultDownloader resultDownloader = new PUGAssayResultDownloader();
		resultDownloader.process(Arrays.asList(new Integer[]{targetAID, diffAID}), listKey);

		long end = System.currentTimeMillis();
		
		System.out.println(String.format("Duration: %s seconds", (end-start)/1000));
	}

}
