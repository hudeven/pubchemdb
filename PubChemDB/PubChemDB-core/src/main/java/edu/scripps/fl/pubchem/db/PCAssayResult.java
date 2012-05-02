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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections.list.GrowthList;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

@Entity
@Table(name = "pcassay_result")
// indexes are only created on "create", not "update"
@org.hibernate.annotations.Table(appliesTo = "pcassay_result", indexes = {
		@Index(name = "idx_pcassay_result_sid", columnNames = { "sid" }),
		@Index(name = "idx_pcassay_result_cid", columnNames = { "cid" }),
		@Index(name = "idx_pcassay_result_assay_id", columnNames = { "assay_assay_id" }) })
@TypeDefs( {
		@TypeDef(name = "DoubleListStringType", typeClass = edu.scripps.fl.hibernate.DoubleListStringType.class),
		@TypeDef(name = "StringListStringType", typeClass = edu.scripps.fl.hibernate.StringListStringType.class) })
public class PCAssayResult implements Serializable {
	private List<String> allValues = GrowthList.decorate(new ArrayList());
	private PCAssay assay = null;
	private Long CID = null;
	private String comments = "";
	private PCCurve curve;
	private Long id = -1L;
	private String outcome = "";
	private PCAssayColumn primaryColumn = null;
	private Double primaryValue = null;
	private String primaryValueAsString = "";
	private String qualifier = "";
	private Integer rankScore = null;
	private Long SID = null;
	private List<Double> testedValues = GrowthList.decorate(new ArrayList());
	private String URL = "";
	private String Xref = "";

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PCAssayResult))
			return false;
		PCAssayResult other = (PCAssayResult) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Type(type = "StringListStringType")
	@Fetch(value = FetchMode.SELECT)
	@Column(name = "result_all_values", length = 4000)
	public List<String> getAllValues() {
		return allValues;
	}

	@ManyToOne(optional = false)
	public PCAssay getAssay() {
		return assay;
	}

	@Column(name = "cid")
	// @Index(name = "idx_pcassay_result_cid") // define at class level instead.
	// Include both and you get 2 indexes!
	public Long getCID() {
		return CID;
	}

	@Column(name = "result_comments")
	public String getComments() {
		return comments;
	}

	@OneToOne(cascade = CascadeType.ALL)
	public PCCurve getCurve() {
		return curve;
	}

	@Id
	@Column(name = "result_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return id;
	}

	@Column(name = "result_outcome")
	public String getOutcome() {
		return outcome;
	}

	@ManyToOne(optional = true, fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
	public PCAssayColumn getPrimaryColumn() {
		return primaryColumn;
	}

	@Column(name = "result_primary_value")
	public Double getPrimaryValue() {
		return primaryValue;
	}

	@Column(name = "result_primary_value_string")
	public String getPrimaryValueAsString() {
		return primaryValueAsString;
	}

	@Column(name = "result_qualifier")
	public String getQualifier() {
		return qualifier;
	}

	@Column(name = "result_rank_score")
	public Integer getRankScore() {
		return rankScore;
	}

	@Column(name = "sid", nullable = false)
	// @Index(name = "idx_pcassay_result_sid") // define at class level instead.
	// Include both and you get 2 indexes!
	public Long getSID() {
		return SID;
	}

	@Type(type = "DoubleListStringType")
	@Fetch(value = FetchMode.SELECT)
	@Column(name = "result_tested_values", length = 4000)
	public List<Double> getTestedValues() {
		return testedValues;
	}

	@Column(name = "result_url")
	public String getURL() {
		return URL;
	}

	@Transient
	public Object getValue(PCAssayColumn column) {
		Object obj = ConvertUtils.convert(getAllValues().get(
				column.getTID() - 1), column.getTypeClass());
		return obj;
	}

	@Column(name = "result_xref")
	public String getXref() {
		return Xref;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public void setAllValues(List<String> allValues) {
		this.allValues = allValues;
	}

	public void setAssay(PCAssay assay) {
		this.assay = assay;
	}

	public void setCID(Long cid) {
		CID = cid;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public void setCurve(PCCurve curve) {
		this.curve = curve;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setOutcome(String outcome) {
		this.outcome = outcome;
	}

	public void setPrimaryColumn(PCAssayColumn activeColumn) {
		this.primaryColumn = activeColumn;
	}

	public void setPrimaryValue(Double primaryValue) {
		this.primaryValue = primaryValue;
	}

	public void setPrimaryValueAsString(String primaryValueAsString) {
		this.primaryValueAsString = primaryValueAsString;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	public void setRankScore(Integer rankScore) {
		this.rankScore = rankScore;
	}

	public void setSID(Long sid) {
		SID = sid;
	}

	public void setTestedValues(List<Double> testedValues) {
		this.testedValues = testedValues;
	}

	public void setURL(String url) {
		URL = url;
	}

	public void setXref(String xref) {
		Xref = xref;
	}
}
