package edu.scripps.fl.pubchem.app.results;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pipeline.StageContext;
import org.apache.commons.pipeline.StageException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;

import edu.scripps.fl.pipeline.SessionStage;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.PubChemFactory;
import edu.scripps.fl.pubchem.db.PCAssay;
import edu.scripps.fl.pubchem.db.PCAssayResult;

public class CsvReaderStage extends SessionStage {
	
	private static final Logger log = LoggerFactory.getLogger(CsvReaderStage.class);
	
	private Map<Integer, AIDTracker> trackers = new ConcurrentHashMap();
	private SourceColumnsInfo.Type type;
	private StageContext context;
	
	@Override
	public void init(StageContext context) {
		super.init(context);
		this.context = context;
	}
	
	@Override
	public SessionFactory getSessionFactory() {
		return PubChemDB.getSessionFactory();
	}
	
	@Override
	public void innerPreprocess() throws StageException {
		type = (SourceColumnsInfo.Type) this.context.getEnv("CsvResultsSource");		
	}
	
	@Override
	public void innerProcess(Object obj) throws StageException {
		try{
			Object[] objs = (Object[]) obj;
			Integer aid = (Integer) objs[0];
			InputStream is = (InputStream) objs[1];
			processDataFromResource(aid, is);
		}
		catch(Exception ex) {
			throw new StageException(this, ex);
		}		
	}
	
	protected void processDataFromResource(Integer aid, InputStream is) throws Exception {
		
		CsvReader reader = new CsvReader(new BufferedReader( new InputStreamReader(is)), ',');
		reader.readHeaders();
		String[] headers = reader.getHeaders();
			
		AIDTracker tracker = new AIDTracker(aid);
		trackers.put(aid, tracker);
			
		Session session = getSession();
		session.clear();
		
		// Get assay with all columns in just one query
		PCAssay assay = PubChemFactory.getInstance().getAssay(session, aid);
			
		SourceColumnsInfo colInfo = SourceColumnsInfo.newInstance(type, assay, headers);
		
		int counter = 0;
		boolean moreRecords = reader.readRecord();
		while(moreRecords) {			
			counter++;
			String[] values = reader.getValues();
			moreRecords = reader.readRecord();
			if( ! moreRecords ) {
				tracker.numRecords = counter;
			}
			for(long SID: colInfo.getSIDs(values)) {
				PCAssayResult result = colInfo.createAssayResult(SID, values);
				emit(new Object[]{aid, trackers, result});	
			}
		}
	}
}