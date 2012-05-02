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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
@Table(name = "pcassay_xref")//, uniqueConstraints = { @UniqueConstraint(columnNames = { "assay_assay_id", "xref_xref_id", "panel_panel_id" }) })
public class PCAssayXRef implements Serializable {

	private PCAssay assay = null;
	private String comment = "";
	private Long id = -1L;
	private PCAssayPanel panel = null;
	private Boolean target = false;
	private Long taxon = -1L;
	private String taxonName = "";
	private String taxonCommon = "";
	private XRef xRef = null;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PCAssayXRef))
			return false;
		PCAssayXRef other = (PCAssayXRef) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@ManyToOne(optional = false)
    @JoinColumn(name="assay_assay_id", insertable=false, updatable=false, nullable=false)
	public PCAssay getAssay() {
		return assay;
	}

	@Column(name = "pcassay_xref_comment", length = 4000)
	public String getComment() {
		return comment;
	}

	@Id
	@Column(name = "pcassay_xref_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	@ManyToOne(optional = true)
	public PCAssayPanel getPanel() {
		return panel;
	}
	
	@Column(name = "pc_assay_xref_taxon")
	public Long getTaxon() {
		return taxon;
	}

	@Column(name = "pc_assay_xref_taxon_common")
	public String getTaxonCommon() {
		return taxonCommon;
	}

	@Column(name = "pc_assay_xref_taxon_name")
	public String getTaxonName() {
		return taxonName;
	}

	@ManyToOne(optional = false)
	public XRef getXRef() {
		return xRef;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Column(name = "pcassay_xref_istarget")
	public Boolean isTarget() {
		return target;
	}

	public void setAssay(PCAssay assay) {
		this.assay = assay;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setPanel(PCAssayPanel panel) {
		this.panel = panel;
	}

	public void setTarget(Boolean target) {
		this.target = target;
	}
	
	public void setTaxon(Long taxon) {
		this.taxon = taxon;
	}

	public void setTaxonCommon(String taxonCommon) {
		this.taxonCommon = taxonCommon;
	}

	public void setTaxonName(String taxonName) {
		this.taxonName = taxonName;
	}

	public void setXRef(XRef ref) {
		xRef = ref;
	}
	
	@Override
	public String toString() {
//		return ToStringBuilder.reflectionToString(this);
		return String.format("assay = %s, panel = %s, xref=%s", getAssay().getId(), null, getXRef().getId());
	}
	
	
	
}