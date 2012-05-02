package edu.scripps.fl.test.pubchem.test;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import edu.scripps.fl.pubchem.web.ELinkResult;
import edu.scripps.fl.pubchem.web.entrez.ELinkWebSession;
import edu.scripps.fl.pubchem.web.entrez.EUtilsWebSession;
import edu.scripps.fl.pubchem.web.entrez.EntrezHistoryKey;

public class ELinkTest {

	public void test2() throws Exception {
		List<Long> list = Arrays.asList(new Long[] { 179L, 15L, 13L, 9L, 7L, 5L, 3L, 1L, 455051L, 455050L, 455049L, 455048L, 455047L, 455046L, 455045L,
				455044L, 455043L, 455042L, 455041L, 455040L, 455039L, 455038L, 455037L, 455036L, 455035L, 455034L, 455033L, 455032L, 455031L, 455030L, 833L,
				2291L, 434982L, 811L, 1823L, 1792L, 1044L, 256L, 248L, 177L, 175L, 173L, 171L, 169L, 167L, 165L, 163L, 161L, 159L, 157L, 155L, 153L, 151L,
				149L, 145L, 143L, 141L, 139L, 137L, 133L, 131L, 129L, 125L, 123L, 121L, 119L, 115L, 113L, 109L, 107L, 105L, 103L, 101L, 99L, 147L, 97L, 95L,
				93L, 91L, 89L, 87L, 85L, 83L, 81L, 79L, 77L, 73L, 71L, 67L, 65L, 59L, 55L, 53L, 49L, 47L, 45L, 43L, 41L, 39L, 37L, 35L, 33L, 31L, 29L, 25L,
				23L, 21L, 19L });
		String linkNeighbor = "pcassay_pcassay_neighbor_list";
		ELinkWebSession session = ELinkWebSession.newInstance("pcassay", "pcassay", Arrays.asList(new String[] { linkNeighbor }), list,
				"summary[activityoutcomemethod]");
		session.run();
		// should be 5 summaries found for this set of AIDs
		System.out.println("Number of summaries found " + session.getAllIds(linkNeighbor).size());
		printResults(session);
	}

	private void printResults(ELinkWebSession session) {
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

	public void testInLoop() throws Exception {
		ELinkWebSession session = new ELinkWebSession();
		session.setDbFrom("pccompound");
		session.setDb("pcassay");
		session.setLinkName("pccompound_pcassay,pccompound_pcassay_active");
		session.setIds(Arrays.asList(new Long[] { 2551L, 2046L }));
		printResults(session);
	}

	public void testGivenCombo() throws Exception {
		ELinkWebSession session = new ELinkWebSession();
		session.setDbFrom("pcassay");
		session.setDb("protein");
		session.setLinkName("pcassay_protein_target");
		session.setIds(Arrays.asList(new Long[] { 2551L, 2046L }));
		for (ELinkResult relation : session.getELinkResults()) {
			System.out.println(String.format("%s", relation.getIds("protein", "pcassay_protein_target")));
		}
	}

	public void testAsMap() throws Exception {
		ELinkWebSession session = new ELinkWebSession();
		session.setDbFrom("pcassay");
		session.setDb("protein");
		session.setLinkName("pcassay_protein_target");
		session.setIds(Arrays.asList(new Long[] { 2551L, 2046L }));
		System.out.println(session.getELinkResultsAsMap());
	}

	public void testPost() throws Exception {
		List<Long> list = new ArrayList(80000);
		for (Iterator<String> iter = IOUtils.lineIterator(new FileReader("c:/home/temp/aids-20.txt")); iter.hasNext();)
			list.add(Long.parseLong(iter.next()));
		System.out.println("#Aids in file: " + list.size());
		EntrezHistoryKey key = EUtilsWebSession.getInstance().postIds("pcassay", list);
		
		EUtilsWebSession.getInstance().getSummaries(key);
		
		String linkNeighbor = "pcassay_pcassay_neighbor_list";
		ELinkWebSession session = ELinkWebSession.newInstance("pcassay", Arrays.asList(new String[] { linkNeighbor }), key, "summary[activityoutcomemethod]");
		session.run();
		// should be 5 summaries found for this set of AIDs
		System.out.println("Number of summaries found " + session.getAllIds(linkNeighbor).size());
		printResults(session);
	}
	
	public void testLarge() throws Exception {
		List<Long> list = new ArrayList(80000);
		for (Iterator<String> iter = IOUtils.lineIterator(new FileReader("c:/home/temp/aids.txt")); iter.hasNext();)
			list.add(Long.parseLong(iter.next()));
		System.out.println("#Aids in file: " + list.size());
		ELinkWebSession session = ELinkWebSession.newInstance("pcassay", "pcassay", Arrays.asList(new String[] { "pcassay_pcassay_neighbor_list" }), list,
				"summary[activityoutcomemethod]");
		session.run();
		printResults(session);
	}
	public static void main(String[] args) throws Exception {
		DOMConfigurator.configure(ELinkTest.class.getClassLoader().getResource("log4j.config.xml"));
//		BasicConfigurator.configure();
		ELinkTest test = new ELinkTest();
		test.testPost();
	}
}