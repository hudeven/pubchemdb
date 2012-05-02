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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.CollectionOfElements;

@Entity
@Table(name = "pccompound")
public class PCCompound implements Serializable {

	private Long CID;
	private Double exactWeight;
	private String formula;
	private Integer hBondAcceptors;
	private Integer hBondDonors;
	private Long id = -1L;
	private String IUPACName;
	private Double molWeight;
	private Integer rotatableBonds;
	private String smiles;
	private String sortableFormula;
	private String structure;
	private Set<String> synonyms = new HashSet<String>();
	private Integer totalCharge;
	private Double TPSA;
	private String traditionalName;
	private Double xLogP;

	@Column(name = "cid", unique = true)
	public Long getCID() {
		return CID;
	}

	@Column(name = "exact_weight")
	public Double getExactWeight() {
		return exactWeight;
	}

	@Column(name = "cd_formula")
	public String getFormula() {
		return formula;
	}

	@Column(name = "h_bond_acceptors")
	public Integer getHBondAcceptors() {
		return hBondAcceptors;
	}

	@Column(name = "h_bond_donors")
	public Integer getHBondDonors() {
		return hBondDonors;
	}

	@Id
	@Column(name = "cd_id")
	public Long getId() {
		return id;
	}

	@Column(name = "iupac_name", length = 1000)
	public String getIUPACName() {
		return IUPACName;
	}

//	@Transient
//	public Molecule getMolecule() {
//		try {
//			Molecule mol = MolImporter.importMol(getStructure());
//			return mol;
//		} catch (MolFormatException ex) {
//			return null;
//		}
//	}

	@Column(name = "cd_molweight")
	public Double getMolWeight() {
		return molWeight;
	}

	@Column(name = "rotatable_bonds")
	public Integer getRotatableBonds() {
		return rotatableBonds;
	}

	@Column(name = "cd_smiles")
	public String getSmiles() {
		return smiles;
	}

	@Column(name = "cd_sortable_formula")
	public String getSortableFormula() {
		return formula;
	}

	@Column(name = "cd_structure")
	public String getStructure() {
		return structure;
	}

	@CollectionOfElements
	@JoinTable(name = "pccompound_synonyms", joinColumns = @JoinColumn(name = "cid") // References parent
	)
	@Column(name = "synonym_name")
	public Set<String> getSynonyms() {
		return synonyms;
	}

	@Column(name = "total_charge")
	public Integer getTotalCharge() {
		return totalCharge;
	}

	@Column(name = "tpsa")
	public Double getTPSA() {
		return TPSA;
	}

	@Column(name = "traditional_name", length = 1000)
	public String getTraditionalName() {
		return traditionalName;
	}

	@Column(name = "xlogp")
	public Double getXLogP() {
		return xLogP;
	}

	public void setCID(Long cid) {
		CID = cid;
	}

	public void setExactWeight(Double exactWeight) {
		this.exactWeight = exactWeight;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public void setHBondAcceptors(Integer bondAcceptors) {
		hBondAcceptors = bondAcceptors;
	}

	public void setHBondDonors(Integer bondDonors) {
		hBondDonors = bondDonors;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setIUPACName(String name) {
		IUPACName = name;
	}

	public void setMolFormula(String formula) {
		this.formula = formula;
	}

	public void setMolWeight(Double molWeight) {
		this.molWeight = molWeight;
	}

	public void setRotatableBonds(Integer rotatableBonds) {
		this.rotatableBonds = rotatableBonds;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

	public void setSortableFormula(String formula) {
		this.sortableFormula = formula;
	}

	public void setStructure(String structure) {
		this.structure = structure;
	}

	public void setSynonyms(Set<String> synonyms) {
		this.synonyms = synonyms;
	}

	public void setTotalCharge(Integer totalCharge) {
		this.totalCharge = totalCharge;
	}

	public void setTPSA(Double tpsa) {
		TPSA = tpsa;
	}

	public void setTraditionalName(String traditionalName) {
		this.traditionalName = traditionalName;
	}

	public void setXLogP(Double logP) {
		xLogP = logP;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
