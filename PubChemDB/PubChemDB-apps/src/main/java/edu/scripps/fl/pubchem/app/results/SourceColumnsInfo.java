package edu.scripps.fl.pubchem.app.results;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;

import edu.scripps.fl.pubchem.db.PCAssay;
import edu.scripps.fl.pubchem.db.PCAssayColumn;
import edu.scripps.fl.pubchem.db.PCAssayResult;

public class SourceColumnsInfo {
	
	public enum Type {
		FTP, PUG;
	}
	
	private Type type;
	private Map<String,Integer> indexes;
	private int SID, CID, COMMENT, OUTCOME, SCORE, URL, REGID;
	private PCAssay assay;
	private PCAssayColumn activeColumn;
	private List<PCAssayColumn> testedCols;
	
	public static SourceColumnsInfo newInstance(Type type, PCAssay assay, String[] headers) {
		SourceColumnsInfo info = new SourceColumnsInfo(type, assay, headers);
		return info;
	}
	
	// Creates a map with the header's name as key and array index as value
	protected Map<String,Integer> getIndexes(String[] headers){
		Map<String,Integer> indexes = new HashMap<String,Integer>();
		for(int ii = 0; ii < headers.length; ii++)
			indexes.put(headers[ii], ii);		
		return indexes;
	}
	
	protected SourceColumnsInfo(Type type, PCAssay assay, String[] headers) {
		this.activeColumn = assay.getActiveColumn();
		this.testedCols = assay.getTestedColumns();
		this.indexes = getIndexes(headers);
		this.type = type;
		this.assay = assay;
		if( type.equals(Type.FTP)) {
			SID = indexes.get("PUBCHEM_SID");
			CID = indexes.get("PUBCHEM_CID");
			COMMENT = indexes.get("PUBCHEM_ASSAYDATA_COMMENT");
			OUTCOME = indexes.get("PUBCHEM_ACTIVITY_OUTCOME");
			SCORE = indexes.get("PUBCHEM_ACTIVITY_SCORE");
			URL = indexes.get("PUBCHEM_ACTIVITY_URL");
			REGID = indexes.get("PUBCHEM_EXT_DATASOURCE_REGID");
		}
		else {
			SID = indexes.get("SID");
			CID = indexes.get("CID");
			COMMENT = indexes.get("Comment");
			OUTCOME = indexes.get("Outcome");
			SCORE = indexes.get("RankScore");
			URL = indexes.get("URL");
			REGID = -1;
		}
	}
	
	public long[] getSIDs(String[] row) {
		String[] sids = row[SID].split("\\s*,\\s*");
		return (long[]) ConvertUtils.convert(sids, long.class);
	}
	
	protected String getCID(String[] row) {
		return row[CID];
	}
	
	protected String getComment(String[] row) {
		return row[COMMENT];
	}

	protected String getOutcome(String[] row) {
		String outcome = row[OUTCOME];
		return outcome;
	}
	
	protected String getScore(String[] row) {
		return row[SCORE];
	}
	
	protected String getURL(String[] row) {
		return row[URL];
	}
	
	protected String getExtRegId(String[] row) {
		return REGID > 0 ? row[REGID] : "";
	}	
	
	private String getColumnValue(int tid, String[] row) {
		int idx;
		if( this.type.equals(Type.FTP))
			idx = tid + 8; // 8 columns before TIDs start
		else
			idx = tid + 10;
		return row[idx-1];
	}
	
	protected String getPubChemOutcome(int score) {
		String sScore = null;
		if (1 == score)
			sScore = "Inactive";
		else if (2 == score)
			sScore = "Active";
		else if (3 == score)
			sScore = "Inconclusive";
		else if (4 == score)
			sScore = "Unspecified";
		else if (5 == score)
			sScore = "Probe";
		else
			sScore = "" + score;
		return sScore;
	}
	
	public PCAssayResult createAssayResult(long SID, String[] values) throws Exception {
		PCAssayResult result = new PCAssayResult();
		result.setAssay(assay);

		result.setSID(SID);

		BeanUtils.setProperty(result, "CID", getCID(values));
		BeanUtils.setProperty(result, "Comments", getComment(values));
		
		Object outcome = this.type.equals(Type.PUG) ? getOutcome(values) : getPubChemOutcome(Integer.parseInt(getOutcome(values)));
		BeanUtils.setProperty(result, "Outcome", outcome);
		BeanUtils.setProperty(result, "Score", getScore(values));
		BeanUtils.setProperty(result, "URL", getURL(values));
		BeanUtils.setProperty(result, "Xref", getExtRegId(values));

		// if a dose response assay with a marked activeConcentration
		if ("confirmatory".equals(assay.getActivityOutcomeMethod()) && activeColumn != null) {
			String actConc = getColumnValue(activeColumn.getTID(), values);
			if (!"".equals(actConc)) {
				result.setPrimaryValue(Double.valueOf(actConc));
				result.setPrimaryColumn(activeColumn);

				PCAssayColumn qualCol = assay.getQualifierColumn();
				if (qualCol != null) {
					String qual = getColumnValue(qualCol.getTID(), values); // get(String)
					if (!"".equals(qual))
						result.setQualifier(qual);
				}
			}

		} else if ("screening".equals(assay.getActivityOutcomeMethod()) && testedCols.size() > 0) {
			PCAssayColumn testedCol = testedCols.get(0);
			String value = getColumnValue(testedCol.getTID(), values);
			result.setPrimaryColumn(testedCol);
			if (!"".equals(value))
				if ("float".equals(testedCol.getType()) || "int".equals(testedCol.getType()))
					result.setPrimaryValue(Double.parseDouble(value));
				else
					result.setPrimaryValueAsString(value);
		}
		// put all testedConcentration columns into an ordered array. Interested in numbers here only.
		result.getTestedValues().clear();
		for (int ii = 0; ii < testedCols.size(); ii++) {
			PCAssayColumn testedCol = testedCols.get(ii);
			String value = getColumnValue(testedCol.getTID(), values);
			if (null != value && !"".equals(value)) {
				try {
					Double dbl = Double.parseDouble(value);
					result.getTestedValues().set(ii, dbl);
				} catch (NumberFormatException ex) {
					// if not a number then don't worry about it.
				}
			}
		}

		List<String> list = result.getAllValues();
		list.clear();
		for (PCAssayColumn col : assay.getColumns()) {
			if (col.getTID() > 0) { // not the outcome and score
				list.set(col.getTID() - 1, getColumnValue(col.getTID(), values));
			}
		}

		return result;
	}
}