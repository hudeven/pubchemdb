package edu.scripps.fl.jchem;

import java.util.Properties;

import chemaxon.util.ConnectionHandler;

public class JChemUtils {

	public static ConnectionHandler createConnectionHandler(Properties props) throws Exception {
       
		String url = props.getProperty("connection.url");
		String driver = props.getProperty("connection.driver_class");
		String user = props.getProperty("connection.username");
		String password = props.getProperty("connection.password");
		
        ConnectionHandler connectionHandler = new ConnectionHandler();
		connectionHandler.setDriver(driver);
		connectionHandler.setUrl(url);
		connectionHandler.setLoginName(user);
		connectionHandler.setPassword(password);
        
		connectionHandler.connect();
		return connectionHandler;
	}
}
