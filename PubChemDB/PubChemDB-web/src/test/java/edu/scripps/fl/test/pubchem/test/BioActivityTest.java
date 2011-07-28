package edu.scripps.fl.test.pubchem.test;

import java.util.Arrays;
import java.util.Map;

import edu.scripps.fl.pubchem.web.session.PCWebSession;

public class BioActivityTest {

	public static void main(String[] args) throws Exception {
		PCWebSession session = new PCWebSession();
//		session.getBioActivityAssaySummary(Arrays.asList(new Long[]{2244L,596L}));
		Map map = session.getBioActivityCompoundSummaryAsMap(Arrays.asList(new Long[]{2244L,596L,2519L}));
		System.out.println(map);
	}

}
