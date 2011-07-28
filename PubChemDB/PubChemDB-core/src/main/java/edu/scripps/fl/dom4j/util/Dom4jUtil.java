package edu.scripps.fl.dom4j.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dom4j.Document;
import org.dom4j.io.DOMReader;

public class Dom4jUtil {

	public static Document getDocument(InputStream inputStream) throws Exception {
		inputStream = decompress(inputStream);
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		DOMReader reader = new DOMReader();
		Document doc = reader.read(builder.parse(inputStream));
		inputStream.close();
//		doc.write(new OutputStreamWriter(System.out));
		return doc;
	}
	
	private static InputStream decompress(InputStream inputStream) throws IOException {
		inputStream = new BufferedInputStream(inputStream);
		inputStream.mark(1024);
	    try {
	    	inputStream = new GZIPInputStream(inputStream);
	    }
	    catch(IOException e) {
	    	inputStream.reset();
	    }
	    return inputStream;
	}
}
