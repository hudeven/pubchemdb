package edu.scripps.fl.pubchem.app.results;

import java.io.InputStream;

import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.stage.BaseStage;

import edu.scripps.fl.pubchem.PUGSoapFactory;

public class PugCsvDownloadStage extends BaseStage {
	
	@Override
	public void process(Object obj) throws StageException {
		Integer aid = (Integer) obj;
		String listKey = (String) this.context.getEnv("ListKey");
		try{
			InputStream is = PUGSoapFactory.getInstance().getAssayResults(aid, listKey);
			emit(new Object[]{aid, is});
		}
		catch(Exception ex) {
			throw new StageException(this, ex);
		}
	}
}
