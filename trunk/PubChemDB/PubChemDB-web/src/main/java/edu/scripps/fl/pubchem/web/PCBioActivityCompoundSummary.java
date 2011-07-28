/*
 * Copyright 2011 The Scripps Research Institute
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
package edu.scripps.fl.pubchem.web;

import org.apache.commons.lang.builder.ToStringBuilder;

public class PCBioActivityCompoundSummary {

	private Long CID;
	private String IUPAC;
	private String synonyms;
	private Integer bioAssayProbes;
	private Integer bioAssayActives;
	private Integer bioAssayTested;
	private Double activeContentrationBelow1uM;
	private Double activeContentrationBelow1nM;
	private Integer activeProteins;
	private Integer testedProteins;
	private Double activeContentrationLowerBound;
	private Double activeContentrationUpperBound;
	
	public Long getCID() {
		return CID;
	}
	public void setCID(Long cID) {
		CID = cID;
	}
	public String getIUPAC() {
		return IUPAC;
	}
	public void setIUPAC(String iUPAC) {
		IUPAC = iUPAC;
	}
	public String getSynonyms() {
		return synonyms;
	}
	public void setSynonyms(String synonyms) {
		this.synonyms = synonyms;
	}
	public Integer getBioAssayProbes() {
		return bioAssayProbes;
	}
	public void setBioAssayProbes(Integer bioAssayProbes) {
		this.bioAssayProbes = bioAssayProbes;
	}
	public Integer getBioAssayActives() {
		return bioAssayActives;
	}
	public void setBioAssayActives(Integer bioAssayActives) {
		this.bioAssayActives = bioAssayActives;
	}
	public Integer getBioAssayTested() {
		return bioAssayTested;
	}
	public void setBioAssayTested(Integer bioAssayTested) {
		this.bioAssayTested = bioAssayTested;
	}
	public Double getActiveContentrationBelow1uM() {
		return activeContentrationBelow1uM;
	}
	public void setActiveContentrationBelow1uM(Double activeContentrationBelow1uM) {
		this.activeContentrationBelow1uM = activeContentrationBelow1uM;
	}
	public Double getActiveContentrationBelow1nM() {
		return activeContentrationBelow1nM;
	}
	public void setActiveContentrationBelow1nM(Double activeContentrationBelow1nM) {
		this.activeContentrationBelow1nM = activeContentrationBelow1nM;
	}
	public Integer getActiveProteins() {
		return activeProteins;
	}
	public void setActiveProteins(Integer activeProteins) {
		this.activeProteins = activeProteins;
	}
	public Integer getTestedProteins() {
		return testedProteins;
	}
	public void setTestedProteins(Integer testedProteins) {
		this.testedProteins = testedProteins;
	}
	public Double getActiveContentrationLowerBound() {
		return activeContentrationLowerBound;
	}
	public void setActiveContentrationLowerBound(Double activeContentrationLowerBound) {
		this.activeContentrationLowerBound = activeContentrationLowerBound;
	}
	public Double getActiveContentrationUpperBound() {
		return activeContentrationUpperBound;
	}
	public void setActiveContentrationUpperBound(Double activeContentrationUpperBound) {
		this.activeContentrationUpperBound = activeContentrationUpperBound;
	}
	
	@Override public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
