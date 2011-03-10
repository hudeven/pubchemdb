package edu.scripps.fl.pubchem.test;

import edu.scripps.fl.pubchem.EUtilsFactory;
import edu.scripps.fl.pubchem.EntrezHistoryKey;
import edu.scripps.fl.pubchem.PUGSoapFactory;

public class EUtilsTest {

	public static void main(String[] args) throws Exception {
		EntrezHistoryKey key = EUtilsFactory.getInstance().ePost("pccompound", "2519");
		PUGSoapFactory.getInstance().getAssayResults(2551, key);
	}

}
