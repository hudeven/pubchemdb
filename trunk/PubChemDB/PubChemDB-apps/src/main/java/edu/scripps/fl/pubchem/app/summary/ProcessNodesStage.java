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
package edu.scripps.fl.pubchem.app.summary;

import java.util.concurrent.atomic.AtomicInteger;

import org.dom4j.Node;
import org.hibernate.SessionFactory;

import edu.scripps.fl.pipeline.CommitStage;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.PubChemFactory;
import edu.scripps.fl.pubchem.db.PCAssay;

public class ProcessNodesStage extends CommitStage {

	private AtomicInteger ai = new AtomicInteger();

	@Override
	public SessionFactory getSessionFactory() {
		return PubChemDB.getSessionFactory();
	}

	@Override
	public void doSave(Object obj) throws Exception {
		Node node = (Node) obj;
		PCAssay assay = PubChemFactory.getInstance().populateAssayFromSummaryDocument(getSession(), node);
		if (ai.incrementAndGet() % 1000 == 0)
			System.gc();
	}
}