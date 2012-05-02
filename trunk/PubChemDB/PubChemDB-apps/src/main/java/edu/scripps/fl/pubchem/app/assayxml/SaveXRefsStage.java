package edu.scripps.fl.pubchem.app.assayxml;

import org.apache.commons.pipeline.StageException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import edu.scripps.fl.pipeline.CommitStage;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.db.PCAssay;
import edu.scripps.fl.util.ProgressWriter;

public class SaveXRefsStage extends CommitStage {

	ProgressWriter pw = new ProgressWriter("SaveXRefs");
	
	public SaveXRefsStage() {
		setCommitFrequency(1);
	}
	
	@Override
	public SessionFactory getSessionFactory() {
		return PubChemDB.getSessionFactory();
	}

	@Override
	public void doSave(Object obj) throws StageException {
		pw.increment();
		Session session = getSession();
		PCAssay assay = (PCAssay) obj;
		PubChemDB.saveXRefs( session, assay);
	}
	
	@Override
	public void process(Object obj) throws StageException {
		super.process(obj);
		emit(obj);
	}
}
