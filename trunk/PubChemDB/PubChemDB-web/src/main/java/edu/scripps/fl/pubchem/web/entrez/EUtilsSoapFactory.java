package edu.scripps.fl.pubchem.web.entrez;

import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.EPostRequest;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.EPostResult;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.ESearchRequest;
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.ESearchResult;

public class EUtilsSoapFactory {
	
	private static EUtilsSoapFactory instance;	
	private static EUtilsServiceStub eUtils;

	public static EUtilsSoapFactory getInstance() {
		if (instance == null) {
			synchronized (EUtilsSoapFactory.class) { // 1
				if (instance == null) {
					synchronized (EUtilsSoapFactory.class) { // 3
						// inst = new Singleton(); //4
						instance = new EUtilsSoapFactory();
					}
					// instance = inst; //5
				}
			}
		}
		return instance;
	}
	
	private EUtilsSoapFactory() {
		try {
			eUtils = new EUtilsServiceStub();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void finalize() throws Throwable {
		if (eUtils != null) {
			eUtils.cleanup();
			eUtils = null;
		}
	}

	public EUtilsServiceStub getService() {
		return eUtils;
	}
	
	public EntrezHistoryKey ePost(String db, String ids) throws Exception {
		EPostRequest request = new EPostRequest();
		request.setDb(db);
		request.setId(ids);
		request.setEmail(EUtilsWebSession.getEmail());
		request.setTool(EUtilsWebSession.getTool());
		EPostResult result = getService().run_ePost(request);
		if (null != result.getERROR())
			throw new Exception(result.getERROR());
		return new EntrezHistoryKey(db, result.getWebEnv(), result.getQueryKey());
	}

	public EntrezHistoryKey eSearch(String db, String searchTerm) throws Exception {
		ESearchRequest eSearch = new ESearchRequest();
		eSearch.setDb(db);
		eSearch.setTerm(searchTerm);
		eSearch.setUsehistory("y");
		eSearch.setRetMax("0");
		eSearch.setEmail(EUtilsWebSession.getEmail());
		eSearch.setTool(EUtilsWebSession.getTool());
		ESearchResult result = getService().run_eSearch(eSearch);
		Integer count = Integer.parseInt(result.getCount());
		if (count < 0)
			throw new Exception(String.format("Unexpected result. eSearch returned %s", count));
		return new EntrezHistoryKey(db, result.getWebEnv(), result.getQueryKey());
	}
}
