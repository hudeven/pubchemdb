package edu.scripps.fl.pubchem.app.assayxml;

import org.apache.commons.pipeline.StageException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import edu.scripps.fl.pipeline.CommitStage;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.db.PCAssay;

public class SaveXRefsStage extends CommitStage {

	public SaveXRefsStage() {
		setCommitFrequency(1);
	}
	
	@Override
	public SessionFactory getSessionFactory() {
		return PubChemDB.getSessionFactory();
	}

	@Override
	public void doSave(Object obj) throws StageException {
		PCAssay assay = (PCAssay) obj;
		PubChemDB.saveXRefs( getSession(), assay);
	}
	
	@Override
	public void innerProcess(Object obj) throws StageException {
		super.innerProcess(obj);
		emit(obj);
	}
}
