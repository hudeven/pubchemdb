package edu.scripps.fl.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCUtils {
	
	private static final Logger log = LoggerFactory.getLogger(JDBCUtils.class);

	public static boolean tableExists(Connection conn, String tableName) throws SQLException {
		String[] TABLE_TYPES = {"TABLE"};
		DatabaseMetaData dbMetaData = conn.getMetaData();
		ResultSet rs = dbMetaData.getTables(null, null, tableName, TABLE_TYPES);
		boolean exists = rs.next();
		return exists;
	}
	
	public static void dropTable(Connection conn, String tableName) throws SQLException {
		if( tableExists(conn, tableName) ){
			Statement stmt  = conn.createStatement();
			stmt.executeUpdate("drop table " + tableName);
			log.info(String.format("Table %s has been dropped", tableName));
		}		
	}
}