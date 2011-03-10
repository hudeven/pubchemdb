package edu.scripps.fl.pubchem;

import java.net.URL;
import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chemaxon.jchem.db.DatabaseProperties;
import chemaxon.jchem.db.StructureTableOptions;
import chemaxon.jchem.db.TableTypeConstants;
import chemaxon.jchem.db.UpdateHandler;
import chemaxon.util.ConnectionHandler;
import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.db.PCCompound;
import edu.scripps.fl.jchem.JChemUtils;
import edu.scripps.fl.jdbc.JDBCUtils;

public class PubChemDBwJChem extends PubChemDB {
	
	private static final Logger log = LoggerFactory.getLogger(PubChemDBwJChem.class);
	
	public static final String TABLE_JCHEMPROPERTIES = "JCHEMPROPERTIES";
	public static final String TABLE_JCHEMPROPERTIES_CR = TABLE_JCHEMPROPERTIES + "_CR";
	public static final String TABLE_PCCOMPOUND = PCCompound.class.getName().replaceAll("[^\\.]+\\.", "");
	public static final String TABLE_PCCOMPOUND_UL = TABLE_PCCOMPOUND + "_UL";
	
	public static void setUp(URL cfgFile) throws Exception {
		PubChemDB.setUp(cfgFile);
		ConnectionHandler ch = JChemUtils.createConnectionHandler(PubChemDB.getConnectionProperties());
		if(! JDBCUtils.tableExists(ch.getConnection(), TABLE_JCHEMPROPERTIES) ) {
			log.info(TABLE_JCHEMPROPERTIES + " table does not exist, creating JChem tables ...");
			configureJChemDB(ch);
			PubChemDB.createSessionFactory("update", cfgFile);
		}
		else
			PubChemDB.createSessionFactory("", cfgFile);
		ch.close();
	}

	protected static void configureJChemDB(ConnectionHandler connectionHandler) throws Exception {
		if(!DatabaseProperties.propertyTableExists(connectionHandler)){
			dropJChemTables(connectionHandler.getConnection());
			log.info("Creating JChem tables");
			DatabaseProperties.createPropertyTable(connectionHandler);
			StructureTableOptions tableOptions = new StructureTableOptions();
			tableOptions.name = TABLE_PCCOMPOUND;
			tableOptions.tableType = TableTypeConstants.TABLE_TYPE_DEFAULT;
			// Setting finger print parameters according to table type
			tableOptions.fp_numberOfInts = TableTypeConstants.FP_DEFAULT_LENGTH_IN_INTS[tableOptions.tableType];
			tableOptions.fp_numberOfOnes = TableTypeConstants.FP_DEFAULT_BITS_PER_PATTERN[tableOptions.tableType];
			tableOptions.fp_numberOfEdges = TableTypeConstants.FP_DEFAULT_PATTERN_LENGTH[tableOptions.tableType];
			UpdateHandler.createStructureTable( connectionHandler, tableOptions);
		}
	}
	
	protected static void dropJChemTables(Connection conn) throws Exception {
		JDBCUtils.dropTable(conn, TABLE_JCHEMPROPERTIES);
		JDBCUtils.dropTable(conn, TABLE_JCHEMPROPERTIES_CR);
		JDBCUtils.dropTable(conn, TABLE_PCCOMPOUND_UL);
		JDBCUtils.dropTable(conn, TABLE_PCCOMPOUND);
	}
}