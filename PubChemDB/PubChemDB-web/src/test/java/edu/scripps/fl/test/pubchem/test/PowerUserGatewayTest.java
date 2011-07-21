package edu.scripps.fl.test.pubchem.test;

import java.net.URL;
import java.util.Arrays;

import org.apache.log4j.xml.DOMConfigurator;
import org.dom4j.Document;

import edu.scripps.fl.pubchem.web.pug.PUGRequest;
import edu.scripps.fl.pubchem.web.pug.PowerUserGateway;
import edu.scripps.fl.pubchem.web.pug.PowerUserGateway.Output;
import edu.scripps.fl.pubchem.web.pug.PowerUserGateway.Type;

public class PowerUserGatewayTest {

	public boolean testDescriptionXML() throws Exception {
		PowerUserGateway pug = PowerUserGateway.newInstance();
		pug.setRequest( PUGRequest.newDescriptionXmlRequest(2551));
		pug.submitAndWait(5000);
		String xml = pug.getResponse().asXML();
		System.out.println(xml);
		return true;
	}
	
	public boolean testMultiAssayDownload() throws Exception {
		PowerUserGateway pug = PowerUserGateway.newInstance();
		Document doc = PUGRequest.newMultiAssayResultRequest(
				Arrays.asList(new Integer[]{2546,2551}),
				Arrays.asList(new Integer[]{24892677,24892644}),
				Type.CID, Output.XML);
		pug.setRequest( doc );
		pug.submitAndWait(5000);
		URL url = pug.getResponseURL();
		System.out.println(url);
		return true;
	}
	
	public boolean testBioAssaySummaryDownload() throws Exception {
		PowerUserGateway pug = PowerUserGateway.newInstance();
		Document doc = PUGRequest.newBioActivitySummaryRequest(
				Arrays.asList(new Integer[]{24892677,24892644}),
				Type.CID);
		pug.setRequest( doc );
		pug.submitAndWait(5000);
		URL url = pug.getResponseURL();
		System.out.println(url);
		return true;
	}
	
	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure(PowerUserGatewayTest.class.getResource("/log4j.config.xml"));
		PowerUserGatewayTest test = new PowerUserGatewayTest();
		test.testDescriptionXML();
//		test.testMultiAssayDownload();
//		test.testBioAssaySummaryDownload();
	}
}
