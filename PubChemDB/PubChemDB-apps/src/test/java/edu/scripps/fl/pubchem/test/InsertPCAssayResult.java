package edu.scripps.fl.pubchem.test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.hibernate.Session;

import com.csvreader.CsvReader;

import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.PubChemFactory;
import edu.scripps.fl.pubchem.app.results.SourceColumnsInfo;
import edu.scripps.fl.pubchem.app.results.SourceColumnsInfo.Type;
import edu.scripps.fl.pubchem.db.PCAssay;
import edu.scripps.fl.pubchem.db.PCAssayResult;
import edu.scripps.fl.pubchem.util.CommandLineHandler;

public class InsertPCAssayResult {

	public static void main(String[] args) throws Exception {
		CommandLineHandler clh = new CommandLineHandler();
		args = clh.handle(args);
		
		CsvReader reader = new CsvReader(new BufferedReader( new InputStreamReader(new FileInputStream("c:\\home\\temp\\2551.csv"))), ',');
		reader.readHeaders();
		String[] headers = reader.getHeaders();
			
		Session session = PubChemDB.getSession();
		
		// Get assay with all columns in just one query
		PCAssay assay = PubChemFactory.getInstance().getAssay(session, 2551);
			
		SourceColumnsInfo colInfo = SourceColumnsInfo.newInstance(Type.FTP, assay, headers);
		
		int counter = 0;
		boolean moreRecords = reader.readRecord();
		while(moreRecords) {			
			counter++;
			String[] values = reader.getValues();
			moreRecords = reader.readRecord();
			for(long SID: colInfo.getSIDs(values)) {
				PCAssayResult result = colInfo.createAssayResult(SID, values);
			}
		}
	}
}
