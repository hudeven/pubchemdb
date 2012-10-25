package edu.scripps.fl.test.pubchem.test;

import edu.scripps.fl.pubchem.web.pug.PUGSoapFactory;
import gov.nih.nlm.ncbi.pubchem.PUGStub.FormatType;

public class IdentitySearchTester {

	public static void main(String[] args) throws Exception {
		int[] cids = PUGSoapFactory.getInstance().identitySearch("CCCCC",FormatType.eFormat_SMILES, 1000, 1000);
		System.out.println(String.format("Found %s CIDs", cids.length));
		for(int cid: cids)
			System.out.println(cid);
	}	
}