package edu.scripps.fl.hibernate;

import java.net.URL;
import java.util.Properties;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

public class HibernateUtil {

	public static Properties getConnectionProperties(URL hibernateCfgXmlFile) throws Exception {
		Document document = new SAXReader().read(hibernateCfgXmlFile);
        String format = "/hibernate-configuration/session-factory/property[@name='connection.%s']";
        Properties props = new Properties();
        for(String prop: new String[]{"url","driver_class","username","password"}) {
        	String value = document.selectSingleNode( String.format(format, prop) ).getText();
        	props.put("connection." + prop, value);
        }
        return props;
	}
}
