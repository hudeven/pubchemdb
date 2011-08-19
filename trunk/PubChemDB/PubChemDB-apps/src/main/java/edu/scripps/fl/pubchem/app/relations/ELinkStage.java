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
package edu.scripps.fl.pubchem.app.relations;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.stage.BaseStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pubchem.web.entrez.EUtilsWebSession;

public class ELinkStage extends BaseStage {

	private static final Logger log = LoggerFactory.getLogger(ELinkStage.class);

	private String databases;

	@Override
	public void preprocess() throws StageException {
		try {
			Set<String> dbs = EUtilsWebSession.getInstance().getDatabases();
			dbs.remove("pccompound");
			dbs.remove("pcsubstance");
			databases = StringUtils.join(dbs, ",");
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}

	@Override
	public void process(Object obj) throws StageException {
		Integer id = (Integer) obj;
		try {
			Thread.sleep(333);
			InputStream in = EUtilsWebSession.getInstance().getInputStream("http://www.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi", "dbfrom", "pcassay",
					"db", databases, "id", "" + id).call();
			File file = File.createTempFile("pubchem", "link.xml");
			IOUtils.copy(in, new FileOutputStream(file));
			emit(file);
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}
}