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

import java.util.List;

import org.apache.commons.pipeline.StageException;
import org.hibernate.SessionFactory;

import edu.scripps.fl.curves.CurveFit;
import edu.scripps.fl.pipeline.SessionStage;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.db.PCAssayResult;
import edu.scripps.fl.pubchem.db.PCCurve;

public class CalculateCurveStage extends SessionStage {

	@Override
	public SessionFactory getSessionFactory() {
		return PubChemDB.getSessionFactory();
	}

	@Override
	public void innerProcess(Object obj) throws StageException {
		try {
			List<Double> concentrations = (List<Double>) ((Object[]) obj)[0];
			PCAssayResult result = (PCAssayResult) ((Object[]) obj)[1];

			PCCurve curve = new PCCurve();
			for (int ii = 0; ii < result.getTestedValues().size(); ii++) {
				Double response = result.getTestedValues().get(ii);
				if (null != response) {
					curve.add(response, concentrations.get(ii));
				}
			}
			if (curve.getResponses().size() > 0) {
				CurveFit.fit(curve);
				curve.setAssayResult(result);
				emit(curve);
			}
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}
}
