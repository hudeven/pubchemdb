package edu.scripps.fl.test.pubchem.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import edu.scripps.fl.pubchem.web.ELinkResult;
import edu.scripps.fl.pubchem.web.entrez.ELinkWebSession;

public class SIDCIDLink {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		List<Long> list = Arrays.asList(new Long[] {2L,
				136946874L,
				136946865L,
				136946888L,
				136946898L,
				143472293L,
				143472279L,
				136949657L,
				136949665L,
				136949652L,
				143472304L,
				143472326L,
				143472255L,
				136947116L
});
		String linkNeighbor = "pcsubstance_pccompound_same";
		ELinkWebSession session = ELinkWebSession.newInstance("pcsubstance", "pccompound", Arrays.asList(new String[] { linkNeighbor }), list,
				"");
		session.run();
		printResults(session);
		
	}

	private static void printResults(ELinkWebSession session) {
		Collection<ELinkResult> relations = session.getELinkResults();
		for (ELinkResult relation : relations) {
			for (String db : relation.getDatabases()) {
				for (String link : relation.getLinks(db)) {
					List<Long> ids = relation.getIds(db, link);
					System.out.println(String.format("%s\t%s\t%s\t%s\t%s", relation.getId(), relation.getDbFrom(), db, link, ids));
				}
			}
		}
	}
}
