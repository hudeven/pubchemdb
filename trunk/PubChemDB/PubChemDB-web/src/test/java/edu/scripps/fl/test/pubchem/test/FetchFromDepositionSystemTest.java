/*
 * Copyright 2010 The Scripps Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.scripps.fl.test.pubchem.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pubchem.web.PCOutcomeCounts;
import edu.scripps.fl.pubchem.web.session.PCDepositionSystemSession;

public class FetchFromDepositionSystemTest {

	private static final Logger log = LoggerFactory.getLogger(FetchFromDepositionSystemTest.class);
	
	public static void main(String[] args) throws Exception {
//		BasicConfigurator.configure();
//		org.apache.log4j.Logger.getRootLogger().setLevel(Level.INFO);
		DOMConfigurator.configure(FetchFromDepositionSystemTest.class.getResource("/log4j.config.xml"));
		
		PCDepositionSystemSession session = new PCDepositionSystemSession();
		session.DEBUGGING = true;
		session.login(args[0], args[1]);
		for(int ii = 2; ii < args.length; ii++) {
			int aid = Integer.parseInt(args[ii]);
			log.info("Fetching outcome counts.");
			PCOutcomeCounts counts = session.getSubstanceOutcomeCounts(aid);
			log.info(String.format("Substance counts for AID %s: All: %s, Probe: %s, Active: %s, Inactive: %s, Inconclusive: %s", aid, counts.all, counts.probe, counts.active, counts.inactive, counts.inconclusive));

			log.info("Fetching full XML.");
			displayFile(aid, session.getAssayXML(aid), "xml");

			log.info("Fetching description XML.");
			displayFile(aid, session.getDescrXML(aid), "xml");

			log.info("Fetching CSV.");
			displayFile(aid, session.getAssayCSV(aid), "csv");

			log.info("Fetching SDF.");
			displayFile(aid, session.getAssaySDF(aid), "sdf");

			log.info("SIDS for AID " + aid + ": " + session.getAssaySIDs(aid));
		}
	}
	
	private static void displayFile(int aid, InputStream is, String ext) throws IOException {
		File file = File.createTempFile("AID" + aid + "-", "." + ext);
		file.deleteOnExit();
		IOUtils.copy(is, new FileOutputStream(file));
		is.close();
		log.info("Created file " + file);
//		Desktop.getDesktop().open(file);
	}
}