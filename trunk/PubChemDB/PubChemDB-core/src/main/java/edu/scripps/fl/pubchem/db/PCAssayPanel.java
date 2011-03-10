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
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
@Table(name = "pcassay_panel", uniqueConstraints = { @UniqueConstraint(columnNames = { "assay_assay_id", "panel_number" }) })
public class PCAssayPanel implements Serializable {

	private String activityOutcomeMethod = "";
	private PCAssay assay;
	private String comment = "";
	private String description = "";
	private Integer id;
	private String name = "";
	private Integer panelNumber;
	private String protocol = "";

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PCAssayPanel))
			return false;
		PCAssayPanel other = (PCAssayPanel) obj;
		if (panelNumber == null) {
			if (other.panelNumber != null)
				return false;
		} else if (!panelNumber.equals(other.panelNumber))
			return false;
		if (assay == null) {
			if (other.assay != null)
				return false;
		} else if (!assay.equals(other.assay))
			return false;
		return true;
	}

	@Column(name = "panel_outcome_method")
	public String getActivityOutcomeMethod() {
		return activityOutcomeMethod;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name = "assay_assay_id")
	public PCAssay getAssay() {
		return assay;
	}

	@Column(name = "panel_comment", length = 500)
	public String getComment() {
		return comment;
	}

	@Column(name = "panel_description", length = 500)
	public String getDescription() {
		return description;
	}

	@Transient
	public XRef getGene() {
		List<PCAssayXRef> list = new ArrayList();
		CollectionUtils.select(assay.getAssayXRefs(), new Predicate() {
			public boolean evaluate(Object object) {
				PCAssayXRef xref = (PCAssayXRef) object;
				if (xref.getPanel() != null)
					if (xref.getPanel().getPanelNumber() == getPanelNumber() 
							&& xref.getXRef() != null && "gene".equals(xref.getXRef().getDatabase()))
						return true;
				return false;
			}
		}, list);
		return list.size() > 0 ? list.get(0).getXRef() : null;
	}

	@Id
	@Column(name = "panel_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}

	@Column(name = "panel_name")
	public String getName() {
		return name;
	}

	@Column(name = "panel_number", nullable = false)
	public Integer getPanelNumber() {
		return panelNumber;
	}

	@Column(name = "panel_protocol", length = 500)
	public String getProtocol() {
		return protocol;
	}

	@Transient
	public XRef getTarget() {
		List<PCAssayXRef> list = new ArrayList();
		CollectionUtils.select(assay.getAssayXRefs(), new Predicate() {
			public boolean evaluate(Object object) {
				PCAssayXRef xref = (PCAssayXRef) object;
				if (xref.getPanel() != null)
					if (xref.getPanel().getPanelNumber() == getPanelNumber() && xref.isTarget() == true)
						return true;
				return false;
			}
		}, list);
		return list.size() > 0 ? list.get(0).getXRef() : null;
	}

	@Transient
	public XRef getTaxonomy() {
		List<PCAssayXRef> list = new ArrayList();
		CollectionUtils.select(assay.getAssayXRefs(), new Predicate() {
			public boolean evaluate(Object object) {
				PCAssayXRef xref = (PCAssayXRef) object;
				if (xref.getPanel() != null)
					if (xref.getPanel().getPanelNumber() == getPanelNumber())
						if (xref.getXRef() != null && "taxonomy".equals(xref.getXRef().getDatabase()))
							return true;
				return false;
			}
		}, list);
		return list.size() > 0 ? list.get(0).getXRef() : null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((panelNumber == null) ? 0 : panelNumber.hashCode());
		result = prime * result + ((assay == null) ? 0 : assay.hashCode());
		return result;
	}

	public void setActivityOutcomeMethod(String activityOutcomeMethod) {
		this.activityOutcomeMethod = activityOutcomeMethod;
	}

	public void setAssay(PCAssay assay) {
		this.assay = assay;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPanelNumber(Integer panelNumber) {
		this.panelNumber = panelNumber;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}