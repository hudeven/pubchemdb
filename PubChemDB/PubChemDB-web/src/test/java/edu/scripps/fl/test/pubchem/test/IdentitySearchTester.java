package edu.scripps.fl.test.pubchem.test;

import java.util.Arrays;

import edu.scripps.fl.pubchem.web.pug.PUGSoapFactory;



public class IdentitySearchTester {

	public static void main(String[] args) throws Exception {
		int[] cids = PUGSoapFactory.getInstance().identitySearch("CCCCC", 1000, 1000);
		for(int cid: cids)
			System.out.println(cid);
		System.out.println("here");
	}

	

	
}
