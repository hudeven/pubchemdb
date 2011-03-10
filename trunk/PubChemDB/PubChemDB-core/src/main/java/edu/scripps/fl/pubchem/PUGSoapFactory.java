package edu.scripps.fl.pubchem;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gov.nih.nlm.ncbi.pubchem.PUGStub;
import gov.nih.nlm.ncbi.pubchem.PUGStub.AnyKeyType;
import gov.nih.nlm.ncbi.pubchem.PUGStub.ArrayOfInt;
import gov.nih.nlm.ncbi.pubchem.PUGStub.AssayColumnsType;
import gov.nih.nlm.ncbi.pubchem.PUGStub.AssayDownload;
import gov.nih.nlm.ncbi.pubchem.PUGStub.AssayFormatType;
import gov.nih.nlm.ncbi.pubchem.PUGStub.CompressType;
import gov.nih.nlm.ncbi.pubchem.PUGStub.Download;
import gov.nih.nlm.ncbi.pubchem.PUGStub.EntrezKey;
import gov.nih.nlm.ncbi.pubchem.PUGStub.FormatType;
import gov.nih.nlm.ncbi.pubchem.PUGStub.GetDownloadUrl;
import gov.nih.nlm.ncbi.pubchem.PUGStub.GetOperationStatus;
import gov.nih.nlm.ncbi.pubchem.PUGStub.GetStatusMessage;
import gov.nih.nlm.ncbi.pubchem.PUGStub.InputAssay;
import gov.nih.nlm.ncbi.pubchem.PUGStub.InputEntrez;
import gov.nih.nlm.ncbi.pubchem.PUGStub.InputList;
import gov.nih.nlm.ncbi.pubchem.PUGStub.PCIDType;
import gov.nih.nlm.ncbi.pubchem.PUGStub.StatusType;

public class PUGSoapFactory {

	private static final Logger log = LoggerFactory.getLogger(PUGSoapFactory.class);

	private static PUGSoapFactory instance;

	public static PUGSoapFactory getInstance() {
		if (instance == null) {
			synchronized (PUGSoapFactory.class) { // 1
				if (instance == null) {
					synchronized (PUGSoapFactory.class) { // 3
						// inst = new Singleton(); //4
						instance = new PUGSoapFactory();
					}
					// instance = inst; //5
				}
			}
		}
		return instance;
	}

	private PUGStub pug;

	private int initSleepTime = 2000;
	private int sleepTime = 6000;

	private PUGSoapFactory() {
		try {
			pug = new PUGStub();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	protected AnyKeyType downloadAssay(String assayKey, CompressType compressType) throws RemoteException {
		AssayDownload download = new AssayDownload();
		download.setAssayKey(assayKey);
		download.setAssayFormat(AssayFormatType.eAssayFormat_CSV);
		download.setECompress(compressType);
		String downloadKey = getPUG().AssayDownload(download).getDownloadKey();
		AnyKeyType anyKey = new AnyKeyType();
		anyKey.setAnyKey(downloadKey);
		return anyKey;
	}

	@Override
	public void finalize() throws Throwable {
		PUGStub pug = getPUG();
		if (pug != null) {
			pug.cleanup();
			pug = null;
		}
	}

	protected InputStream getAssayDownload(AnyKeyType anyKey, StatusType status) throws Exception {
		if (status == StatusType.eStatus_Success) {
			GetDownloadUrl getURL = new GetDownloadUrl();
			getURL.setDownloadKey(anyKey.getAnyKey());
			URL url = new URL(getPUG().GetDownloadUrl(getURL).getUrl());
			log.debug("Success! Download URL = " + url.toString());
			return url.openStream();
		} else {
			GetStatusMessage message = new GetStatusMessage();
			message.setGetStatusMessage(anyKey);
			String errorMessage = "Error while downloading the assay: "
					+ getPUG().GetStatusMessage(message).getMessage();
			log.error(errorMessage);
			throw new Exception(errorMessage);
		}
	}

	protected String getAssayKey(int aid, String listKey) throws RemoteException {
		InputAssay assay = new InputAssay();
		assay.setAID(aid);
		assay.setColumns(AssayColumnsType.eAssayColumns_Complete);
		assay.setListKeySCIDs(listKey);
		String assayKey = getPUG().InputAssay(assay).getAssayKey();
		return assayKey;
	}

	public InputStream getAssayResults(int aid, EntrezHistoryKey key) throws Exception {
		String listKey = getListKey(key.getDatabase(), key.getWebEnv(), key.getQueryKey());
		return getAssayResults(aid, listKey);
	}

	public InputStream getAssayResults(int aid, String database, String searchTerm) throws Exception {
		EntrezHistoryKey key = EUtilsFactory.getInstance().eSearch(database, searchTerm);
		String listKey = getListKey(key);
		return getAssayResults(aid, listKey);
	}

	public InputStream getAssayResults(int aid, String listKey) throws Exception {
		String assayKey = getAssayKey(aid, listKey);
		AnyKeyType anyKey = downloadAssay(assayKey, CompressType.eCompress_GZip);
		StatusType status = getOnCompleteStatus(anyKey);
		InputStream is = getAssayDownload(anyKey, status);
		return new GZIPInputStream(is);

	}

	public String getListKey(String database, String searchTerm) throws Exception {
		EntrezHistoryKey entrezKey = EUtilsFactory.getInstance().eSearch(database, searchTerm);
		String key = getListKey(entrezKey);
		return key;
	}

	public String getListKey(EntrezHistoryKey key) throws RemoteException {
		return getListKey(key.getDatabase(), key.getWebEnv(), key.getQueryKey());
	}

	public String getListKey(String db, String webEnv, String queryKey) throws RemoteException {
		EntrezKey entrezKey = new EntrezKey();
		entrezKey.setDb(db);
		entrezKey.setWebenv(webEnv);
		entrezKey.setKey(queryKey);
		InputEntrez entrez = new InputEntrez();
		entrez.setEntrezKey(entrezKey);
		String listKey = getPUG().InputEntrez(entrez).getListKey();
		return listKey;
	}

	protected StatusType getOnCompleteStatus(AnyKeyType anyKey) throws Exception {
		return getOnCompleteStatus(anyKey, this.initSleepTime, this.sleepTime);
	}

	protected StatusType getOnCompleteStatus(AnyKeyType anyKey, long initSleepTime, long sleepTime) throws Exception {
		GetOperationStatus getStatus = new GetOperationStatus();
		getStatus.setGetOperationStatus(anyKey);
		StatusType status;
		int counter = 0;
		while ((status = getPUG().GetOperationStatus(getStatus).getStatus()) == StatusType.eStatus_Running
				|| status == StatusType.eStatus_Queued) {
			log.debug("Waiting for operation to finish...");
			if (counter < 2)
				Thread.sleep(initSleepTime);
			else
				Thread.sleep(sleepTime);
		}
		return status;
	}

	private PUGStub getPUG() {
		return pug;
	}
	
	public URL getSDFile(PCIDType type, int[] ids) throws IOException, InterruptedException {
		ArrayOfInt arr = new ArrayOfInt();
		arr.set_int(ids);
		InputList list = new InputList();
		list.setIds(arr);
		list.setIdType(type);
		String listKey = getPUG().InputList(list).getListKey();

		Download download = new Download();
		download.setListKey(listKey);
		download.setEFormat(FormatType.eFormat_SDF);
		download.setECompress(CompressType.eCompress_GZip);
		String downloadKey = getPUG().Download(download).getDownloadKey();

		GetOperationStatus statusRequest = new GetOperationStatus();
		AnyKeyType anyKey = new AnyKeyType();
		anyKey.setAnyKey(downloadKey);
		statusRequest.setGetOperationStatus(anyKey);
		StatusType status;
		long timeStart = System.currentTimeMillis();
		while ((status = getPUG().GetOperationStatus(statusRequest).getStatus()) == StatusType.eStatus_Running
				|| status == StatusType.eStatus_Queued) {
			Thread.sleep(10000);
			long timeNow = System.currentTimeMillis();
			System.out.println(String.format("Waiting on pug operation. Total duration = %ss",
					(timeNow - timeStart) / 1000));
		}

		if (status == StatusType.eStatus_Success) {
			GetDownloadUrl downloadUrl = new GetDownloadUrl();
			downloadUrl.setDownloadKey(downloadKey);
			URL url = new URL(getPUG().GetDownloadUrl(downloadUrl).getUrl());
			return url;
		} else {
			GetStatusMessage errorStatusRequest = new GetStatusMessage();
			errorStatusRequest.setGetStatusMessage(anyKey);
			log.error("Error: " + getPUG().GetStatusMessage(errorStatusRequest).getMessage());
			throw new RuntimeException(getPUG().GetStatusMessage(errorStatusRequest).getMessage());
		}
	}
	
	public URL getSDFilefromCIDs(int[] cids) throws IOException, InterruptedException {
		return getSDFile(PCIDType.eID_CID, cids);
	}

	public URL getSDFilefromSIDs(int[] sids) throws IOException, InterruptedException {
		return getSDFile(PCIDType.eID_SID, sids);
	}
}