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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "pcassay")
public class PCAssay implements Serializable {

	private Integer activeCidCount; // eSummary
	private Integer activePanelCidCount; // eSummary
	private Integer activePanelSidCount; // eSummary
	private Integer activeSidCount; // eSummary
	private String activityOutcomeMethod = "";
	private Integer AID = null;
	private String assayType = "";
	private List<PCAssayXRef> assayXRefs = new ArrayList<PCAssayXRef>();
	private List<PCAssayColumn> columns = new ArrayList<PCAssayColumn>();
	private String comment = "";
	private Map<String, String> comments = new HashMap<String, String>();
	private Date depositDate = null;
	private String description = "";
	private String extRegId = null;
	private String grantNumber = "";
	private Boolean hasScore = Boolean.FALSE;
	private Date holdUntilDate = null;
	private Integer id = null;
	private Integer inactiveCidCount;
	private Integer inactiveSidCount;
	private Integer inconclusiveCidCount;
	private Integer inconclusiveSidCount;
	private Boolean isPanel = Boolean.FALSE;
	private Date lastDataChange = null;
	private Date modifyDate = null;
	private String name = null;
	private Integer numberOfCidsWithMicroMolActivity;
	private Integer numberOfCidsWithNanoMolActivity;
	private Integer numberOfSidsWithMicroMolActivity;
	private Integer numberOfSidsWithNanoMolActivity;
	private Boolean onHold = Boolean.FALSE;
	private String panelDescription = "";
	private String panelName = "";
	private List<PCAssayPanel> panels = new ArrayList<PCAssayPanel>();
	private Integer probeCidCount = null;
	private Integer probeSidCount = null;
	private String projectCategory = "";
	private String protocol = "";
	private Integer readoutCount = null;
	private Integer revision = null;
	private String sourceName = "";
	private Integer targetCount = null;
	private Integer totalCidCount = null;
	private Integer totalSidCount = null;
	private Integer unspecifiedCidCount = null;
	private Integer unspecifiedSidCount = null;
	private Integer version = null;
	private Boolean versionChanged = Boolean.FALSE;
	private List<PCAssayResult> results = new ArrayList<PCAssayResult>();

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof PCAssay))
			return false;
		PCAssay other = (PCAssay) obj;
		if (AID == null) {
			if (other.AID != null)
				return false;
		} else if (!AID.equals(other.AID))
			return false;
		return true;
	}
	
	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "assay_assay_aid", referencedColumnName = "assay_aid")
	public List<PCAssayResult> getResults() {
		return results;
	}

	public void setResults(List<PCAssayResult> results) {
		this.results = results;
	}

	@Column(name = "assay_active_cid_count")
	public Integer getActiveCidCount() {
		return activeCidCount;
	}

	@Transient
	public PCAssayColumn getActiveColumn() {
		for (PCAssayColumn column : getColumns()) {
			if (column.isActiveConcentration())
				return column;
		}
		return null;
	}

	@Column(name = "assay_active_panel_cid_count")
	public Integer getActivePanelCidCount() {
		return activePanelCidCount;
	}

	@Column(name = "assay_active_panel_sid_count")
	public Integer getActivePanelSidCount() {
		return activePanelSidCount;
	}

	@Column(name = "assay_active_sid_count")
	public Integer getActiveSidCount() {
		return activeSidCount;
	}

	@Index(name = "idx_pcassay_method")
	@Column(name = "assay_activity_outcome_method")
	public String getActivityOutcomeMethod() {
		return activityOutcomeMethod;
	}

	@Transient
	public String getActivityOutcomeMethodFormatted() {
		return PCOutcomeMethod.getActivityOutcomeMethodFormatted(getActivityOutcomeMethod());
	}

	@Transient
	public Integer getActivityOutcomeMethodId() {
		return PCOutcomeMethod.getActivityOutcomeMethodId(getActivityOutcomeMethod());
	}

	@Column(name = "assay_aid", unique = true, nullable = true)
	public Integer getAID() {
		return AID;
	}

	@Column(name = "assay_type")
	public String getAssayType() {
		return assayType;
	}

	@OneToMany
	@OrderColumn(name = "pcassay_xref_position")
	@JoinColumn(name = "assay_assay_id", nullable = false)
	public List<PCAssayXRef> getAssayXRefs() {
		return assayXRefs;
	}

	@CollectionOfElements(fetch = FetchType.EAGER)
	@JoinTable(name = "pcassay_comments", joinColumns = @JoinColumn(name = "assay_assay_id"))
	@Column(name = "comment_name", nullable = false)
	@org.hibernate.annotations.MapKey(columns = { @Column(name = "comment_value") })
	public Map<String, String> getCategorizedComments() {
		return comments;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
	@JoinColumn(name = "assay_assay_id", nullable = false)
	public List<PCAssayColumn> getColumns() {
		return columns;
	}

	@Lob
	@Column(name = "assay_comments")
	public String getComment() {
		return comment;
	}

	@Column(name = "assay_deposit_date")
	public Date getDepositDate() {
		return depositDate;
	}

	@Lob
	@Index(name = "idx_pcassay_description")
	@Column(name = "assay_description")
	public String getDescription() {
		return description;
	}

	@Column(name = "assay_ext_reg_id", nullable = false)
	public String getExtRegId() {
		return extRegId;
	}

	@Column(name = "assay_grant_number")
	public String getGrantNumber() {
		return grantNumber;
	}

	@Type(type = "true_false")
	@Column(name = "assay_has_score", nullable = false)
	public Boolean getHasScore() {
		return hasScore;
	}

	@Column(name = "assay_hold_until_date")
	public Date getHoldUntilDate() {
		return holdUntilDate;
	}

	@Id
	@Column(name = "assay_id")
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Integer getId() {
		return id;
	}

	@Column(name = "assay_inactive_cid_count")
	public Integer getInactiveCidCount() {
		return inactiveCidCount;
	}

	@Column(name = "assay_inactive_sid_count")
	public Integer getInactiveSidCount() {
		return inactiveSidCount;
	}

	@Column(name = "assay_inconclusive_cid_count")
	public Integer getInconclusiveCidCount() {
		return inconclusiveCidCount;
	}

	@Column(name = "assay_inconclusive_sid_count")
	public Integer getInconclusiveSidCount() {
		return inconclusiveSidCount;
	}

	@Column(name = "assay_last_data_change")
	public Date getLastDataChange() {
		return lastDataChange;
	}

	@Column(name = "assay_modify_date")
	public Date getModifyDate() {
		return modifyDate;
	}

	@Index(name = "idx_pcassay_name")
	@Column(name = "assay_name", length=2000)
	public String getName() {
		return name;
	}

	@Column(name = "assay_cids_micromolar_activity")
	public Integer getNumberOfCidsWithMicroMolActivity() {
		return numberOfCidsWithMicroMolActivity;
	}

	@Column(name = "assay_cids_nanomolar_activity")
	public Integer getNumberOfCidsWithNanoMolActivity() {
		return numberOfCidsWithNanoMolActivity;
	}

	@Column(name = "assay_sids_micromolar_activity")
	public Integer getNumberOfSidsWithMicroMolActivity() {
		return numberOfSidsWithMicroMolActivity;
	}

	@Column(name = "assay_sids_nanomolar_activity")
	public Integer getNumberOfSidsWithNanoMolActivity() {
		return numberOfSidsWithNanoMolActivity;
	}

	@Type(type = "true_false")
	@Column(name = "assay_on_hold", nullable = false)
	public Boolean getOnHold() {
		return onHold;
	}

	@Column(name = "assay_panel_description")
	public String getPanelDescription() {
		return panelDescription;
	}

	@Column(name = "assay_panel_name")
	public String getPanelName() {
		return panelName;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.ALL })
	@JoinColumn(name = "panel_assay_id", nullable = false)
	public List<PCAssayPanel> getPanels() {
		return panels;
	}

	@Column(name = "assay_probe_cid_count")
	public Integer getProbeCidCount() {
		return probeCidCount;
	}

	@Column(name = "assay_probe_sid_count")
	public Integer getProbeSidCount() {
		return probeSidCount;
	}

	@Column(name = "assay_project_category")
	public String getProjectCategory() {
		return projectCategory;
	}

	// @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	// @JoinColumn(name = "pcassay_xref_id")
	// @org.hibernate.annotations.Cascade(value = org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
	// @org.hibernate.annotations.IndexColumn(name = "pcassay_xref_position")

	@Transient
	public String getProjectCategoryFormatted() {
		return PCProjectCategory.getProjectCategoryFormatted(getProjectCategory());
	}

	@Transient
	public Integer getProjectCategoryId() {
		return PCProjectCategory.getProjectCategoryId(getProjectCategory());
	}

	@Lob
	@Column(name = "assay_protocol")
	public String getProtocol() {
		return protocol;
	}

	@Transient
	public PCAssayColumn getQualifierColumn() {
		PCAssayColumn activeCol = getActiveColumn();
		if (activeCol != null) {
			for (PCAssayColumn column : getColumns()) {
				if (column.getTID() != null && 1 == (activeCol.getTID() - column.getTID()) && column.getName().toLowerCase().contains("qualifier"))
					return column;
			}
		}
		return null;
	}

	@Column(name = "assay_readout_count")
	public Integer getReadoutCount() {
		return readoutCount;
	}

	@Column(name = "assay_revision")
	public Integer getRevision() {
		return revision;
	}

	@Index(name = "idx_pcassay_sourcename")
	@Column(name = "assay_source_name")
	public String getSourceName() {
		return sourceName;
	}

	@Column(name = "assay_target_count")
	public Integer getTargetCount() {
		return targetCount;
	}

	@Transient
	public List<PCAssayColumn> getTestedColumns() {
		List<PCAssayColumn> testedCols = new ArrayList<PCAssayColumn>();
		for (PCAssayColumn col : getColumns()) {
			if (col.getTestedConcentration() != null) {
				testedCols.add(col);
			}
		}
		Collections.sort(testedCols, new java.util.Comparator<PCAssayColumn>() {
			public int compare(PCAssayColumn c1, PCAssayColumn c2) {
				return c1.getTID().compareTo(c2.getTID());
			}
		});
		return testedCols;
	}

	@Column(name = "assay_total_cid_count")
	public Integer getTotalCidCount() {
		return totalCidCount;
	}

	@Column(name = "assay_total_sid_count")
	public Integer getTotalSidCount() {
		return totalSidCount;
	}

	@Column(name = "assay_unspecified_cid_count")
	public Integer getUnspecifiedCidCount() {
		return unspecifiedCidCount;
	}

	@Column(name = "assay_unspecified_sid_count")
	public Integer getUnspecifiedSidCount() {
		return unspecifiedSidCount;
	}

	@Column(name = "assay_version")
	public Integer getVersion() {
		return version;
	}

	@Type(type = "true_false")
	@Column(name = "assay_version_changed", nullable = false)
	public Boolean getVersionChanged() {
		return versionChanged;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((AID == null) ? 0 : AID.hashCode());
		return result;
	}

	@Type(type = "true_false")
	@Column(name = "assay_is_panel", nullable = false)
	public Boolean isPanel() {
		return isPanel;
	}

	public void setActiveCidCount(Integer activeCidCount) {
		this.activeCidCount = activeCidCount;
	}

	public void setActivePanelCidCount(Integer activePanelCidCount) {
		this.activePanelCidCount = activePanelCidCount;
	}

	public void setActivePanelSidCount(Integer activePanelSidCount) {
		this.activePanelSidCount = activePanelSidCount;
	}

	public void setActiveSidCount(Integer activeSidCount) {
		this.activeSidCount = activeSidCount;
	}

	public void setActivityOutcomeMethod(String activityOutcomeMethod) {
		this.activityOutcomeMethod = activityOutcomeMethod;
	}

	public void setAID(Integer aid) {
		AID = aid;
	}

	public void setAssayType(String assayType) {
		this.assayType = assayType;
	}

	public void setAssayXRefs(List<PCAssayXRef> assayXRefs) {
		this.assayXRefs = assayXRefs;
	}

	public void setCategorizedComments(Map<String, String> comments) {
		this.comments = comments;
	}

	public void setColumns(List<PCAssayColumn> columns) {
		this.columns = columns;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setDepositDate(Date depositDate) {
		this.depositDate = depositDate;
	}

	public void setDescription(String assayDescription) {
		this.description = assayDescription;
	}

	public void setExtRegId(String extRegId) {
		this.extRegId = extRegId;
	}

	public void setGrantNumber(String grantNumber) {
		this.grantNumber = grantNumber;
	}

	public void setHasScore(Boolean hasScore) {
		this.hasScore = hasScore;
	}

	public void setHoldUntilDate(Date holdUntilDate) {
		this.holdUntilDate = holdUntilDate;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	// @OneToMany(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
	// @JoinColumn(name = "aid", referencedColumnName = "aid")
	// public List<PCAssayResult> getResults() {
	// return results;
	// }
	//
	// public void setResults(List<PCAssayResult> results) {
	// this.results = results;
	// }

	public void setInactiveCidCount(Integer inactiveCidCount) {
		this.inactiveCidCount = inactiveCidCount;
	}

	public void setInactiveSidCount(Integer inactiveSidCount) {
		this.inactiveSidCount = inactiveSidCount;
	}

	public void setInconclusiveCidCount(Integer inconclusiveCidCount) {
		this.inconclusiveCidCount = inconclusiveCidCount;
	}

	public void setInconclusiveSidCount(Integer inconclusiveSidCount) {
		this.inconclusiveSidCount = inconclusiveSidCount;
	}

	public void setLastDataChange(Date lastDataChange) {
		this.lastDataChange = lastDataChange;
	}

	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}

	public void setName(String assayName) {
		this.name = assayName;
	}

	public void setNumberOfCidsWithMicroMolActivity(Integer numberofCidsWithMicroMActivity) {
		this.numberOfCidsWithMicroMolActivity = numberofCidsWithMicroMActivity;
	}

	public void setNumberOfCidsWithNanoMolActivity(Integer numberofCidsWithNanoMActivity) {
		this.numberOfCidsWithNanoMolActivity = numberofCidsWithNanoMActivity;
	}

	public void setNumberOfSidsWithMicroMolActivity(Integer numberofSidsWithMicroMActivity) {
		this.numberOfSidsWithMicroMolActivity = numberofSidsWithMicroMActivity;
	}

	public void setNumberOfSidsWithNanoMolActivity(Integer numberofSidsWithNanoMActivity) {
		this.numberOfSidsWithNanoMolActivity = numberofSidsWithNanoMActivity;
	}

	public void setOnHold(Boolean onHold) {
		this.onHold = onHold;
	}

	public void setPanel(Boolean isPanel) {
		this.isPanel = isPanel;
	}

	public void setPanelDescription(String panelDescription) {
		this.panelDescription = panelDescription;
	}

	public void setPanelName(String panelName) {
		this.panelName = panelName;
	}

	public void setPanels(List<PCAssayPanel> panels) {
		this.panels = panels;
	}

	public void setProbeCidCount(Integer probeCidCount) {
		this.probeCidCount = probeCidCount;
	}

	public void setProbeSidCount(Integer probeSidCount) {
		this.probeSidCount = probeSidCount;
	}

	public void setProjectCategory(String projectCategory) {
		this.projectCategory = projectCategory;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public void setReadoutCount(Integer readoutCount) {
		this.readoutCount = readoutCount;
	}

	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public void setTargetCount(Integer targetCount) {
		this.targetCount = targetCount;
	}

	public void setTotalCidCount(Integer totalCidCount) {
		this.totalCidCount = totalCidCount;
	}

	public void setTotalSidCount(Integer totalSidCount) {
		this.totalSidCount = totalSidCount;
	}

	public void setUnspecifiedCidCount(Integer unspecifiedCidCount) {
		this.unspecifiedCidCount = unspecifiedCidCount;
	}

	public void setUnspecifiedSidCount(Integer unspecifiedSidCount) {
		this.unspecifiedSidCount = unspecifiedSidCount;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public void setVersionChanged(Boolean versionChanged) {
		this.versionChanged = versionChanged;
	}
}