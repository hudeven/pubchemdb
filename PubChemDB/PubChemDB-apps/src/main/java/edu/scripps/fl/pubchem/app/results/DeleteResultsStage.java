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

import org.apache.commons.pipeline.StageException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import edu.scripps.fl.pipeline.SessionStage;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.db.PCAssay;

public class DeleteResultsStage extends SessionStage {

	@Override
	public SessionFactory getSessionFactory() {
		return PubChemDB.getSessionFactory();
	}

	@Override
	public void process(Object obj) throws StageException {
		Integer aid = (Integer) obj;
		try {
			Query query = getSession().createQuery("from PCAssay where AID = :aid");
			query.setParameter("aid", aid);
			PCAssay assay = (PCAssay) query.uniqueResult();
			query = getSession().createQuery("delete from PCAssayResult where assay.id = :id");
			query.setParameter("id", assay.getId());
			int deleted = query.executeUpdate();
			emit(aid);
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}
}
