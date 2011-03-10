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
package edu.scripps.fl.pubchem.app.cids;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.stage.BaseStage;

import chemaxon.jchem.db.Importer;
import chemaxon.util.ConnectionHandler;
import edu.scripps.fl.pubchem.PubChemDB;

public class ImportSDFStage extends BaseStage {

	private String connectionUrl = "";
	private String driverClass = "";
	private String fieldConnections = "";
	private String password = "";
	private String tableName = "";
	private String username = "";

	public ImportSDFStage() {
		configureConnectionFromHibernateProperties();
	}
	
	public void configureConnectionFromHibernateProperties() {
		Properties props = PubChemDB.getConnectionProperties();
		setDriverClass(props.getProperty("connection.driver_class"));
		setConnectionUrl(props.getProperty("connection.url"));
		setUsername(props.getProperty("connection.username"));
		setPassword(props.getProperty("connection.password"));
	}

	public ConnectionHandler getConnectionHandler() throws Exception {
		ConnectionHandler ch = new ConnectionHandler();
		ch.setDriver(getDriverClass());
		ch.setUrl(getConnectionUrl());
		ch.setLoginName(getUsername());
		ch.setPassword(getPassword());
		ch.connect();
		return ch;
	}

	public String getConnectionUrl() {
		return connectionUrl;
	}

	public String getDriverClass() {
		return driverClass;
	}

	public String getFieldConnections() {
		return fieldConnections;
	}

	public String getPassword() {
		return password;
	}

	public String getTableName() {
		return tableName;
	}

	public String getUsername() {
		return username;
	}

	@Override
	public void preprocess() throws StageException {
		super.preprocess();
		try {

		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}

	@Override
	public void process(Object obj) throws StageException {
		URL url = (URL) obj;
		try {
			InputStream inputStream = url.openStream();
			inputStream = Importer.decompress(inputStream);

			Importer importer = new Importer();
			importer.setConnectionHandler(getConnectionHandler());
			importer.setTableName(getTableName());
			importer.setFieldConnections(getFieldConnections());
			importer.setLinesToCheck(200);
			importer.setHaltOnError(false);
			importer.setStoreImportedIDs(true);
			importer.setStoreDuplicates(true);
			importer.setDuplicateImportAllowed(true);
			importer.setInfoStream(System.out);
			importer.setInput(inputStream);

			importer.init();

			importer.importMols();

			Integer count = importer.getImportedNumber();
			emit(count);

		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}

	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public void setFieldConnections(String fieldConnections) {
		this.fieldConnections = fieldConnections;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setUsername(String username) {
		this.username = username;
	}

//	public static void main(String[] args) throws Exception {
//		Configuration config = PubChemDB.getAnnotationConfiguration(new File(args[0]).toURI().toURL());
//		ImportSDFStage stage = new ImportSDFStage();
//		stage.configureConnectionFromHibernateProperties(config.getProperties());
//		stage.setTableName("pccompound");
//		stage
//				.setFieldConnections("CID=PUBCHEM_COMPOUND_CID;h_bond_acceptors=PUBCHEM_CACTVS_HBOND_ACCEPTOR;h_bond_donors=PUBCHEM_CACTVS_HBOND_DONOR;iupac_name=PUBCHEM_IUPAC_NAME;traditional_name=PUBCHEM_IUPAC_TRADITIONAL_NAME;rotatable_bonds=PUBCHEM_CACTVS_ROTATABLE_BOND;xlogp=PUBCHEM_XLOGP3;total_charge=PUBCHEM_TOTAL_CHARGE;tpsa=PUBCHEM_CACTVS_TPSA;exact_weight=PUBCHEM_EXACT_MASS");
//		File file = new File(args[1]);
//		stage.process(file.toURI().toURL());
//	}
}