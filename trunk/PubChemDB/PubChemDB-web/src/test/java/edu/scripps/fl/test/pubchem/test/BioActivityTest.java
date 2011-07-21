package edu.scripps.fl.test.pubchem.test;

import java.util.Arrays;

import edu.scripps.fl.pubchem.web.session.PCWebSession;

public class BioActivityTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		PCWebSession session = new PCWebSession();
		session.getBioActivities(Arrays.asList(new Long[]{2244L,596L}));

	}

}
