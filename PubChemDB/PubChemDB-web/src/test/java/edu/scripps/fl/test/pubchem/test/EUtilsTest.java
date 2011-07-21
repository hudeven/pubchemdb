package edu.scripps.fl.test.pubchem.test;

import edu.scripps.fl.pubchem.web.entrez.EUtilsSoapFactory;
import edu.scripps.fl.pubchem.web.entrez.EntrezHistoryKey;
import edu.scripps.fl.pubchem.web.pug.PUGSoapFactory;

public class EUtilsTest {

	public static void main(String[] args) throws Exception {
		EntrezHistoryKey key = EUtilsSoapFactory.getInstance().ePost("pccompound", "2519");
		PUGSoapFactory.getInstance().getAssayResults(2551, key);
	}

}
