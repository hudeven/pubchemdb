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
package edu.scripps.fl.pubchem.app.relations;

import org.apache.commons.pipeline.StageException;
import org.dom4j.Document;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pipeline.DocumentStage;
import edu.scripps.fl.pipeline.SessionStage;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.PubChemFactory;

public class ProcessRelationsStage extends SessionStage {

	private static final Logger log = LoggerFactory.getLogger(DocumentStage.class);

	@Override
	public SessionFactory getSessionFactory() {
		return PubChemDB.getSessionFactory();
	}

	@Override
	public void innerProcess(Object obj) throws StageException {
		Document document = (Document) obj;
		try {
			int relations = PubChemFactory.getInstance().populateRelations(getSession(), document);
		} catch (Exception ex) {

			throw new StageException(this, ex);
		}
	}
}
