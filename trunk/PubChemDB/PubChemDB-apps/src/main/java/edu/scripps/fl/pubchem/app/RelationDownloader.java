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
package edu.scripps.fl.pubchem.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.VFS;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pubchem.PubChemDB;
import edu.scripps.fl.pubchem.db.Relation;
import edu.scripps.fl.pubchem.util.CommandLineHandler;

public class RelationDownloader {

	private static final Logger log = LoggerFactory.getLogger(RelationDownloader.class);

	private static final String assayNeighborURL = "ftp://ftp.ncbi.nlm.nih.gov/pubchem/Bioassay/AssayNeighbors";

	public static void main(String[] args) throws Exception {
		new CommandLineHandler().handle(args);
		new RelationDownloader().call();
	}

	protected void update(long fromId, String name, Collection<Relation> relations) {
		Session session = PubChemDB.getSession();
		Transaction trx = session.beginTransaction();

		Query query = session.createQuery("delete from Relation where fromId = ? and relationName = ?");
		query.setLong(0, fromId);
		query.setString(1, name);
		query.executeUpdate();

		for (Relation relation : relations) {
			session.save(relation);
		}
		relations.clear();

		session.flush();
		trx.commit();
	}

	public void call() throws Exception {
		Pattern pattern = Pattern.compile("^AID(\\d+)\\s+AID(\\d+)$");

		FileObject folder = VFS.getManager().resolveFile(assayNeighborURL);
		for (FileObject rFile : folder.getChildren()) {
			String name = rFile.getName().getBaseName();
			log.info("Processing file: " + name);
			BufferedReader reader = new BufferedReader(new InputStreamReader(rFile.getContent().getInputStream()));
			String line = null;
			long lastFrom = 0;
			List<Relation> relations = new ArrayList(100);
			while (null != (line = reader.readLine())) {
				Matcher matcher = pattern.matcher(line);
				if (!matcher.matches())
					throw new java.lang.UnsupportedOperationException("Cannot determine AIDs from line: " + line);
				long from = Long.parseLong(matcher.group(1));
				long to = Long.parseLong(matcher.group(2));
				if (lastFrom == 0) // very first time only.
					lastFrom = from;
				if (from != lastFrom) { // when we change to the next aid in the file
					update(from, name, relations);
					PubChemDB.getSession().clear();
					lastFrom = from;
				}
				Relation relation = new Relation();
				relation.setFromDb("pcassay");
				relation.setToDb("pcassay");
				relation.setFromId(from);
				relation.setToId(to);
				relation.setRelationName(name);
				relations.add(relation);
			}
			update(lastFrom, name, relations);
		}
	}
}