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
package edu.scripps.fl.pubchem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.VFS;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;

import edu.scripps.fl.pubchem.db.PCAssay;
import edu.scripps.fl.pubchem.db.PCAssayColumn;
import edu.scripps.fl.pubchem.db.PCAssayResult;
import edu.scripps.fl.pubchem.db.PCAssayXRef;
import edu.scripps.fl.pubchem.db.Relation;
import edu.scripps.fl.pubchem.db.XRef;
import edu.scripps.fl.pubchem.web.entrez.EUtilsWebSession;

public class PubChemFactory {

	private static final Logger log = LoggerFactory.getLogger(PubChemFactory.class);

	private static final String ftpUser = "anonymous";
	private static final String ftpPass = "scripps.edu";
	private static final String ftpHost = "ftp.ncbi.nlm.nih.gov";
	private static final String pubchemBioAssayUrlFormat = "ftp://%s:%s@ftp.ncbi.nlm.nih.gov/pubchem/Bioassay/CSV";

	private static PubChemFactory instance;
	private Properties propMap = new Properties();

	private PubChemFactory() {
		try {
			propMap.load(getClass().getClassLoader().getResourceAsStream(
					"edu/scripps/fl/pubchem/eSummaryXMLtoBeanMap.properties"));
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	public static PubChemFactory getInstance() {
		if (instance == null) {
			synchronized (PubChemFactory.class) { // 1
				if (instance == null) {
					synchronized (PubChemFactory.class) { // 3
						// inst = new Singleton(); //4
						instance = new PubChemFactory();
					}
					// instance = inst; //5
				}
			}
		}
		return instance;
	}

	public TreeSet<Long> getAIDs(String filter) throws Exception {
		return (TreeSet<Long>) EUtilsWebSession.getInstance().getIds(filter, "pcassay", new TreeSet<Long>(), 75000);
	}

	public TreeSet<Long> getAIDs() throws Exception {
		return (TreeSet<Long>) getAIDs("\"all\"[filter]");
	}

	public TreeSet<Long> getMolecularLibrariesOrBeforeAIDs() throws Exception {
		return (TreeSet<Long>) getAIDs("\"NIH Molecular Libraries Program\"[SourceCategory] OR \"DTP/NCI\"[SourceName]");
	}

	public TreeSet<Long> getAIDs(int daysInPast) throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1 * Math.abs(daysInPast));
		return getAIDs(cal.getTime());
	}

	public TreeSet<Long> getAIDs(Date from) throws Exception {
		return getAIDs(from, new Date());
	}

	public TreeSet<Long> getAIDs(Date from, Date to) throws Exception {
		String query = String
				.format(
						"(%1$tY/%1$tm/%1$td[DepositDate] : %2$tY/%2$tm/%2$td[DepositDate]) OR (%1$tY/%1$tm/%1$td[ModifyDate] : %2$tY/%2$tm/%2$td[ModifyDate]) OR (%1$tY/%1$tm/%1$td[HoldUntilDate] : %2$tY/%2$tm/%2$td[HoldUntilDate])",
						from, to);
		return (TreeSet<Long>) getAIDs(query);
	}

	public static String getAIDArchive(long aid) {
		long low = nextLowestMultiple(aid, 1000);
		return String.format("%07d_%07d", low + 1, low + 1000);
	}

	// http://mindprod.com/jgloss/round.html
	// rounding m down to multiple of n
	protected static long nextLowestMultiple(long m, long n) {
		long floor = (m-1) / n * n;
		return floor;
	}

	public InputStream getXmlDescr(URL parent, long aid) throws IOException {
		String archive = getAIDArchive(aid);
		String sUrl = String.format("%s/%s/%s.zip", parent, "Description", archive);
		String sZip = String.format("zip:%s!%s/%s.descr.xml.gz", sUrl, archive, aid);
		// log.debug(sZip);
		FileObject fo = VFS.getManager().resolveFile(sZip);
		// log.debug("Resolved file: " + sZip);
		InputStream is = fo.getContent().getInputStream();
		return new GZIPInputStream(is);
	}

	public InputStream getXmlDescr(long aid) throws IOException {
		String sUrl = String.format(pubchemBioAssayUrlFormat, ftpUser, ftpPass);
		return getXmlDescr(new URL(sUrl), aid);
	}

	public InputStream getCsv(URL parent, long aid) throws IOException {
		String archive = getAIDArchive(aid);
		String sUrl = String.format("%s/%s/%s.zip", parent, "Data", archive);
		String sZip = String.format("zip:%s!/%s/%s.csv.gz", sUrl, archive, aid);
		// log.debug(sZip);
		FileObject fo = VFS.getManager().resolveFile(sZip);
		// log.debug("Resolved file: " + sZip);
		InputStream is = fo.getContent().getInputStream();
		return new GZIPInputStream(is);
	}

	public InputStream getCsv(long aid) throws IOException {
		String sUrl = String.format(pubchemBioAssayUrlFormat, ftpUser, ftpPass);
		return getCsv(new URL(sUrl), aid);
	}

	public void populateAssayFromSummary(PCAssay assay) throws Exception {
		Document document = EUtilsWebSession.getInstance().getSummary(assay.getAID(), "pcassay");
		populateAssayFromSummaryDocument(assay, document
				.selectSingleNode("eSummaryResult/DocumentSummarySet/DocumentSummary"));
	}

	// eutils version 2.0
	public int populateAssaysFromSummaryDocument(Session session, Node document) throws Exception {
		List<Node> list = document.selectNodes("/eSummaryResult/DocumentSummarySet", ".");
		for (Node node : list) {
			Integer id = Integer.parseInt(node.selectSingleNode("AID").getText());
			PCAssay assay = getAssay(session, id);
			populateAssayFromSummaryDocument(assay, node);
			session.save(assay);
		}
		return list.size();
	}

	public PCAssay getAssay(Session session, int aid) {
		Query query = session.createQuery("from PCAssay where assay_aid = " + aid);
		PCAssay assay = (PCAssay) query.uniqueResult();
		if (assay == null) {
			assay = new PCAssay();
			assay.setVersion(0);
			assay.setRevision(0);
			assay.setName("");
			assay.setExtRegId("");
			session.save(assay);
		}
		return assay;
	}

	public XRef getXRef(Session session, String xRefId, String database) {
		Criteria criteria = session.createCriteria(XRef.class);
		criteria.add(Restrictions.eq("database", database));
		criteria.add(Restrictions.eq("XRefId", xRefId));
		XRef xref = (XRef) criteria.uniqueResult();
		if (xref == null) {
			xref = new XRef();
			xref.setXRefId(xRefId);
			xref.setDatabase(database);
			session.save(xref);
		}
		return xref;
	}

	public PCAssay populateAssayFromSummaryDocument(Session session, Node docSumNode) throws Exception {
		// Node idNode = docSumNode.selectSingleNode("AID");
		// Integer aid = Integer.parseInt(idNode.getText());
		String uid = docSumNode.valueOf("@uid");
		PCAssay assay = getAssay(session, Integer.parseInt(uid));
		populateAssayFromSummaryDocument(assay, docSumNode);
		for (PCAssayXRef assayXRef : assay.getAssayXRefs()) {
			session.saveOrUpdate(assayXRef);
		}
		session.save(assay);
		return assay;
	}

	private static Map<String, String> unprocessedProperties = new java.util.concurrent.ConcurrentHashMap<String, String>();

	public List<Long> getSummaryAID(Integer aid) throws Exception {
		return EUtilsWebSession.getInstance().getIds(aid + "[XRefAid] AND summary[activityoutcomemethod]", "pcassay");
	}

	public void populateAssayFromSummaryDocument(PCAssay assay, Node docSumNode) throws Exception {
		Node errorNode = docSumNode.selectSingleNode("error");
		if (errorNode != null)
			throw new Exception("Entrez error: " + errorNode.getText());

		List<Node> list = docSumNode.selectNodes("*");
		String uid = docSumNode.valueOf("@uid");
		assay.setAID(Integer.parseInt(uid));
		for (Node node : list) {
			String name = node.getName();
			Object value = node.getText();
			if (node.selectNodes("*").size() > 0) {

			} else {
				String property = propMap.getProperty(name);
				if (null != property) {
					Class clazz = PropertyUtils.getPropertyType(assay, property);
					if (clazz.isAssignableFrom(Date.class))
						value = parseDate(value);
					BeanUtils.setProperty(assay, property, value);
				} else {
					if (!unprocessedProperties.containsKey(name)) {
						unprocessedProperties.put(name, "");
						log.warn(String.format("Cannot determine PCAssay bean property '%s'", name));
					}
				}
			}
		}
		String desc = assay.getDescription();
		// eutils summary description doesn't contain new lines 
		// so don't update it if it already has a value (when we populate via xml first).
		if ( desc == null || "".equals(desc) || ! desc.contains("\n")) {
			Node node = docSumNode.selectSingleNode("AssayDescription");
			assay.setDescription(node.getText());
		}
		return;
	}

	public Date parseDate(Object value) {
		if (value == null)
			return null;
		Integer[] iParts = null;
		if (value instanceof Number) {
			if (((Number) value).intValue() == 0)
				return null;
			List<String> parts = new ArrayList();
			String valueStr = value.toString();
			parts.add(valueStr.substring(0, 4));
			for (int ii = 4; ii < valueStr.length(); ii += 2) {
				String substr = valueStr.substring(ii, ii + 2);
				parts.add(substr);
			}
			iParts = (Integer[]) ConvertUtils.convert(parts, Integer[].class);
		} else {
			iParts = (Integer[]) ConvertUtils.convert(value.toString().split("\\D+"), Integer[].class);
		}
		Calendar cal = Calendar.getInstance();
		if (iParts.length > 0)
			cal.set(Calendar.YEAR, iParts[0]);
		if (iParts.length > 1)
			cal.set(Calendar.MONTH, iParts[1] - 1); // months are zero based.
		if (iParts.length > 2)
			cal.set(Calendar.DAY_OF_MONTH, iParts[2]);
		if (iParts.length > 3)
			cal.set(Calendar.HOUR_OF_DAY, iParts[3]);
		if (iParts.length > 4)
			cal.set(Calendar.MINUTE, iParts[4]);
		if (iParts.length > 5)
			cal.set(Calendar.SECOND, iParts[5]);
		// log.debug(String.format("parsed date %s to %s", value,
		// cal.getTime()));
		return cal.getTime();
	}

	// public void populateAssayFromPug(PCAssay assay) throws Exception {
	// GetAssayDescription gad = new GetAssayDescription();
	// gad.setAID(assay.getAID().intValue());
	// gad.setGetVersion(true);
	// GetAssayDescriptionResponse response = pug.GetAssayDescription(gad);
	// AssayDescriptionType adt = response.getAssayDescription();
	//
	// assay.setVersion(new
	// Double(response.getAssayDescription().getVersion()));
	// assay.setLastDataChange(parseDate(adt.getLastDataChange()));
	//
	// // if (!assay.isNewVersion()) {
	// // log.debug("AID " + assay.getAID() + " verison unchanged");
	// // return assay;
	// // }
	// assay.setHasScore(adt.getHasScore());
	// if (adt.getProtocol() != null)
	// assay.setProtocol(StringUtils.join(adt.getProtocol().getString(),
	// "\r\n"));
	//
	// List<Target> targets = new ArrayList();
	// Iterator<Integer> iter =
	// JXPathContext.newContext(response).iterate("//assayDescription/targets/target/gi");
	// while (iter.hasNext()) {
	// Integer gid = iter.next();
	// Target target = (Target) HibernateUtil.getSession().get(Target.class,
	// gid.intValue());
	// if (target == null) {
	// target = new Target();
	// target.setGID(gid.intValue());
	// HibernateUtil.getSession().save(target);
	// HibernateUtil.getSession().flush();
	// }
	//			
	// SequenceFactory.getInstance().populateTarget(target);
	// target = (Target) HibernateUtil.getSession().merge(target);
	// targets.add(target);
	// }
	// assay.setTargets(targets);
	// }

	// public void populateAssayColumns(PCAssay assay) throws Exception {
	// PUGStub.GetAssayColumnDescriptions request = new
	// PUGStub.GetAssayColumnDescriptions();
	// request.setAID(assay.getAID());
	// PUGStub.GetAssayColumnDescriptionsResponse response =
	// pug.GetAssayColumnDescriptions(request);
	// Map<PCAssayColumn, PCAssayColumn> map = new HashMap();
	// for(PCAssayColumn col: assay.getColumns())
	// map.put(col,col);
	// for (ColumnDescriptionType desc : response.getColumnDescription()) {
	// PCAssayColumn col = new PCAssayColumn();
	// col.setAssay(assay);
	// if (desc.getTID() == Integer.MIN_VALUE) {
	// if (desc.getHeading().getValue().equalsIgnoreCase("Outcome"))
	// col.setTID(-1);
	// else if (desc.getHeading().getValue().equalsIgnoreCase("Score"))
	// col.setTID(0);
	// }
	// else
	// col.setTID(desc.getTID());
	//			
	// if( map.containsKey(col) )
	// col = map.get(col);
	// else
	// assay.getColumns().add(col);
	//			
	// col.setActiveConcentration(desc.getActiveConcentration());
	// col.setName(desc.getName());
	// String description[] = (desc.getDescription() == null) ? new String[0] :
	// desc.getDescription().getString();
	// col.setDescription(StringUtils.join(description, "\r\n"));
	// col.setHeading(desc.getHeading().getValue());
	// col.setType(desc.getType());
	// col.setUnit(desc.getUnit());
	// if (desc.getTestedConcentration() != null) {
	// col.setTestedConcentration(desc.getTestedConcentration().getConcentration());
	// col.setTestedConcentrationUnit(desc.getTestedConcentration().getUnit());
	// }
	// }
	// }

	public void populateAssayResults(Session session, PCAssay assay, InputStream is) throws Exception {
		long counter = 0L;
		PCAssayColumn activeColumn = assay.getActiveColumn();
		List<PCAssayColumn> testedCols = assay.getTestedColumns();

		CsvReader reader = new CsvReader(new BufferedReader(new InputStreamReader(is)), ',');
		reader.readHeaders();
		String[] headers = reader.getHeaders();

		Transaction trx = session.beginTransaction();
		while (reader.readRecord()) {

			String[] values = reader.getValues();

			String[] sids = reader.get("PUBCHEM_SID").split("\\s*,\\s*");

			if (sids.length > 1) {
				log.warn(String.format("AID %s has %s SIDS on a line", assay.getAID(), sids.length));
			}

			for (String sid : sids) {

				PCAssayResult result = new PCAssayResult();

				result.setSID(Long.parseLong(sid));

				String cid = reader.get("PUBCHEM_CID");
				if (!"".equals(cid))
					result.setCID(Long.parseLong(cid));

				result.setComments(reader.get("PUBCHEM_ASSAYDATA_COMMENT"));

				int outcome = Integer.parseInt(reader.get("PUBCHEM_ACTIVITY_OUTCOME"));
				result.setOutcome(getOutcome(outcome));

				String score = reader.get("PUBCHEM_ACTIVITY_SCORE");
				if (!"".equals(score))
					result.setRankScore(Integer.parseInt(score));

				result.setURL(reader.get("PUBCHEM_ACTIVITY_URL"));

				result.setXref(reader.get("PUBCHEM_EXT_DATASOURCE_REGID"));

				// if a dose response assay with a marked activeConcentration
				if ("confirmatory".equals(assay.getActivityOutcomeMethod()) && activeColumn != null) {
					String actConc = reader.get("" + activeColumn.getTID());
					if (!"".equals(actConc)) {
						result.setPrimaryValue(Double.valueOf(actConc));
						result.setPrimaryColumn(activeColumn);

						PCAssayColumn qualCol = assay.getQualifierColumn();
						if (qualCol != null) {
							String qual = reader.get("" + qualCol.getTID()); // get(String)
							if (!"".equals(qual))
								result.setQualifier(qual);
						}
					}

				} else if ("screening".equals(assay.getActivityOutcomeMethod()) && testedCols.size() > 0) {
					PCAssayColumn testedCol = testedCols.get(0);
					String value = reader.get("" + testedCol.getTID());
					result.setPrimaryColumn(testedCol);
					if (!"".equals(value))
						if ("float".equals(testedCol.getType()) || "int".equals(testedCol.getType()))
							result.setPrimaryValue(Double.parseDouble(value));
						else
							result.setPrimaryValueAsString(value);
				}
				// put all testedConcentration columns into an ordered array.
				// Interested in numbers here only.
				result.getTestedValues().clear();
				for (int ii = 0; ii < testedCols.size(); ii++) {
					String value = reader.get("" + testedCols.get(ii).getTID());
					try {
						Double dbl = Double.parseDouble(value);
						result.getTestedValues().set(ii, dbl);
					} catch (NumberFormatException ex) {
						// if not a number then don't worry about it.
					}
				}

				List<String> list = result.getAllValues();
				list.clear();
				for (PCAssayColumn col : assay.getColumns()) {
					if (col.getTID() > 0) { // not the outcome and score
						String value = reader.get("" + col.getTID());
						list.set(col.getTID() - 1, value);
					}
				}

				result.setAssay(assay);

				session.save(result);

				if (++counter % 100 == 0) {
					trx.commit();
					session.clear(); // else huge build up of hibernate memory
					// cache
					trx = session.beginTransaction();
				}
				if (counter % 10000 == 0)
					log.debug(String.format("AID %s: processed %s results", assay.getAID(), counter));
			}
		}
		log.debug(String.format("Processed AID: %s, contained %s results", assay.getAID(), counter));
		assay = (PCAssay) session.merge(assay);
		assay.setVersionChanged(false);
		session.save(assay);
		trx.commit();
	}

	protected String getOutcome(int score) {
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

	// public int populateRelations(int id, String fromDb, String toDb) throws
	// IOException, DocumentException {
	// Query query = HibernateUtil.getSession().createQuery(
	// "delete from Relation where fromId = ? and fromDb = ? and toDb = ?");
	// query.setInteger(0, id);
	// query.setString(1, "pcassay");
	// query.setString(2, "pcassay");
	// query.executeUpdate();
	// Document document =
	// EUtilsFactory.getInstance().getDocument("http://www.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi",
	// "dbfrom", fromDb,
	// "db", toDb, "id", "" + id);
	// return populateRelations(document);
	// }

	public int populateRelations(Session session, Document document) throws IOException, DocumentException {
		String fromDb = document.selectSingleNode("/eLinkResult/LinkSet/DbFrom").getText();
		String idStr = document.selectSingleNode("/eLinkResult/LinkSet/IdList/Id").getText();
		Long id = Long.parseLong(idStr);
		List<Node> linkSetDbs = document.selectNodes("/eLinkResult/LinkSet/LinkSetDb");

		Transaction trx = session.beginTransaction();
		int counter = 0;
		for (Node linkSetDb : linkSetDbs) {
			String toDb = linkSetDb.selectSingleNode("DbTo").getText();
			String linkName = linkSetDb.selectSingleNode("LinkName").getText();
			List<Node> ids = linkSetDb.selectNodes("Link/Id");
			for (Node idNode : ids) {
				long relatedId = Long.parseLong(idNode.getText());
				if (id == relatedId)
					continue;
				Relation relation = new Relation();
				relation.setRelationName(linkName);
				relation.setFromDb(fromDb);
				relation.setFromId(id);
				relation.setToDb(toDb);
				relation.setToId(relatedId);
				session.save(relation);
				if (counter++ % 100 == 0) {
					trx.commit();
					session.clear(); // cache grows hugely during very large
					// inserts
					trx = session.beginTransaction();
				}
			}
		}
		trx.commit();
		return counter;
	}

	//
	// public URL getAssayTable(int aid, int[] cids) throws Exception {
	// return getAssayTable(aid, cids, 5);
	// }
	//
	// public URL getAssayTable(int aid, int[] cids, int sleepSeconds) throws
	// Exception {
	// ArrayOfInt arr = new ArrayOfInt();
	// arr.set_int(cids);
	// InputList list = new InputList();
	// list.setIdType(PCIDType.eID_CID);
	// list.setIds(arr);
	// String cidListKey = pug.InputList(list).getListKey();
	// log.debug("CID ListKey = " + cidListKey);
	// InputAssay assay = new InputAssay();
	// assay.setAID(aid);
	// assay.setColumns(AssayColumnsType.eAssayColumns_Complete);
	// assay.setListKeySCIDs(cidListKey);
	// String assayKey = pug.InputAssay(assay).getAssayKey();
	// log.debug("AssayKey = " + assayKey);
	//
	// AssayDownload download = new AssayDownload();
	// download.setAssayKey(assayKey);
	// download.setAssayFormat(AssayFormatType.eAssayFormat_CSV);
	// download.setECompress(CompressType.eCompress_None);
	// String downloadKey = pug.AssayDownload(download).getDownloadKey();
	// log.debug("DownloadKey = " + downloadKey);
	//
	// AnyKeyType anyKey = new AnyKeyType();
	// anyKey.setAnyKey(downloadKey);
	// GetOperationStatus getStatus = new GetOperationStatus();
	// getStatus.setGetOperationStatus(anyKey);
	// StatusType status;
	// while ((status = pug.GetOperationStatus(getStatus).getStatus()) ==
	// StatusType.eStatus_Running
	// || status == StatusType.eStatus_Queued) {
	// log.debug("Waiting for download to finish...");
	// Thread.sleep(sleepSeconds * 1000);
	// }
	//
	// // On success, get the download URL, save to local file
	// if (status == StatusType.eStatus_Success) {
	// GetDownloadUrl getURL = new GetDownloadUrl();
	// getURL.setDownloadKey(downloadKey);
	// URL url = new URL(pug.GetDownloadUrl(getURL).getUrl());
	// log.debug("Success! Download URL = " + url.toString());
	// return url;
	//
	// } else {
	// GetStatusMessage message = new GetStatusMessage();
	// message.setGetStatusMessage(anyKey);
	// String msg = pug.GetStatusMessage(message).getMessage();
	// log.error(msg);
	// throw new Exception("Error: " + msg);
	// }
	// }
	//
	// public static void main(String[] args) throws Exception {
	// args = new CommandLineHandler().handle(args);
	// // List<Long> aids = EUtilsFactory.getInstance().getIds("all[filter]",
	// "pcassay");
	// // Collections.sort(aids, Collections.reverseOrder());
	// // System.out.println(StringUtils.join(aids,","));
	//		
	// PCAssay assay = new PCAssay();
	// assay.setAID(1811);
	// getInstance().populateAssayFromPug(assay);
	// }

	public Collection<Long> getNeighbors(Long id) throws Exception {
		Document document = EUtilsWebSession.getInstance().getDocument(
				"http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi", "dbfrom", "pcassay", "id", "" + id,
				"linkname", "pcassay_pcassay_neighbor_list");
		List<Node> linkSetDbs = document.selectNodes("/eLinkResult/LinkSet/LinkSetDb");
		Set<Long> relatedIds = new HashSet();
		int counter = 0;
		for (Node linkSetDb : linkSetDbs) {
			String linkName = linkSetDb.selectSingleNode("LinkName").getText();
			List<Node> ids = linkSetDb.selectNodes("Link/Id");
			for (Node idNode : ids) {
				Long relatedId = Long.parseLong(idNode.getText());
				relatedIds.add(relatedId);
			}
		}
		return relatedIds;
	}

	public Collection<Long> getRelatedAssays(Long aid) throws Exception {
		Collection<Long> neighborAids = (Set<Long>) getNeighbors(aid);

		// find summary aids for assay
		Document doc = EUtilsWebSession.getInstance().getSummary(neighborAids, "pcassay");
		Iterator<Element> iter = doc.getRootElement().elementIterator("DocSum");
		Long summaryAid = null;
		List<Long> summaryAids = new ArrayList<Long>();
		while (iter.hasNext()) {
			Element elem = iter.next();
			if (elem.selectSingleNode("Item[@Name='ActivityOutcomeMethod']").getText().equals("summary"))
				summaryAid = Long.parseLong(elem.selectSingleNode("Id").getText());
			summaryAids.add(summaryAid);
		}

		// find aids related to summary aids
		for (Long summary : summaryAids)
			neighborAids.addAll(getNeighbors(summaryAid));

		return neighborAids;
	}
	
	public List<Relation> getRelations(Long id, String fromDb, String toDb) throws Exception {
		Document document = EUtilsWebSession.getInstance().getDocument(
				"http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi", "dbfrom", fromDb, "db", toDb, "id", "" + id);
		return getRelations(document);
	}

	public List<Relation> getRelations(Document document) {
		String fromDb = document.selectSingleNode("/eLinkResult/LinkSet/DbFrom").getText();
		String idStr = document.selectSingleNode("/eLinkResult/LinkSet/IdList/Id").getText();
		Long id = Long.parseLong(idStr);
		List<Node> linkSetDbs = document.selectNodes("/eLinkResult/LinkSet/LinkSetDb");
		ArrayList<Relation> list = new ArrayList();
		for (Node linkSetDb : linkSetDbs) {
			String toDb = linkSetDb.selectSingleNode("DbTo").getText();
			String linkName = linkSetDb.selectSingleNode("LinkName").getText();
			List<Node> ids = linkSetDb.selectNodes("Link/Id");
			list.ensureCapacity(list.size() + ids.size());
			for (Node idNode : ids) {
				long relatedId = Long.parseLong(idNode.getText());
				if (id == relatedId)
					continue;
				Relation relation = new Relation();
				relation.setRelationName(linkName);
				relation.setFromDb(fromDb);
				relation.setFromId(id);
				relation.setToDb(toDb);
				relation.setToId(relatedId);
				list.add(relation);
			}
		}
		return list;
	}

}