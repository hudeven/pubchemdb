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

import java.io.Serializable;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.persistence.Table;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.hibernate.HibernateStaticService;
import edu.scripps.fl.hibernate.HibernateUtil;
import edu.scripps.fl.jdbc.JDBCUtils;
import edu.scripps.fl.pubchem.db.PCAssay;
import edu.scripps.fl.pubchem.db.PCAssayColumn;
import edu.scripps.fl.pubchem.db.PCAssayPanel;
import edu.scripps.fl.pubchem.db.PCAssayResult;
import edu.scripps.fl.pubchem.db.PCAssayXRef;
import edu.scripps.fl.pubchem.db.PCCompound;
import edu.scripps.fl.pubchem.db.PCCurve;
import edu.scripps.fl.pubchem.db.Relation;
import edu.scripps.fl.pubchem.db.XRef;

public class PubChemDB {

	private static final Logger log = LoggerFactory.getLogger(PubChemDB.class);
	
	private static final String KEY = "PubChem";
	
	public static Set<Class<? extends Serializable>> ENTITY_CLASSES =  new HashSet(Arrays.asList(PCAssay.class,PCAssayColumn.class,PCAssayResult.class,PCCompound.class,PCAssayPanel.class,PCAssayXRef.class,XRef.class,Relation.class,PCCurve.class));
	
	private static URL hibernateCfgXmlFile = null;
	private static Properties connectionProperties = new Properties();
	
	public static void setUp(URL hibernateCfgXmlFile) throws Exception {
		PubChemDB.hibernateCfgXmlFile = hibernateCfgXmlFile;
		connectionProperties = HibernateUtil.getConnectionProperties(hibernateCfgXmlFile);
		if(!isSchemaCreated()) {
			log.info("PCAssay table does not exist, creating schema...");
			createSessionFactory("create", hibernateCfgXmlFile);
		}
		else {
			log.info("PCAssay table exists, assuming full schema exists...");
			createSessionFactory("", hibernateCfgXmlFile);
		}
	}
	
	public static Properties getConnectionProperties() {
		return connectionProperties;
	}
	
	public static Connection getConnection() throws Exception {
		Properties props = HibernateUtil.getConnectionProperties(hibernateCfgXmlFile);
        
        String url = props.getProperty("connection.url");
		String driver = props.getProperty("connection.driver_class");
		String user = props.getProperty("connection.username");
		String password = props.getProperty("connection.password");
		
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(url, user, password);
		return conn;
	}
	
	protected static boolean isSchemaCreated() throws Exception {
		Table table = PCAssay.class.getAnnotation(Table.class);
		String name = table.name();
		Connection conn = getConnection();
		boolean exists = JDBCUtils.tableExists(conn, name);
		conn.close();
		return exists;
	}
	
	public static void createSessionFactory(String mode, URL hibernateCfgXmlFile) throws Exception {
		log.info(String.format("hibernate.hbm2ddl.auto = '%s'", mode));
		AnnotationConfiguration config = new AnnotationConfiguration();
		config.configure(hibernateCfgXmlFile);
		if( null != mode && ! "".equals(mode) )
			config.setProperty("hibernate.hbm2ddl.auto", mode);
		else
			config.getProperties().remove("hibernate.hbm2ddl.auto");
    	for(Class<?> clazz : ENTITY_CLASSES)
    		config.addAnnotatedClass(clazz);

		SessionFactory factory = config.buildSessionFactory();
		setSessionFactory(factory);
	}
	
	public static Session getSession() {
		return HibernateStaticService.getHibernateSession(KEY);
	}

	public static SessionFactory getSessionFactory() {
		return HibernateStaticService.getHibernateSessionFactory(KEY);
	}

	public static void setSessionFactory(SessionFactory sessionFactory) {
		HibernateStaticService.setHibernateSessionFactory(KEY, sessionFactory);
	}	
	
	private static void delete(Session session, List list) {
		for(Object obj: list) {
			session.delete(obj);
		}
		list.clear();
	}
	
	public static void saveAssay(Session session, PCAssay assay) {
		PCAssay storedAssay = null;
		if( assay.getAID() > 0 ) {
			Query query = session.createQuery("from PCAssay where assay_aid = " + assay.getAID());
			storedAssay = (PCAssay) query.uniqueResult();
	    }
		if( storedAssay != null ) {
			
			if( storedAssay.getVersion() != assay.getVersion() && storedAssay.getRevision() != assay.getRevision() )
				assay.setVersionChanged(true);
			
			delete(session, storedAssay.getPanels());
			delete(session, storedAssay.getColumns());
			delete(session, storedAssay.getAssayXRefs());
			storedAssay.getCategorizedComments().clear();
			session.save(storedAssay);
			session.flush();

			for(PCAssayPanel panel: assay.getPanels()){
				panel.setAssay(storedAssay);
				storedAssay.getPanels().add(panel);
				session.save(panel);
			}
			for(PCAssayXRef assayXRef: assay.getAssayXRefs()) {
				assayXRef.setAssay(storedAssay);
				storedAssay.getAssayXRefs().add(assayXRef);
				session.save(assayXRef);
			}
			for(PCAssayColumn col: assay.getColumns()) {
				col.setAssay(storedAssay);
				storedAssay.getColumns().add(col);
				session.save(col);
			}
			assay.setId(storedAssay.getId());
			session.evict(storedAssay);
			session.update(assay);
		}
		else {
			assay.setVersionChanged(true);
			session.save(assay);
			for(PCAssayPanel panel: assay.getPanels()){
				session.save(panel);
			}
			for(PCAssayXRef assayXRef: assay.getAssayXRefs()) {
				session.save(assayXRef);
			}
			for(PCAssayColumn col: assay.getColumns()) {
				session.save(col);
			}
		}
	}
	
	public static void saveXRefs(Session session, PCAssay assay) {
		for(PCAssayXRef assayXRef: assay.getAssayXRefs()) {
			XRef xref = assayXRef.getXRef();
			
			Criteria criteria = session.createCriteria(XRef.class);
			criteria.add(Restrictions.eq("database", xref.getDatabase()));
			criteria.add(Restrictions.eq("XRefId", xref.getXRefId()));
			XRef dbxref = (XRef) criteria.uniqueResult();
			if( null == dbxref)
				session.save(xref);
			else {
				xref.setId( dbxref.getId() );
				xref = (XRef) session.merge(xref);
			}
		}
	}
}