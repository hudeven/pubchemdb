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
package edu.scripps.fl.pubchem.app.results;

import java.util.Map;

import org.apache.commons.pipeline.StageException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import edu.scripps.fl.pipeline.CommitStage;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.db.PCAssayResult;

public class CommitAssayResultStage extends CommitStage {

	@Override
	public SessionFactory getSessionFactory() {
		return PubChemDB.getSessionFactory();
	}

	@Override
	public void doSave(Object obj) throws StageException {
		Object[] objs = (Object[]) obj;
		Integer aid = (Integer) objs[0];
		Map<Integer, AIDTracker> trackers = (Map<Integer, AIDTracker>) objs[1];
		PCAssayResult result = (PCAssayResult) objs[2];

		Session session = getSession();
		session.save(result);
		session.flush(); // problem with duplicate objects, don't know why :-?
		session.clear();

		AIDTracker tracker = trackers.get(aid);
		int saved = tracker.recordsSaved.incrementAndGet(); // no more records for this aid
		if (tracker.numRecords > 0 && saved >= tracker.numRecords) {
			trackers.remove(aid);
			emit(aid);
		}
	}
}
