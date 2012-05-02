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
package edu.scripps.fl.pubchem.app.assayxml;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.stage.BaseStage;
import org.dom4j.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pubchem.PubChemXMLParserFactory;
import edu.scripps.fl.pubchem.db.PCAssay;
import edu.scripps.fl.pubchem.web.entrez.EUtilsWebSession;
import edu.scripps.fl.util.ProgressWriter;

public class ProcessDocumentStage extends BaseStage {

	private static final Logger log = LoggerFactory.getLogger(ProcessDocumentStage.class);

	private Set<Long> onHoldAidSet;
	private Set<Long> rnaiAidSet;
	private Set<Long> smallMoleculeAidSet;
	ProgressWriter pw = new ProgressWriter("ProcessDocument");

	@Override
	public void preprocess() throws StageException {
		super.preprocess();
		try {
//			onHoldAidSet = (Set<Long>) EUtilsWebSession.getInstance().getIds("\"hasonhold\"[filter]", "pcassay", new HashSet<Long>());
			rnaiAidSet = (Set<Long>) EUtilsWebSession.getInstance().getIds("\"rnai\"[filter]", "pcassay", new HashSet<Long>());
			smallMoleculeAidSet = (Set<Long>) EUtilsWebSession.getInstance().getIds("\"small_molecule\"[filter]", "pcassay", new HashSet<Long>());
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}

	@Override
	public void process(Object obj) throws StageException {
		pw.increment();
		try {
			Document document = (Document) obj;
			List<PCAssay> assays = PubChemXMLParserFactory.getInstance().populateAssaysFromXMLDocument(document, false);
			PCAssay assay = assays.get(0);
			
			long aid = assay.getAID().longValue();
			if (rnaiAidSet.contains(aid))
				assay.setAssayType("RNAi");
			else if (smallMoleculeAidSet.contains(aid))
				assay.setAssayType("Small Molecule");
			else
				assay.setAssayType("");
	
//			if (onHoldAidSet.contains(aid)) {
//				assay.setOnHold(true);
//			} else {
//				assay.setOnHold(false);
//			}
			
			emit(assay);
		}
		catch(Exception ex) {
			throw new StageException(this, ex);
		}
	}
}