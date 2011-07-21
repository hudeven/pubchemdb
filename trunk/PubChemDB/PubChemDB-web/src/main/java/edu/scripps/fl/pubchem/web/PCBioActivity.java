package edu.scripps.fl.pubchem.web;

public class PCBioActivity {
	private Long AID;
	private Integer probeCount;
	private Integer activeCount;
	private Integer inactiveCount;
	private Integer testedCount;
	private Integer activesLessThan1uM;
	private Integer activesLessThan1nM;
	private Double activityConcentrationMin;
	private Double activityConcentrationMax;
	private String bioAssayName;
	private String proteinTargetName;
	
	public Long getAID() {
		return AID;
	}
	public void setAID(Long aID) {
		AID = aID;
	}
	public Integer getProbeCount() {
		return probeCount;
	}
	public void setProbeCount(Integer probeCount) {
		this.probeCount = probeCount;
	}
	public Integer getActiveCount() {
		return activeCount;
	}
	public void setActiveCount(Integer activeCount) {
		this.activeCount = activeCount;
	}
	public Integer getInactiveCount() {
		return inactiveCount;
	}
	public void setInactiveCount(Integer inactiveCount) {
		this.inactiveCount = inactiveCount;
	}
	public Integer getTestedCount() {
		return testedCount;
	}
	public void setTestedCount(Integer testedCount) {
		this.testedCount = testedCount;
	}
	public Integer getActivesLessThan1uM() {
		return activesLessThan1uM;
	}
	public void setActivesLessThan1uM(Integer activesLessThan1uM) {
		this.activesLessThan1uM = activesLessThan1uM;
	}
	public Integer getActivesLessThan1nM() {
		return activesLessThan1nM;
	}
	public void setActivesLessThan1nM(Integer activesLessThan1nM) {
		this.activesLessThan1nM = activesLessThan1nM;
	}
	public Double getActivityConcentrationMin() {
		return activityConcentrationMin;
	}
	public void setActivityConcentrationMin(Double activityConcentrationMin) {
		this.activityConcentrationMin = activityConcentrationMin;
	}
	public Double getActivityConcentrationMax() {
		return activityConcentrationMax;
	}
	public void setActivityConcentrationMax(Double activityConcentrationMax) {
		this.activityConcentrationMax = activityConcentrationMax;
	}
	public String getBioAssayName() {
		return bioAssayName;
	}
	public void setBioAssayName(String bioAssayName) {
		this.bioAssayName = bioAssayName;
	}
	public String getProteinTargetName() {
		return proteinTargetName;
	}
	public void setProteinTargetName(String proteinTargetName) {
		this.proteinTargetName = proteinTargetName;
	}	
}