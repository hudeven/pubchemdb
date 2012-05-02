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
package edu.scripps.fl.pubchem.app.assayxml;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.stage.BaseStage;
import org.dom4j.Document;
import org.dom4j.io.DOMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.scripps.fl.pubchem.PubChemFactory;
import edu.scripps.fl.pubchem.web.entrez.EUtilsWebSession;
import edu.scripps.fl.util.ProgressWriter;

public class ReadDocumentStage extends BaseStage {

	private static final Logger log = LoggerFactory.getLogger(ReadDocumentStage.class);

	private AtomicInteger ac = new AtomicInteger();

	private Set<Long> onHoldAidSet;

	private URL dataUrl;

	public URL getDataUrl() {
		return dataUrl;
	}

	public void setDataUrl(URL dataUrl) {
		this.dataUrl = dataUrl;
	}

	@Override
	public void preprocess() throws StageException {
		super.preprocess();
		try {
			onHoldAidSet = (Set<Long>) EUtilsWebSession.getInstance().getIds("\"hasonhold\"[filter]", "pcassay", new HashSet<Long>());
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}

	@Override
	public void process(Object obj) throws StageException {
		Long id = (Long) obj;
		try {
			int count = ac.incrementAndGet();
			if (!onHoldAidSet.contains(id)) { // no file when on hold
				InputStream is = PubChemFactory.getInstance().getXmlDescr(getDataUrl(), id.intValue());
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				DOMReader reader = new DOMReader();
				Document doc = reader.read(builder.parse(is));
				is.close();
				emit(doc);
			}
			if (count % 1000 == 0)
				log.debug(String.format("Read %s Bioassay XML documents", count));
		} catch (Exception ex) {
			throw new StageException(this, ex);
		}
	}
}