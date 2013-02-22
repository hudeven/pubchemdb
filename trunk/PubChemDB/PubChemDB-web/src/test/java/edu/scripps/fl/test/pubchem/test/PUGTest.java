package edu.scripps.fl.test.pubchem.test;

import java.io.File;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import edu.scripps.fl.pubchem.web.pug.PUGSoapFactory;
import edu.scripps.fl.pubchem.web.pug.PowerUserGateway;

public class PUGTest {
	
	public static void main(String[] args) throws Exception{

//			PowerUserGateway pug = PowerUserGateway.newInstance();
//			URL url = new File("C:\\Users\\scanny\\Desktop\\std_pug.xml").toURI().toURL();
//			Document doc =PowerUserGateway.getDocument(url.openStream());
//			pug.setRequest( doc );
//			pug.submitAndWait(5000);
//			url = pug.getResponseURL();
//			System.out.println(url);
			
			int[] SIDs = new int[]{125268667};
			System.out.println(PUGSoapFactory.getInstance().getSDFilefromSIDs(SIDs));
	}

}
