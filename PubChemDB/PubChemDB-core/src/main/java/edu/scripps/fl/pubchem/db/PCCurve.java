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
package edu.scripps.fl.pubchem.db;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import edu.scripps.fl.curves.Curve;

@Entity
@Table(name = "curves")
public class PCCurve extends Curve implements Serializable {

	private PCAssayResult assayResult;

	@OneToOne
	public PCAssayResult getAssayResult() {
		return assayResult;
	}

	public void setAssayResult(PCAssayResult assayResult) {
		this.assayResult = assayResult;
	}

}
