package edu.scripps.fl.pubchem.test;

import edu.scripps.fl.pubchem.PubChemFactory;

public class VFSFilenameTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int id = 1;
		String name = PubChemFactory.getInstance().getAIDArchive(id);
		System.out.println(id + "\t" + name);
	}

}
