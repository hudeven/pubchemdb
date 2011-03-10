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
package edu.scripps.fl.pubchem.app.curves;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.pipeline.StageException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;

import edu.scripps.fl.pipeline.SessionStage;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.db.PCAssay;
import edu.scripps.fl.pubchem.db.PCAssayColumn;
import edu.scripps.fl.pubchem.db.PCAssayResult;

public class AIDStage extends SessionStage {

	private int referenceAID;
	private List<Double> referenceConcentrations;
	private int topX = 1000;

	public int getTopX() {
		return topX;
	}

	public void setTopX(int topX) {
		this.topX = topX;
	}

	public int getReferenceAID() {
		return referenceAID;
	}

	public void setReferenceAID(int referenceAID) {
		this.referenceAID = referenceAID;
	}

	@Override
	public SessionFactory getSessionFactory() {
		return PubChemDB.getSessionFactory();
	}

	protected List<Double> getConcentrations(int aid) {
		PCAssay assay = (PCAssay) getSession().load(PCAssay.class, aid);
		List<Double> concentrations = new ArrayList();
		for (PCAssayColumn column : assay.getColumns()) {
			Double conc = column.getTestedConcentration();
			if (conc != null) {
				conc = conc * 1E-6; // conc. always in uM
				concentrations.add(conc);
			}
		}
		return concentrations;
	}

	@Override
	public void innerPreprocess() throws StageException {
		try {
			this.referenceConcentrations = getConcentrations(getReferenceAID());
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}

	@Override
	public void innerProcess(Object obj) throws StageException {
		try {
			Integer aid = (Integer) obj;
			List<Double> concentrations = getConcentrations(aid);

			Query query2 = getSession().createQuery("from PCAssayResult where assay.AID = ? and SID = ?");
			// get top 10000 results from reference aid
			int counter = 0;
			Query query = getSession().createQuery(
					"from PCAssayResult where assay.AID = ? AND ( outcome = 'Active' OR outcome = 'Probe' ) ORDER BY rankScore desc");
			query.setInteger(0, getReferenceAID());
			Iterator<PCAssayResult> iter = query.iterate();
			while (iter.hasNext() && counter++ < getTopX()) {
				PCAssayResult referenceResult = iter.next();
				referenceResult.getTestedValues();
				emit(new Object[] { referenceConcentrations, referenceResult });

				query2.setInteger(0, aid);
				query2.setLong(1, referenceResult.getSID());
				Iterator<PCAssayResult> iter2 = query2.iterate();
				while (iter2.hasNext()) {
					PCAssayResult result = iter2.next();
					result.getTestedValues();
					emit(new Object[] { concentrations, result });
				}
			}
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}
}
