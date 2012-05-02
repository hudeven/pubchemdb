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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.builder.ToStringBuilder;

@Entity
@Table(name = "pcassay_column")//, uniqueConstraints = { @UniqueConstraint(columnNames = { "assay_assay_id", "column_tid" }) })
public class PCAssayColumn implements Serializable {

	private boolean activeConcentration;
	private PCAssay assay = null;
	private Integer curvePlotLabel;
	private String description = "";
	private Integer id = -1;
	private String name = "";
	private PCAssayPanel panel = null;
	private String panelReadoutType;
	private Double testedConcentration;
	private String testedConcentrationUnit;
	private Integer TID;
	private String type;

	private Class typeClass = null;

	private String unit;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PCAssayColumn))
			return false;
		PCAssayColumn other = (PCAssayColumn) obj;
		if (TID == null) {
			if (other.TID != null)
				return false;
		} else if (!TID.equals(other.TID))
			return false;
		if (assay == null) {
			if (other.assay != null)
				return false;
		} else if (!assay.equals(other.assay))
			return false;
		return true;
	}

	@ManyToOne(optional = false)
	@JoinColumn(name="assay_assay_id", insertable=false, updatable=false, nullable=false)
	public PCAssay getAssay() {
		return assay;
	}
	
	@Column(name = "column_curve_plot_label")
	public Integer getCurvePlotLabel() {
		return curvePlotLabel;
	}

	@Column(name = "column_description", length = 1000)
	public String getDescription() {
		return description;
	}

	@Transient
	public String getEndpointType() {
		String[] patterns = new String[] { "([AEIC]C50)", "(MIC)", "(Ki)", "(Potency)", "(Kd)" };
		for (String pattern : patterns) {
			Pattern rxPattern = Pattern.compile(pattern);
			Matcher matcher = rxPattern.matcher(getName());
			if (matcher.find()) {
				return matcher.group(1);
			} else {
				matcher = rxPattern.matcher(getDescription());
				if (matcher.find()) {
					return matcher.group(1);
				}
			}
		}
		return null;
	}

	@Id
	@Column(name = "column_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}

	@Column(name = "column_name", nullable = false)
	public String getName() {
		return name;
	}

	@ManyToOne(optional = true)
	public PCAssayPanel getPanel() {
		return panel;
	}

	@Column(name = "column_panel_readout_type")
	public String getPanelReadoutType() {
		return panelReadoutType;
	}

	@Transient
	public Integer getPanelReadoutTypeId() {
		return PCPanelReadout.getReadoutTypeId(getPanelReadoutType());
	}

	@Column(name = "column_tested_conc")
	public Double getTestedConcentration() {
		return testedConcentration;
	}

	@Column(name = "column_tested_conc_unit")
	public String getTestedConcentrationUnit() {
		return testedConcentrationUnit;
	}

	@Column(name = "column_tid", nullable = false)
	public Integer getTID() {
		return TID;
	}

	@Column(name = "column_type")
	public String getType() {
		return type;
	}

	@Transient
	public Class getTypeClass() {
		if (null == typeClass) {
			if ("float".equals(getType()))
				typeClass = Double.class;
			else if ("string".equals(getType()))
				typeClass = String.class;
			else if ("int".equals(getType()))
				typeClass = Integer.class;
			else if ("bool".equals(getType()))
				typeClass = Boolean.class;
		}
		return typeClass;
	}

	@Transient
	public Integer getTypeId() {
		return PCResultType.getResultTypeId(getType());
	}

	@Column(name = "column_unit")
	public String getUnit() {
		return unit;
	}

	@Transient
	public Integer getUnitId() {
		return PCResultUnit.getResultTypeId(getUnit());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((TID == null) ? 0 : TID.hashCode());
		result = prime * result + ((assay == null) ? 0 : assay.hashCode());
		return result;
	}

	@Column(name = "column_active_conc")
	public boolean isActiveConcentration() {
		return activeConcentration;
	}

	public void setActiveConcentration(boolean activeConcentration) {
		this.activeConcentration = activeConcentration;
	}

	public void setAssay(PCAssay assay) {
		this.assay = assay;
	}

	public void setCurvePlotLabel(Integer curvePlotLabel) {
		this.curvePlotLabel = curvePlotLabel;
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

	public void setPanel(PCAssayPanel panel) {
		this.panel = panel;
	}

	public void setPanelReadoutType(String panelReadoutType) {
		this.panelReadoutType = panelReadoutType;
	}

	public void setTestedConcentration(Double testedConcentration) {
		this.testedConcentration = testedConcentration;
	}

	public void setTestedConcentrationUnit(String testedConcentrationUnit) {
		this.testedConcentrationUnit = testedConcentrationUnit;
	}

	public void setTID(Integer tid) {
		TID = tid;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}