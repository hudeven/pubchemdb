package edu.scripps.fl.test.pubchem.test;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.BasicConfigurator;

import java.util.Arrays;
import edu.scripps.fl.pubchem.web.ELinkResult;
import edu.scripps.fl.pubchem.web.entrez.ELinkWebSession;

public class ELinkTest {

	
	public void testInLoop() throws Exception {
		ELinkWebSession session = new ELinkWebSession();
		session.setDbFrom("pccompound");
		session.setDb("pcassay");
		session.setLinkName("pccompound_pcassay,pccompound_pcassay_active");
		session.setIds( Arrays.asList(new Long[]{2551L,2046L}) );
		Collection<ELinkResult> relations = session.getELinkResults();
		for(ELinkResult relation: relations) {
			for(String db: relation.getDatabases()) {
				for(String link: relation.getLinks(db)) {
					List<Long> ids = relation.getIds(db, link);
					System.out.println(String.format("%s\t%s\t%s\t%s\t%s",relation.getId(), relation.getDbFrom(), db, link, ids));
				}
			}
		}
	}
	
	public void testGivenCombo() throws Exception {
		ELinkWebSession session = new ELinkWebSession();
		session.setDbFrom("pcassay");
		session.setDb("protein");
		session.setLinkName("pcassay_protein_target");
		session.setIds( Arrays.asList(new Long[]{2551L,2046L}) );
		for(ELinkResult relation: session.getELinkResults()) {
			System.out.println(String.format("%s", relation.getIds("protein", "pcassay_protein_target")));
		}
	}
	
	public void testAsMap() throws Exception {
		ELinkWebSession session = new ELinkWebSession();
		session.setDbFrom("pcassay");
		session.setDb("protein");
		session.setLinkName("pcassay_protein_target");
		session.setIds( Arrays.asList(new Long[]{2551L,2046L}) );
		System.out.println(session.getELinkResultsAsMap());
	}
	
	public static void main(String[] args) throws Exception {
//		BasicConfigurator.configure();
		ELinkTest test = new ELinkTest();
		test.testAsMap();		
	}
}